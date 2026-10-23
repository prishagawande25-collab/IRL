package com.example.irl.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.irl.data.AppPreferencesRepository
import com.example.irl.ui.theme.BorderLight
import com.example.irl.ui.theme.ButterYellow
import com.example.irl.ui.theme.Coral
import com.example.irl.ui.theme.DeepTeal
import com.example.irl.ui.theme.DustyRose
import com.example.irl.ui.theme.Lavender
import com.example.irl.ui.theme.MutedPlum
import com.example.irl.ui.theme.ParchmentCard
import com.example.irl.ui.theme.Peach
import com.example.irl.ui.theme.SageGreen
import com.example.irl.ui.theme.SurfaceElevated
import com.example.irl.ui.theme.TextBody
import com.example.irl.ui.theme.TextHeading
import com.example.irl.ui.theme.TextSubtle
import com.example.irl.ui.theme.WarmCream
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RewardItem(
    val id: String,
    val title: String,
    val description: String,
    val costPoints: Int,
    val isCustom: Boolean = false
)

data class AchievementItem(
    val id: String,
    val title: String,
    val requirement: String,
    val points: Int,
    val iconName: String
)

private val DEFAULT_CATALOGUE = listOf(
    RewardItem("def_1", "1-Hour Offline Hobby Time", "Enjoy sketching, reading, or playing music guilt-free.", 100),
    RewardItem("def_2", "Favourite Coffee / Bubble Tea", "A personal treat on your next outing.", 150),
    RewardItem("def_3", "Sleep In 30 Minutes", "Grant yourself extra rest on Saturday morning.", 200),
    RewardItem("def_4", "Movie or Game Night with Friends", "Dedicated uninterrupted social time.", 250),
    RewardItem("def_5", "Buy a New Book or Sketchpad", "Invest in an offline creative medium.", 300)
)

private val ACHIEVEMENTS_LIST = listOf(
    AchievementItem("ach_first_session", "First Focus", "Complete your first focus session", 25, "Star"),
    AchievementItem("ach_reaction_fast", "Lightning Reflexes", "Score under 300ms in Reaction Test", 50, "Speed"),
    AchievementItem("ach_number_rush_speed", "Number Speedster", "Complete Number Rush in under 40s", 50, "Timer"),
    AchievementItem("ach_memory_champ", "Memory Champ", "Solve Memory Match in 16 moves or fewer", 50, "SportsEsports"),
    AchievementItem("ach_century_points", "Centurion", "Earn 100+ wellbeing points", 50, "EmojiEvents")
)

