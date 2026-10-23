package com.example.irl.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val IRLLightColorScheme = lightColorScheme(
    // Primary — deep teal for all primary actions
    primary                = DeepTeal,
    onPrimary              = WarmCream,
    primaryContainer       = SageGreen.copy(alpha = 0.3f),
    onPrimaryContainer     = DeepTeal,

    // Secondary — muted plum for secondary actions & bedtime
    secondary              = MutedPlum,
    onSecondary            = WarmCream,
    secondaryContainer     = Lavender.copy(alpha = 0.35f),
    onSecondaryContainer   = MutedPlum,

    // Tertiary — coral for highlights and important CTAs
    tertiary               = Coral,
    onTertiary             = WarmCream,
    tertiaryContainer      = Peach.copy(alpha = 0.4f),
    onTertiaryContainer    = Coral,

    // Background & Surface — warm cream base
    background             = WarmCream,
    onBackground           = TextHeading,
    surface                = ParchmentCard,
    onSurface              = TextHeading,
    surfaceVariant         = SurfaceElevated,
    onSurfaceVariant       = TextBody,

    // Outline
    outline                = BorderLight,
    outlineVariant         = BorderLight.copy(alpha = 0.5f),

    // Error
    error                  = Coral,
    onError                = WarmCream,
    errorContainer         = Peach.copy(alpha = 0.3f),
    onErrorContainer       = Coral
)

@Composable
fun IRLTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = IRLLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = WarmCream.toArgb()
                window.navigationBarColor = WarmCream.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}