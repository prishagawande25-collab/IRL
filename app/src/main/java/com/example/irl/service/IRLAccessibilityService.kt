package com.example.irl.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.irl.data.ActiveSessionState
import com.example.irl.data.AppPreferencesRepository
import com.example.irl.data.BedtimeConfig
import com.example.irl.ui.block.BlockingActivity
import com.example.irl.util.AppListProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Calendar

class IRLAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var repository: AppPreferencesRepository

    @Volatile
    private var currentSession: ActiveSessionState = ActiveSessionState()

    @Volatile
    private var bedtimeConfig: BedtimeConfig = BedtimeConfig()

    private var lastBlockedPackage: String? = null
    private var lastBlockedTimeMs: Long = 0L

    companion object {
        @Volatile
        var isServiceRunning: Boolean = false
            private set

        fun isAccessibilityEnabled(context: Context): Boolean {
            return try {
                val enabledServices = android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: return false
                val expected = "${context.packageName}/${IRLAccessibilityService::class.java.canonicalName}"
                enabledServices.split(':').any { it.equals(expected, ignoreCase = true) }
            } catch (_: Exception) {
                false
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = AppPreferencesRepository.getInstance(applicationContext)

        // Observe session state changes reactively
        repository.activeSessionFlow
            .onEach { session ->
                currentSession = session
            }
            .launchIn(serviceScope)

        repository.bedtimeConfigFlow
            .onEach { config ->
                bedtimeConfig = config
            }
            .launchIn(serviceScope)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        // Strictly read package name only. Never read window content, text, or keystrokes.
        val packageName = event.packageName?.toString() ?: return

        // Safety whitelist: never block system dialer, settings, system UI, launchers, or IRL itself.
        if (AppListProvider.isUnblockablePackage(this, packageName)) {
            return
        }

        val now = System.currentTimeMillis()

        // 1. Check Active Session Block
        val session = currentSession
        val isSessionActive = session.isActive && now < session.endTimeMs
        val isBreakActive = session.isBreakActive && now < session.breakEndTimeMs
        val isSessionBlocked = isSessionActive && !isBreakActive && session.blockedPackages.contains(packageName)

        // 2. Check Bedtime Schedule Block
        val isBedtimeBlocked = checkBedtimeBlock(packageName, now)

        if (isSessionBlocked || isBedtimeBlocked) {
            // Debounce within 1200ms for the same package to avoid re-triggering rapid intents
            if (packageName == lastBlockedPackage && (now - lastBlockedTimeMs) < 1200L) {
                return
            }

            lastBlockedPackage = packageName
            lastBlockedTimeMs = now

            val targetEndTime = if (isSessionBlocked) session.endTimeMs else calculateBedtimeEndTimeMs(now)
            val modeName = if (isSessionBlocked) session.mode.name else "FOCUS"

            val intent = Intent(this, BlockingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(BlockingActivity.EXTRA_BLOCKED_PACKAGE, packageName)
                putExtra(BlockingActivity.EXTRA_END_TIME, targetEndTime)
                putExtra(BlockingActivity.EXTRA_MODE, modeName)
                putExtra(BlockingActivity.EXTRA_IS_BEDTIME, isBedtimeBlocked && !isSessionBlocked)
            }
            startActivity(intent)
        }
    }

    private fun checkBedtimeBlock(packageName: String, now: Long): Boolean {
        val config = bedtimeConfig
        if (!config.enabled || !config.blockedPackages.contains(packageName)) {
            return false
        }

        val calendar = Calendar.getInstance().apply { timeInMillis = now }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        if (!config.daysOfWeek.contains(dayOfWeek)) {
            return false
        }

        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val startMinutes = config.startHour * 60 + config.startMinute
        val endMinutes = config.endHour * 60 + config.endMinute

        return if (startMinutes < endMinutes) {
            // Same day bedtime window (e.g. 13:00 to 15:00)
            currentMinutes in startMinutes until endMinutes
        } else {
            // Overnight bedtime window (e.g. 22:30 to 06:30)
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        }
    }

    private fun calculateBedtimeEndTimeMs(now: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = now }
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val endMinutes = bedtimeConfig.endHour * 60 + bedtimeConfig.endMinute

        val targetCal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, bedtimeConfig.endHour)
            set(Calendar.MINUTE, bedtimeConfig.endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (currentMinutes >= endMinutes) {
            targetCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return targetCal.timeInMillis
    }

    override fun onInterrupt() {
        // Safe fail-open no-op
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        serviceScope.cancel()
    }
}
