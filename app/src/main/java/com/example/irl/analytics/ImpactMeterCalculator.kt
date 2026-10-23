package com.example.irl.analytics

import androidx.compose.ui.graphics.Color
import com.example.irl.ui.theme.ElectricBlue
import com.example.irl.ui.theme.SuccessEmerald
import com.example.irl.ui.theme.VioletAccent
import com.example.irl.ui.theme.WarningAmber

data class ImpactNote(
    val title: String,
    val message: String,
    val category: String
)

data class ImpactScoreResult(
    val score: Int,
    val ratingTitle: String,
    val ratingColor: Color,
    val goalPoints: Int,       // Out of 70
    val bedtimePoints: Int,    // Out of 15
    val focusPoints: Int,      // Out of 15
    val formulaExplanation: String,
    val practicalNotes: List<ImpactNote>
)

object ImpactMeterCalculator {

    fun calculate(
        todayUsageMs: Long,
        dailyGoalMinutes: Int,
        isBedtimeActiveToday: Boolean,
        activeFocusSessionsCompleted: Int
    ): ImpactScoreResult {
        val todayMinutes = todayUsageMs / (1000 * 60)
        val goalMinutes = dailyGoalMinutes.coerceAtLeast(30)

        // 1. Goal Adherence Points (Max 70 points)
        val goalPoints = if (todayMinutes <= goalMinutes) {
            70
        } else {
            val overrunMinutes = todayMinutes - goalMinutes
            val penalty = ((overrunMinutes.toFloat() / goalMinutes.toFloat()) * 70f).toInt()
            (70 - penalty).coerceIn(0, 70)
        }

        // 2. Bedtime Protection Points (Max 15 points)
        // Rewards having bedtime schedule active and respecting rest hours
        val bedtimePoints = if (isBedtimeActiveToday) 15 else 8

        // 3. Deliberate Focus Activity Bonus (Max 15 points)
        // Rewards active self-control sessions and micro-breaks
        val focusPoints = (activeFocusSessionsCompleted * 5).coerceIn(0, 15)

        val totalScore = (goalPoints + bedtimePoints + focusPoints).coerceIn(0, 100)

        val (ratingTitle, ratingColor) = when {
            totalScore >= 85 -> "Balanced Headspace" to SuccessEmerald
            totalScore >= 65 -> "Mindful Engagement" to ElectricBlue
            totalScore >= 45 -> "Moderate Screen Load" to WarningAmber
            else -> "High Attention Load" to VioletAccent
        }

        val formulaExplanation = """
            Formula: Score = Goal Adherence (70 pts) + Bedtime Protection (15 pts) + Active Focus Bonus (15 pts)
            
            • Goal Adherence ($goalPoints / 70 pts): Evaluated against your personal $goalMinutes-minute daily goal.
            • Bedtime Protection ($bedtimePoints / 15 pts): Rewards keeping bedtime schedule active to protect sleep hygiene.
            • Active Focus ($focusPoints / 15 pts): Rewards completing deliberate IRL focus blocks.
        """.trimIndent()

        val notes = listOf(
            ImpactNote(
                title = "Sleep & Melatonin",
                message = "Bright screen light late in the evening can delay melatonin release. Setting bedtime mode 30 minutes before sleep helps natural rest.",
                category = "Sleep Hygiene"
            ),
            ImpactNote(
                title = "Study & Deep Work",
                message = "Switching between apps frequently introduces attention residue. A single 30-minute block without checks accomplishes more than double that time interrupted.",
                category = "Cognitive Focus"
            ),
            ImpactNote(
                title = "Offline Reset",
                message = "Short walks outside, hydration, and tangible hobbies recharge visual and mental energy faster than passive scrolling.",
                category = "Real Life"
            )
        )

        return ImpactScoreResult(
            score = totalScore,
            ratingTitle = ratingTitle,
            ratingColor = ratingColor,
            goalPoints = goalPoints,
            bedtimePoints = bedtimePoints,
            focusPoints = focusPoints,
            formulaExplanation = formulaExplanation,
            practicalNotes = notes
        )
    }
}
