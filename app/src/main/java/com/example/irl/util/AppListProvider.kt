package com.example.irl.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.example.irl.data.InstalledAppItem

object AppListProvider {

    private val CRITICAL_SYSTEM_PACKAGES = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.google.android.settings",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.phone",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.server.telecom"
    )

    fun isUnblockablePackage(context: Context, packageName: String): Boolean {
        if (packageName.isBlank()) return true
        if (packageName == context.packageName) return true
        if (CRITICAL_SYSTEM_PACKAGES.contains(packageName)) return true

        // Check if package is the default home/launcher
        val defaultHome = getDefaultLauncherPackage(context)
        if (packageName == defaultHome) return true

        // Check if package is the default dialer
        val defaultDialer = getDefaultDialerPackage(context)
        if (packageName == defaultDialer) return true

        return false
    }

    fun getDefaultLauncherPackage(context: Context): String? {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName
    }

    fun getDefaultDialerPackage(context: Context): String? {
        val intent = Intent(Intent.ACTION_DIAL)
        val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName
    }

    fun getInstalledLaunchableApps(
        context: Context,
        selectedPackages: Set<String>
    ): List<InstalledAppItem> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
        val unblockablePackages = mutableSetOf<String>()
        unblockablePackages.addAll(CRITICAL_SYSTEM_PACKAGES)
        unblockablePackages.add(context.packageName)
        getDefaultLauncherPackage(context)?.let { unblockablePackages.add(it) }
        getDefaultDialerPackage(context)?.let { unblockablePackages.add(it) }

        val seen = mutableSetOf<String>()
        val result = mutableListOf<InstalledAppItem>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg != null && !seen.contains(pkg) && !unblockablePackages.contains(pkg)) {
                seen.add(pkg)
                val label = resolveInfo.loadLabel(pm)?.toString() ?: pkg
                result.add(
                    InstalledAppItem(
                        packageName = pkg,
                        appName = label,
                        isBlocked = selectedPackages.contains(pkg)
                    )
                )
            }
        }

        return result.sortedBy { it.appName.lowercase() }
    }

    fun getAppLabel(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName
        }
    }

    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            null
        }
    }
}
