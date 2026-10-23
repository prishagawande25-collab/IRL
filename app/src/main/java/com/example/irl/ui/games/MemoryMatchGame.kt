package com.example.irl.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.irl.ui.theme.ElectricBlue
import com.example.irl.ui.theme.NavyBackground
import com.example.irl.ui.theme.NavyCardStroke
import com.example.irl.ui.theme.NavySurface
import com.example.irl.ui.theme.NavySurfaceElevated
import com.example.irl.ui.theme.SuccessEmerald
import com.example.irl.ui.theme.TextMuted
import com.example.irl.ui.theme.TextPrimary
import com.example.irl.ui.theme.TextSecondary
import com.example.irl.ui.theme.VioletAccent
import com.example.irl.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val EMOJI_POOL = listOf("⚡", "🌟", "🎯", "🌿", "💎", "💡", "🚀", "🎨")

@Composable
fun MemoryMatchGame(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { AppPreferencesRepository.getInstance(context) }
    val bestMoves by repo.bestMemoryMatchMovesFlow.collectAsState(initial = 0)

    var cards by remember { mutableStateOf((EMOJI_POOL + EMOJI_POOL).shuffled()) }
    var flippedIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var matchedIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var movesCount by remember { mutableIntStateOf(0) }
    var isCheckingMatch by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var isNewRecord by remember { mutableStateOf(false) }
    var pointsEarned by remember { mutableIntStateOf(0) }

    fun restartGame() {
        cards = (EMOJI_POOL + EMOJI_POOL).shuffled()
        flippedIndices = emptyList()
        matchedIndices = emptySet()
        movesCount = 0
        isCheckingMatch = false
        isFinished = false
        isNewRecord = false
        pointsEarned = 0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VioletAccent.copy(alpha = 0.5f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Memory Match",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Find all 8 matching pairs with minimal flips",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                if (bestMoves > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Best: $bestMoves moves",
                            style = MaterialTheme.typography.labelMedium,
                            color = WarningAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score Tracker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavySurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Moves: $movesCount",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Matched: ${matchedIndices.size / 2} / 8",
                    style = MaterialTheme.typography.labelMedium,
                    color = VioletAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isFinished) {
                // 4x4 Cards Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0 until 4) {
                                val index = row * 4 + col
                                val isMatched = matchedIndices.contains(index)
                                val isFlipped = flippedIndices.contains(index) || isMatched

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isMatched -> SuccessEmerald.copy(alpha = 0.2f)
                                                isFlipped -> VioletAccent.copy(alpha = 0.25f)
                                                else -> NavySurfaceElevated
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            when {
                                                isMatched -> SuccessEmerald
                                                isFlipped -> VioletAccent
                                                else -> NavyCardStroke
                                            },
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable(enabled = !isFlipped && !isCheckingMatch) {
                                            if (flippedIndices.size < 2) {
                                                val nextFlipped = flippedIndices + index
                                                flippedIndices = nextFlipped

                                                if (nextFlipped.size == 2) {
                                                    movesCount++
                                                    isCheckingMatch = true
                                                    val first = nextFlipped[0]
                                                    val second = nextFlipped[1]

                                                    if (cards[first] == cards[second]) {
                                                        matchedIndices = matchedIndices + first + second
                                                        flippedIndices = emptyList()
                                                        isCheckingMatch = false

                                                        if (matchedIndices.size == 16) {
                                                            isFinished = true
                                                            val pts = 60
                                                            pointsEarned = pts
                                                            scope.launch {
                                                                isNewRecord = repo.saveMemoryMatchScore(movesCount)
                                                                repo.addPoints(pts)
                                                                if (movesCount <= 16) {
                                                                    repo.unlockAchievement("ach_memory_champ")
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        scope.launch {
                                                            delay(750L)
                                                            flippedIndices = emptyList()
                                                            isCheckingMatch = false
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isFlipped) {
                                        Text(
                                            text = cards[index],
                                            fontSize = 24.sp
                                        )
                                    } else {
                                        Text(
                                            text = "?",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextMuted,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Victory Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Memory Master!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SuccessEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Matched all pairs in $movesCount moves",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )

                    if (isNewRecord) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "New Best Score!",
                            style = MaterialTheme.typography.labelMedium,
                            color = WarningAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+$pointsEarned Wellbeing Points Earned",
                        style = MaterialTheme.typography.titleSmall,
                        color = VioletAccent,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { restartGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletAccent, contentColor = TextPrimary)
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
