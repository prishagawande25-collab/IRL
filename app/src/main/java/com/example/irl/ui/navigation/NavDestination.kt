package com.example.irl.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    BLOCK("Block", Icons.Default.Block),
    RESET("Reset", Icons.Default.Spa),
    INSIGHTS("Insights", Icons.Default.BarChart),
    REWARDS("Rewards", Icons.Default.EmojiEvents)
}

sealed class Screen {
    data object Main : Screen()
    data object Privacy : Screen()
}
