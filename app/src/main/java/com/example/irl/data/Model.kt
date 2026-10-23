package com.example.irl.data

enum class BlockMode(val displayName: String, val shortDescription: String) {
    GENTLE(
        displayName = "Gentle",
        shortDescription = "Dismissible block with a 10-second mindful cooldown pause."
    ),
    FOCUS(
        displayName = "Focus",
        shortDescription = "Standard focus session with intentional friction."
    ),
    DEEP_FOCUS(
        displayName = "Deep Focus",
        shortDescription = "Strict lockdown. No pause or skip until session completes."
    );

    companion object {
        fun fromString(name: String?): BlockMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: FOCUS
        }
    }
}

data class ActiveSessionState(
    val isActive: Boolean = false,
    val startTimeMs: Long = 0L,
    val endTimeMs: Long = 0L,
    val mode: BlockMode = BlockMode.FOCUS,
    val blockedPackages: Set<String> = emptySet(),
    val isBreakActive: Boolean = false,
    val breakEndTimeMs: Long = 0L
) {
    val isExpired: Boolean
        get() = isActive && System.currentTimeMillis() >= endTimeMs

    val isBreakValid: Boolean
        get() = isBreakActive && System.currentTimeMillis() < breakEndTimeMs

    val remainingMs: Long
        get() {
            if (!isActive) return 0L
            val left = endTimeMs - System.currentTimeMillis()
            return if (left > 0) left else 0L
        }
}

data class BedtimeConfig(
    val enabled: Boolean = false,
    val startHour: Int = 22,
    val startMinute: Int = 30,
    val endHour: Int = 6,
    val endMinute: Int = 30,
    val daysOfWeek: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7), // 1=Sunday .. 7=Saturday
    val blockedPackages: Set<String> = emptySet()
)

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean = false
)
