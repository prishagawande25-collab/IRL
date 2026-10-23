package com.example.irl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.irl.ui.block.BlockTabScreen
import com.example.irl.ui.home.HomeScreen
import com.example.irl.ui.insights.InsightsScreen
import com.example.irl.ui.navigation.MainTab
import com.example.irl.ui.navigation.Screen
import com.example.irl.ui.privacy.PrivacyScreen
import com.example.irl.ui.reset.ResetScreen
import com.example.irl.ui.rewards.RewardsScreen
import com.example.irl.ui.theme.DeepTeal
import com.example.irl.ui.theme.IRLTheme
import com.example.irl.ui.theme.MutedPlum
import com.example.irl.ui.theme.ParchmentCard
import com.example.irl.ui.theme.TextHeading
import com.example.irl.ui.theme.TextSubtle
import com.example.irl.ui.theme.WarmCream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IRLTheme {
                MainAppShell()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell() {
    var currentScreen by rememberSaveable { mutableStateOf<String>("MAIN") }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    if (currentScreen == "PRIVACY") {
        BackHandler {
            currentScreen = "MAIN"
        }
        PrivacyScreen(
            onNavigateBack = { currentScreen = "MAIN" }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "IRL",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextHeading,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    actions = {
                        IconButton(onClick = { currentScreen = "PRIVACY" }) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Privacy & Permissions",
                                tint = DeepTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = WarmCream
                    )
                )
            },
            bottomBar = {
                // Floating pill navigation bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmCream)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    NavigationBar(
                        containerColor = ParchmentCard,
                        contentColor = TextHeading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = DeepTeal.copy(alpha = 0.12f),
                                spotColor = DeepTeal.copy(alpha = 0.1f)
                            )
                    ) {
                        MainTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = WarmCream,
                                    unselectedIconColor = TextSubtle,
                                    selectedTextColor = DeepTeal,
                                    unselectedTextColor = TextSubtle,
                                    indicatorColor = DeepTeal
                                )
                            )
                        }
                    }
                }
            },
            containerColor = WarmCream
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(
                    targetState = selectedTab,
                    animationSpec = tween(durationMillis = 250),
                    label = "TabCrossfade"
                ) { tab ->
                    when (tab) {
                        MainTab.HOME -> HomeScreen(
                            onNavigateToTab = { target -> selectedTab = target },
                            onNavigateToPrivacy = { currentScreen = "PRIVACY" }
                        )
                        MainTab.BLOCK -> BlockTabScreen(
                            onNavigateToPrivacy = { currentScreen = "PRIVACY" }
                        )
                        MainTab.RESET -> ResetScreen()
                        MainTab.INSIGHTS -> InsightsScreen(
                            onNavigateToPrivacy = { currentScreen = "PRIVACY" }
                        )
                        MainTab.REWARDS -> RewardsScreen()
                    }
                }
            }
        }
    }
}