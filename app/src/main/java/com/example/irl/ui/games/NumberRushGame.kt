package com.example.irl.ui.games
import androidx.compose.material3.TextButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.irl.data.AppPreferencesRepository
import com.example.irl.ui.theme.DangerCrimson
import com.example.irl.ui.theme.ElectricBlue
import com.example.irl.ui.theme.NavyBackground
import com.example.irl.ui.theme.NavyCardStroke
import com.example.irl.ui.theme.NavySurface
import com.example.irl.ui.theme.NavySurfaceElevated
import com.example.irl.ui.theme.SuccessEmerald
import com.example.irl.ui.theme.TextMuted
import com.example.irl.ui.theme.TextPrimary
import com.example.irl.ui.theme.TextSecondary
import com.example.irl.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun NumberRushGame(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AppPreferencesRepository.getInstance(context) }
    val bestScoreMs by repo.bestNumberRushMsFlow.collectAsState(initial = 0L)

    var numbers by remember { mutableStateOf((1..25).toList().shuffled()) }
    var nextExpected by remember { mutableIntStateOf(1) }
    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var startTimeMs by remember { mutableLongStateOf(0L) }
    var elapsedTimeMs by remember { mutableLongStateOf(0L) }
    var isNewRecord by remember { mutableStateOf(false) }
    var pointsEarned by remember { mutableIntStateOf(0) }
    var errorIndex by remember { mutableStateOf<Int?>(null) }

    fun restartGame() {
        numbers = (1..25).toList().shuffled()
        nextExpected = 1
        isRunning = false
        isFinished = false
        startTimeMs = 0L
        elapsedTimeMs = 0L
        isNewRecord = false
        pointsEarned = 0
        errorIndex = null
    }

    LaunchedEffect(isRunning) {
        while (isRunning && !isFinished) {
            elapsedTimeMs = System.currentTimeMillis() - startTimeMs
            delay(50L)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ElectricBlue.copy(alpha = 0.5f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Title & High Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Number Rush",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap numbers 1 through 25 in order",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                if (bestScoreMs > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "Best: %.1fs", bestScoreMs / 1000f),
                            style = MaterialTheme.typography.labelMedium,
                            color = WarningAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Timer & Next Target Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavySurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format(Locale.US, "%.1fs", elapsedTimeMs / 1000f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFinished) SuccessEmerald else ElectricBlue)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isFinished) "Complete!" else "Next: $nextExpected",
                        style = MaterialTheme.typography.labelMedium,
                        color = NavyBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isFinished) {
                // 5x5 Number Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (row in 0 until 5) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (col in 0 until 5) {
                                val index = row * 5 + col
                                val num = numbers[index]
                                val isTapped = num < nextExpected
                                val isError = errorIndex == index

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isTapped -> SuccessEmerald.copy(alpha = 0.2f)
                                                isError -> DangerCrimson.copy(alpha = 0.4f)
                                                else -> NavySurfaceElevated
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            when {
                                                isTapped -> SuccessEmerald.copy(alpha = 0.5f)
                                                isError -> DangerCrimson
                                                else -> NavyCardStroke
                                            },
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable(enabled = !isTapped && !isFinished) {
                                            if (!isRunning) {
                                                isRunning = true
                                                startTimeMs = System.currentTimeMillis()
                                            }

                                            if (num == nextExpected) {
                                                errorIndex = null
                                                if (nextExpected == 25) {
                                                    // Finished!
                                                    isRunning = false
                                                    isFinished = true
                                                    val finalTime = System.currentTimeMillis() - startTimeMs
                                                    elapsedTimeMs = finalTime

                                                    val basePts = 50
                                                    val speedBonus = if (finalTime < 40_000L) 25 else 0
                                                    val totalPts = basePts + speedBonus
                                                    pointsEarned = totalPts

                                                    scope.launch {
                                                        isNewRecord = repo.saveNumberRushScore(finalTime)
                                                        repo.addPoints(totalPts)
                                                        if (finalTime < 40_000L) {
                                                            repo.unlockAchievement("ach_number_rush_speed")
                                                        }
                                                    }
                                                } else {
                                                    nextExpected++
                                                }
                                            } else {
                                                errorIndex = index
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isTapped) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SuccessEmerald, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(
                                            text = "$num",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Victory Screen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Great Reflexes!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SuccessEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format(Locale.US, "Completed in %.2f seconds", elapsedTimeMs / 1000f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )

                    if (isNewRecord) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "New Personal Best!",
                            style = MaterialTheme.typography.labelMedium,
                            color = WarningAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+$pointsEarned Wellbeing Points Earned",
                        style = MaterialTheme.typography.titleSmall,
                        color = ElectricBlue,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { restartGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = NavyBackground)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Again", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { restartGame() },
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(NavyCardStroke))
                ) {
                    Text("Restart", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }

                TextButton(onClick = onDismiss) {
                    Text("Close", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
