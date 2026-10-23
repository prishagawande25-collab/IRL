package com.example.irl.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "irl_secure_preferences")

class AppPreferencesRepository private constructor(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        @Volatile
        private var INSTANCE: AppPreferencesRepository? = null

        fun getInstance(context: Context): AppPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        private val KEY_SESSION_ACTIVE = booleanPreferencesKey("session_active")
        private val KEY_SESSION_START_TIME = longPreferencesKey("session_start_time")
        private val KEY_SESSION_END_TIME = longPreferencesKey("session_end_time")
        private val KEY_SESSION_MODE = stringPreferencesKey("session_mode")
        private val KEY_SESSION_BLOCKED_PACKAGES = stringSetPreferencesKey("session_blocked_packages")
        private val KEY_BREAK_ACTIVE = booleanPreferencesKey("session_break_active")
        private val KEY_BREAK_END_TIME = longPreferencesKey("session_break_end_time")

        private val KEY_SELECTED_APPS = stringSetPreferencesKey("configured_blocked_apps")

        private val KEY_BEDTIME_ENABLED = booleanPreferencesKey("bedtime_enabled")
        private val KEY_BEDTIME_START_H = intPreferencesKey("bedtime_start_h")
        private val KEY_BEDTIME_START_M = intPreferencesKey("bedtime_start_m")
        private val KEY_BEDTIME_END_H = intPreferencesKey("bedtime_end_h")
        private val KEY_BEDTIME_END_M = intPreferencesKey("bedtime_end_m")
        private val KEY_BEDTIME_DAYS = stringSetPreferencesKey("bedtime_days")
        private val KEY_BEDTIME_PACKAGES = stringSetPreferencesKey("bedtime_packages")

        private val KEY_DEMO_DATA = booleanPreferencesKey("demo_data_enabled")
        private val KEY_USER_POINTS = intPreferencesKey("user_points")
        private val KEY_FOCUS_STREAK = intPreferencesKey("focus_streak")
        private val KEY_DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")

        private val KEY_BEST_NUMBER_RUSH_MS = longPreferencesKey("best_number_rush_ms")
        private val KEY_BEST_MEMORY_MATCH_MOVES = intPreferencesKey("best_memory_match_moves")
        private val KEY_BEST_REACTION_TIME_MS = longPreferencesKey("best_reaction_time_ms")
        private val KEY_UNLOCKED_ACHIEVEMENTS = stringSetPreferencesKey("unlocked_achievements")
        private val KEY_CUSTOM_REWARDS = stringSetPreferencesKey("custom_rewards_set")
        private val KEY_REDEMPTIONS_LIST = stringSetPreferencesKey("redemptions_history_set")
    }

    val bestNumberRushMsFlow: Flow<Long> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[KEY_BEST_NUMBER_RUSH_MS] ?: 0L }

    suspend fun saveNumberRushScore(timeMs: Long): Boolean {
        var isNewBest = false
        dataStore.edit { prefs ->
            val currentBest = prefs[KEY_BEST_NUMBER_RUSH_MS] ?: 0L
            if (currentBest == 0L || timeMs < currentBest) {
                prefs[KEY_BEST_NUMBER_RUSH_MS] = timeMs
                isNewBest = true
            }
        }
        return isNewBest
    }

    val bestMemoryMatchMovesFlow: Flow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[KEY_BEST_MEMORY_MATCH_MOVES] ?: 0 }

    suspend fun saveMemoryMatchScore(moves: Int): Boolean {
        var isNewBest = false
        dataStore.edit { prefs ->
            val currentBest = prefs[KEY_BEST_MEMORY_MATCH_MOVES] ?: 0
            if (currentBest == 0 || moves < currentBest) {
                prefs[KEY_BEST_MEMORY_MATCH_MOVES] = moves
                isNewBest = true
            }
        }
        return isNewBest
    }

    val bestReactionTimeMsFlow: Flow<Long> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[KEY_BEST_REACTION_TIME_MS] ?: 0L }

    suspend fun saveReactionTimeScore(timeMs: Long): Boolean {
        var isNewBest = false
        dataStore.edit { prefs ->
            val currentBest = prefs[KEY_BEST_REACTION_TIME_MS] ?: 0L
            if (currentBest == 0L || timeMs < currentBest) {
                prefs[KEY_BEST_REACTION_TIME_MS] = timeMs
                isNewBest = true
            }
        }
        return isNewBest
    }

    val unlockedAchievementsFlow: Flow<Set<String>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[KEY_UNLOCKED_ACHIEVEMENTS] ?: emptySet() }

    suspend fun unlockAchievement(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_UNLOCKED_ACHIEVEMENTS] ?: emptySet()
            prefs[KEY_UNLOCKED_ACHIEVEMENTS] = current + id
        }
    }

    val customRewardsFlow: Flow<Set<String>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[KEY_CUSTOM_REWARDS] ?: emptySet() }

    suspend fun addCustomReward(encoded: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_CUSTOM_REWARDS] ?: emptySet()
            prefs[KEY_CUSTOM_REWARDS] = current + encoded
        }
    }

    suspend fun removeCustomReward(encoded: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_CUSTOM_REWARDS] ?: emptySet()
            prefs[KEY_CUSTOM_REWARDS] = current - encoded
        }
    }

    val redemptionsHistoryFlow: Flow<Set<String>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[KEY_REDEMPTIONS_LIST] ?: emptySet() }

    suspend fun redeemReward(title: String, cost: Int): Boolean {
        var success = false
        dataStore.edit { prefs ->
            val points = prefs[KEY_USER_POINTS] ?: 0
            if (points >= cost) {
                prefs[KEY_USER_POINTS] = points - cost
                val history = prefs[KEY_REDEMPTIONS_LIST] ?: emptySet()
                val entry = "${System.currentTimeMillis()}|#|$title|#|$cost"
                prefs[KEY_REDEMPTIONS_LIST] = history + entry
                success = true
            }
        }
        return success
    }

    val dailyGoalMinutesFlow: Flow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[KEY_DAILY_GOAL_MINUTES] ?: 180 }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_DAILY_GOAL_MINUTES] = minutes
        }
    }

    val activeSessionFlow: Flow<ActiveSessionState> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val isActive = prefs[KEY_SESSION_ACTIVE] ?: false
            val startTimeMs = prefs[KEY_SESSION_START_TIME] ?: 0L
            val endTimeMs = prefs[KEY_SESSION_END_TIME] ?: 0L
            val mode = BlockMode.fromString(prefs[KEY_SESSION_MODE])
            val blocked = prefs[KEY_SESSION_BLOCKED_PACKAGES] ?: emptySet()
            val isBreakActive = prefs[KEY_BREAK_ACTIVE] ?: false
            val breakEndTimeMs = prefs[KEY_BREAK_END_TIME] ?: 0L

            val now = System.currentTimeMillis()
            // Fail open: if current time has passed session end time, session is no longer active
            val effectiveActive = isActive && now < endTimeMs
            val effectiveBreak = isBreakActive && now < breakEndTimeMs

            ActiveSessionState(
                isActive = effectiveActive,
                startTimeMs = startTimeMs,
                endTimeMs = endTimeMs,
                mode = mode,
                blockedPackages = blocked,
                isBreakActive = effectiveBreak,
                breakEndTimeMs = breakEndTimeMs
            )
        }

    suspend fun getActiveSessionSnapshot(): ActiveSessionState {
        return activeSessionFlow.first()
    }

    suspend fun startSession(durationMinutes: Int, mode: BlockMode, packages: Set<String>) {
        val now = System.currentTimeMillis()
        val end = now + (durationMinutes * 60 * 1000L)
        dataStore.edit { prefs ->
            prefs[KEY_SESSION_ACTIVE] = true
            prefs[KEY_SESSION_START_TIME] = now
            prefs[KEY_SESSION_END_TIME] = end
            prefs[KEY_SESSION_MODE] = mode.name
            prefs[KEY_SESSION_BLOCKED_PACKAGES] = packages
            prefs[KEY_BREAK_ACTIVE] = false
            prefs[KEY_BREAK_END_TIME] = 0L
        }
    }

    suspend fun stopSession() {
        dataStore.edit { prefs ->
            prefs[KEY_SESSION_ACTIVE] = false
            prefs[KEY_SESSION_START_TIME] = 0L
            prefs[KEY_SESSION_END_TIME] = 0L
            prefs[KEY_SESSION_BLOCKED_PACKAGES] = emptySet()
            prefs[KEY_BREAK_ACTIVE] = false
            prefs[KEY_BREAK_END_TIME] = 0L
        }
    }

    suspend fun grantEmergencyBreak(breakMinutes: Int = 5) {
        val now = System.currentTimeMillis()
        val breakEnd = now + (breakMinutes * 60 * 1000L)
        dataStore.edit { prefs ->
            prefs[KEY_BREAK_ACTIVE] = true
            prefs[KEY_BREAK_END_TIME] = breakEnd
        }
    }

    val selectedBlockedPackagesFlow: Flow<Set<String>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[KEY_SELECTED_APPS] ?: emptySet() }

    suspend fun setSelectedBlockedPackages(packages: Set<String>) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_APPS] = packages
        }
    }

    val bedtimeConfigFlow: Flow<BedtimeConfig> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            BedtimeConfig(
                enabled = prefs[KEY_BEDTIME_ENABLED] ?: false,
                startHour = prefs[KEY_BEDTIME_START_H] ?: 22,
                startMinute = prefs[KEY_BEDTIME_START_M] ?: 30,
                endHour = prefs[KEY_BEDTIME_END_H] ?: 6,
                endMinute = prefs[KEY_BEDTIME_END_M] ?: 30,
                daysOfWeek = (prefs[KEY_BEDTIME_DAYS] ?: setOf("1", "2", "3", "4", "5", "6", "7"))
                    .mapNotNull { it.toIntOrNull() }.toSet(),
                blockedPackages = prefs[KEY_BEDTIME_PACKAGES] ?: emptySet()
            )
        }

    suspend fun updateBedtimeConfig(config: BedtimeConfig) {
        dataStore.edit { prefs ->
            prefs[KEY_BEDTIME_ENABLED] = config.enabled
            prefs[KEY_BEDTIME_START_H] = config.startHour
            prefs[KEY_BEDTIME_START_M] = config.startMinute
            prefs[KEY_BEDTIME_END_H] = config.endHour
            prefs[KEY_BEDTIME_END_M] = config.endMinute
            prefs[KEY_BEDTIME_DAYS] = config.daysOfWeek.map { it.toString() }.toSet()
            prefs[KEY_BEDTIME_PACKAGES] = config.blockedPackages
        }
    }

    val demoDataEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[KEY_DEMO_DATA] ?: false }

    suspend fun setDemoDataEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_DEMO_DATA] = enabled
        }
    }

    val userPointsFlow: Flow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[KEY_USER_POINTS] ?: 0 }

    suspend fun addPoints(points: Int) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_USER_POINTS] ?: 0
            prefs[KEY_USER_POINTS] = (current + points).coerceAtLeast(0)
        }
    }

    val focusStreakFlow: Flow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[KEY_FOCUS_STREAK] ?: 0 }

    suspend fun clearAllData() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
