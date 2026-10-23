package com.example.irl.ui.reset

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.irl.ui.games.MemoryMatchGame
import com.example.irl.ui.games.NumberRushGame
import com.example.irl.ui.games.ReactionTestGame
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

private enum class ActiveGame { NONE, NUMBER_RUSH, MEMORY_MATCH, REACTION_TEST }

@Composable
fun ResetScreen() {
    var activeGame by remember { mutableStateOf(ActiveGame.NONE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmCream)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Header ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Step Away",
                style = MaterialTheme.typography.displaySmall,
                color = TextHeading
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Breathe, play, and reconnect with real life.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextBody
            )
        }

        // ── Active Game (full-width) ─────────────────────────────────────────
        AnimatedVisibility(
            visible = activeGame == ActiveGame.NUMBER_RUSH,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                NumberRushGame(onDismiss = { activeGame = ActiveGame.NONE })
            }
        }
        AnimatedVisibility(
            visible = activeGame == ActiveGame.MEMORY_MATCH,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                MemoryMatchGame(onDismiss = { activeGame = ActiveGame.NONE })
            }
        }
        AnimatedVisibility(
            visible = activeGame == ActiveGame.REACTION_TEST,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                ReactionTestGame(onDismiss = { activeGame = ActiveGame.NONE })
            }
        }

        // ── Games Section ────────────────────────────────────────────────────
        if (activeGame == ActiveGame.NONE) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                SectionLabel("Focus micro-games")
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Train attention, earn points, no internet needed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSubtle
                )
                Spacer(modifier = Modifier.height(16.dp))

                GameCard(
                    title = "Number Rush",
                    description = "Tap 1–25 in order as fast as you can.",
                    tag = "Speed & attention",
                    gradientColors = listOf(DeepTeal, DeepTeal.copy(alpha = 0.75f)),
                    accentColor = SageGreen,
                    isActive = false,
                    onPlay = { activeGame = ActiveGame.NUMBER_RUSH }
                )
                Spacer(modifier = Modifier.height(12.dp))
                GameCard(
                    title = "Memory Match",
                    description = "Flip cards and find all matching pairs with fewest moves.",
                    tag = "Working memory",
                    gradientColors = listOf(MutedPlum, MutedPlum.copy(alpha = 0.75f)),
                    accentColor = Lavender,
                    isActive = false,
                    onPlay = { activeGame = ActiveGame.MEMORY_MATCH }
                )
                Spacer(modifier = Modifier.height(12.dp))
                GameCard(
                    title = "Reaction Test",
                    description = "Wait for the signal, tap fast. False starts are penalised.",
                    tag = "Reflex timing",
                    gradientColors = listOf(Coral.copy(alpha = 0.8f), Peach.copy(alpha = 0.7f)),
                    accentColor = ButterYellow,
                    isActive = false,
                    onPlay = { activeGame = ActiveGame.REACTION_TEST }
                )

                Spacer(modifier = Modifier.height(36.dp))

                // ── Mindful Breaks ────────────────────────────────────────────
                SectionLabel("Guided mindful breaks")
                Spacer(modifier = Modifier.height(14.dp))

                OfflineActivityRow(
                    title = "4-7-8 Breathing",
                    description = "Inhale 4s · Hold 7s · Exhale 8s · 3 rounds. Calms your nervous system in under 2 minutes.",
                    accentColor = Lavender,
                    icon = Icons.Default.SelfImprovement
                )
                Spacer(modifier = Modifier.height(12.dp))
                OfflineActivityRow(
                    title = "20-20-20 Eye Rest",
                    description = "Look at something 20 feet away for 20 seconds. Relaxes eye muscles strained by screens.",
                    accentColor = SageGreen,
                    icon = Icons.Default.Spa
                )

                Spacer(modifier = Modifier.height(36.dp))

                // ── Real Life Ideas ──────────────────────────────────────────
                SectionLabel("Things to do in real life")
                Spacer(modifier = Modifier.height(14.dp))

                RealLifeCard(
                    title = "Water & sunshine",
                    description = "A tall glass of water and 5 minutes outside without checking notifications.",
                    colorStrip = SageGreen
                )
                Spacer(modifier = Modifier.height(10.dp))
                RealLifeCard(
                    title = "Read something physical",
                    description = "Open a book, magazine, or even a comic. Even 10 pages resets your focus.",
                    colorStrip = DustyRose
                )
                Spacer(modifier = Modifier.height(10.dp))
                RealLifeCard(
                    title = "Sketch or doodle",
                    description = "Pick up any pen and just draw. No talent required — just the act itself.",
                    colorStrip = ButterYellow
                )
                Spacer(modifier = Modifier.height(10.dp))
                RealLifeCard(
                    title = "Message a friend",
                    description = "A voice note, a hand-written note, or just knocking on a door.",
                    colorStrip = Peach
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
        color = DeepTeal
    )
}

@Composable
private fun GameCard(
    title: String,
    description: String,
    tag: String,
    gradientColors: List<Color>,
    accentColor: Color,
    isActive: Boolean,
    onPlay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(gradientColors))
            .clickable { onPlay() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = WarmCream
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmCream.copy(alpha = 0.75f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onPlay,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmCream.copy(alpha = 0.18f),
                    contentColor = WarmCream
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun OfflineActivityRow(
    title: String,
    description: String,
    accentColor: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ParchmentCard)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextHeading,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextBody
            )
        }
    }
}

@Composable
private fun RealLifeCard(
    title: String,
    description: String,
    colorStrip: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ParchmentCard)
    ) {
        // Left color accent strip
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(72.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                .background(colorStrip)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextHeading,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextBody
            )
        }
    }
}