@Composable
fun RewardsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AppPreferencesRepository.getInstance(context) }

    val points by repo.userPointsFlow.collectAsState(initial = 0)
    val streak by repo.focusStreakFlow.collectAsState(initial = 0)
    val unlockedAchievements by repo.unlockedAchievementsFlow.collectAsState(initial = emptySet())
    val customRewardsEncoded by repo.customRewardsFlow.collectAsState(initial = emptySet())
    val redemptionsHistoryEncoded by repo.redemptionsHistoryFlow.collectAsState(initial = emptySet())

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var selectedRewardToRedeem by remember { mutableStateOf<RewardItem?>(null) }

    val (levelTitle, nextLevelPts, currentLevelBase) = when {
        points < 150 -> Triple("Mindful Starter", 150, 0)
        points < 400 -> Triple("Focus Apprentice", 400, 150)
        points < 800 -> Triple("Digital Naturalist", 800, 400)
        points < 1500 -> Triple("Attention Master", 1500, 800)
        else -> Triple("IRL Legend", 2500, 1500)
    }

    val levelProgress = if (nextLevelPts > currentLevelBase) {
        ((points - currentLevelBase).toFloat() / (nextLevelPts - currentLevelBase)).coerceIn(0f, 1f)
    } else 1f

    val customRewards = remember(customRewardsEncoded) {
        customRewardsEncoded.mapNotNull { encoded ->
            val parts = encoded.split("|#|")
            if (parts.size >= 3) {
                RewardItem(
                    id = parts[0],
                    title = parts[1],
                    description = "Personal real-life reward",
                    costPoints = parts[2].toIntOrNull() ?: 100,
                    isCustom = true
                )
            } else null
        }
    }

    val allRewards = DEFAULT_CATALOGUE + customRewards

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Hero — Points & Level ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(DeepTeal, MutedPlum.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 28.dp, vertical = 28.dp)
        ) {
            Column {
                Text(
                    text = "Your Progress",
                    style = MaterialTheme.typography.headlineMedium.copy(fontStyle = FontStyle.Italic),
                    color = WarmCream
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Points + streak in one row
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$points",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = WarmCream,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "pts",
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmCream.copy(alpha = 0.65f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (streak > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = ButterYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$streak day streak",
                                style = MaterialTheme.typography.labelLarge,
                                color = ButterYellow
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Level label and progress bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = levelTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = WarmCream.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "$points / $nextLevelPts",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmCream.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { levelProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ButterYellow,
                    trackColor = WarmCream.copy(alpha = 0.15f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {

            // ── Honesty note (minimal, no card) ─────────────────────────────
            Text(
                text = "Rewards are personal real-life treats you choose for yourself. IRL never issues money, gift cards, or external payouts.",
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = TextSubtle
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Achievements ─────────────────────────────────────────────────
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                color = DeepTeal
            )
            Spacer(modifier = Modifier.height(14.dp))

            ACHIEVEMENTS_LIST.forEach { ach ->
                val isUnlocked = unlockedAchievements.contains(ach.id) ||
                        (ach.id == "ach_century_points" && points >= 100)
                AchievementRow(ach = ach, isUnlocked = isUnlocked)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Reward Catalogue ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reward catalogue",
                    style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                    color = DeepTeal
                )
                IconButton(
                    onClick = { showAddCustomDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DeepTeal.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add custom reward",
                        tint = DeepTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Redeem focus points for real-world treats.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSubtle
            )
            Spacer(modifier = Modifier.height(14.dp))

            allRewards.forEach { reward ->
                val canAfford = points >= reward.costPoints
                RewardRow(
                    reward = reward,
                    canAfford = canAfford,
                    onRedeem = { selectedRewardToRedeem = reward },
                    onDelete = if (reward.isCustom) {
                        {
                            val encoded = "${reward.id}|#|${reward.title}|#|${reward.costPoints}"
                            scope.launch { repo.removeCustomReward(encoded) }
                        }
                    } else null
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Redemption History ────────────────────────────────────────────
            if (redemptionsHistoryEncoded.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Redemption history",
                    style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                    color = DeepTeal
                )
                Spacer(modifier = Modifier.height(12.dp))

                val dateFormat = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())
                redemptionsHistoryEncoded.reversed().forEach { entry ->
                    val parts = entry.split("|#|")
                    if (parts.size >= 3) {
                        val dateMs = parts[0].toLongOrNull() ?: 0L
                        val title = parts[1]
                        val cost = parts[2]

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextHeading
                                )
                                Text(
                                    text = dateFormat.format(Date(dateMs)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSubtle
                                )
                            }
                            Text(
                                text = "−$cost pts",
                                style = MaterialTheme.typography.labelMedium,
                                color = SageGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // ── Add Custom Reward Dialog ───────────────────────────────────────────
    if (showAddCustomDialog) {
        var customTitle by remember { mutableStateOf("") }
        var customCostStr by remember { mutableStateOf("100") }

        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = {
                Text(
                    text = "Create a reward",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextHeading
                )
            },
            text = {
                Column {
                    Text(
                        text = "Define a personal treat or privilege you already have access to:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextBody
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        label = { Text("Reward title (e.g. 1-hr gaming)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ParchmentCard,
                            unfocusedContainerColor = ParchmentCard,
                            focusedBorderColor = DeepTeal,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextHeading,
                            unfocusedTextColor = TextHeading
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customCostStr,
                        onValueChange = { customCostStr = it },
                        label = { Text("Points cost (e.g. 150)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ParchmentCard,
                            unfocusedContainerColor = ParchmentCard,
                            focusedBorderColor = DeepTeal,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextHeading,
                            unfocusedTextColor = TextHeading
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cost = customCostStr.toIntOrNull() ?: 100
                        if (customTitle.isNotBlank()) {
                            val id = "cust_${System.currentTimeMillis()}"
                            val encoded = "$id|#|$customTitle|#|$cost"
                            scope.launch { repo.addCustomReward(encoded) }
                            showAddCustomDialog = false
                        }
                    },
                    enabled = customTitle.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal, contentColor = WarmCream)
                ) {
                    Text("Add Reward", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("Cancel", color = TextSubtle)
                }
            },
            containerColor = WarmCream,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Redeem Confirmation Dialog ─────────────────────────────────────────
    selectedRewardToRedeem?.let { reward ->
        AlertDialog(
            onDismissRequest = { selectedRewardToRedeem = null },
            title = {
                Text(
                    text = "Redeem reward?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextHeading
                )
            },
            text = {
                Column {
                    Text(
                        text = "\"${reward.title}\" for ${reward.costPoints} points?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextBody
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This will deduct ${reward.costPoints} pts from your balance and record it in your history.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSubtle
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch { repo.redeemReward(reward.title, reward.costPoints) }
                        selectedRewardToRedeem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen, contentColor = WarmCream)
                ) {
                    Text("Confirm", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRewardToRedeem = null }) {
                    Text("Cancel", color = TextSubtle)
                }
            },
            containerColor = WarmCream,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun AchievementRow(ach: AchievementItem, isUnlocked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isUnlocked) SageGreen.copy(alpha = 0.1f) else ParchmentCard)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
            contentDescription = null,
            tint = if (isUnlocked) SageGreen else TextSubtle,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ach.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isUnlocked) TextHeading else TextBody,
                fontWeight = if (isUnlocked) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = ach.requirement,
                style = MaterialTheme.typography.bodySmall,
                color = TextSubtle
            )
        }
        Text(
            text = if (isUnlocked) "Done" else "+${ach.points}",
            style = MaterialTheme.typography.labelSmall,
            color = if (isUnlocked) SageGreen else TextSubtle,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RewardRow(
    reward: RewardItem,
    canAfford: Boolean,
    onRedeem: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ParchmentCard)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reward.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextHeading,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = reward.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextBody
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${reward.costPoints} pts",
                style = MaterialTheme.typography.labelSmall,
                color = if (canAfford) SageGreen else TextSubtle,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (onDelete != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextSubtle,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
        Button(
            onClick = onRedeem,
            enabled = canAfford,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepTeal,
                contentColor = WarmCream,
                disabledContainerColor = SurfaceElevated,
                disabledContentColor = TextSubtle
            )
        ) {
            Text(text = "Redeem", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}
