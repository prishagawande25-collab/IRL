package com.example.irl.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.irl.analytics.ImpactScoreResult
import com.example.irl.ui.theme.ElectricBlue
import com.example.irl.ui.theme.NavyBackground
import com.example.irl.ui.theme.NavyCardStroke
import com.example.irl.ui.theme.NavySurface
import com.example.irl.ui.theme.NavySurfaceElevated
import com.example.irl.ui.theme.TextMuted
import com.example.irl.ui.theme.TextPrimary
import com.example.irl.ui.theme.TextSecondary
import com.example.irl.ui.theme.VioletAccent

@Composable
fun ImpactMeterCard(
    result: ImpactScoreResult,
    modifier: Modifier = Modifier
) {
    var showFormulaDialog by remember { mutableStateOf(false) }

    val animatedScoreProgress by animateFloatAsState(
        targetValue = result.score / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "ImpactScoreAnim"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(result.ratingColor.copy(alpha = 0.5f)))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DIGITAL IMPACT METER",
                        style = MaterialTheme.typography.labelSmall,
                        color = result.ratingColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { showFormulaDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Explain Formula",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score Dial & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Gauge
                Box(
                    modifier = Modifier.size(84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(84.dp),
                        color = NavySurfaceElevated,
                        strokeWidth = 8.dp
                    )
                    CircularProgressIndicator(
                        progress = { animatedScoreProgress },
                        modifier = Modifier.size(84.dp),
                        color = result.ratingColor,
                        strokeWidth = 8.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result.score}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "/ 100",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Rating & Breakdown Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.ratingTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Goal Adherence: ${result.goalPoints}/70 pts\nSleep & Focus Protection: ${result.bedtimePoints + result.focusPoints}/30 pts",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Practical Notes List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                result.practicalNotes.forEach { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = VioletAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = note.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Formula Transparency Dialog
    if (showFormulaDialog) {
        AlertDialog(
            onDismissRequest = { showFormulaDialog = false },
            title = {
                Text(
                    text = "How Your Score is Calculated",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "The Digital Impact Meter is an objective, explainable formula based strictly on your own goals. We make zero medical diagnoses and invent no hidden statistics.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = result.formulaExplanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Everything is calculated on-device. Your goals can be adjusted at any time in the Insights tab.",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFormulaDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = NavyBackground)
                ) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
