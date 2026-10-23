package com.example.irl.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// IRL uses a serif/sans-serif editorial pairing:
// — Serif (FontFamily.Serif)  for display headings → editorial, warm, premium feel
// — SansSerif (FontFamily.SansSerif) for body text → clean, readable
//
// On Android, FontFamily.Serif resolves to Noto Serif / device serif.
// The contrast between serif headings and sans-serif body creates the
// Pinterest/lifestyle aesthetic without requiring downloadable fonts
// (which need INTERNET permission — forbidden in this app).

private val SerifDisplay = FontFamily.Serif
private val SansBody     = FontFamily.SansSerif

val Typography = Typography(
    // ─── Display — large hero headings ─────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily   = SerifDisplay,
        fontWeight   = FontWeight.Normal, // Serif looks best at Normal weight for elegance
        fontSize     = 40.sp,
        lineHeight   = 48.sp,
        letterSpacing = (-0.5).sp,
        color        = TextHeading
    ),
    displayMedium = TextStyle(
        fontFamily   = SerifDisplay,
        fontWeight   = FontWeight.Normal,
        fontSize     = 32.sp,
        lineHeight   = 40.sp,
        letterSpacing = (-0.25).sp,
        color        = TextHeading
    ),
    displaySmall = TextStyle(
        fontFamily   = SerifDisplay,
        fontWeight   = FontWeight.Normal,
        fontSize     = 26.sp,
        lineHeight   = 34.sp,
        letterSpacing = 0.sp,
        color        = TextHeading
    ),

    // ─── Headline — section headings ────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily   = SerifDisplay,
        fontWeight   = FontWeight.Normal,
        fontSize     = 22.sp,
        lineHeight   = 30.sp,
        letterSpacing = 0.sp,
        color        = TextHeading
    ),
    headlineMedium = TextStyle(
        fontFamily   = SerifDisplay,
        fontWeight   = FontWeight.Normal,
        fontSize     = 20.sp,
        lineHeight   = 28.sp,
        letterSpacing = 0.sp,
        color        = TextHeading
    ),
    headlineSmall = TextStyle(
        fontFamily   = SerifDisplay,
        fontWeight   = FontWeight.Normal,
        fontSize     = 18.sp,
        lineHeight   = 26.sp,
        letterSpacing = 0.sp,
        color        = TextHeading
    ),

    // ─── Title — card titles, tab titles ────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily   = SansBody,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 18.sp,
        lineHeight   = 26.sp,
        letterSpacing = 0.sp,
        color        = TextHeading
    ),
    titleMedium = TextStyle(
        fontFamily   = SansBody,
        fontWeight   = FontWeight.Medium,
        fontSize     = 15.sp,
        lineHeight   = 22.sp,
        letterSpacing = 0.15.sp,
        color        = TextHeading
    ),
    titleSmall = TextStyle(
        fontFamily   = SansBody,
        fontWeight   = FontWeight.Medium,
        fontSize     = 13.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.1.sp,
        color        = TextBody
    ),

    // ─── Body ────────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily   = SansBody,
        fontWeight   = FontWeight.Normal,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.15.sp,
        color        = TextBody
    ),
    bodyMedium = TextStyle(
        fontFamily   = SansBody,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.25.sp,
        color        = TextBody
    ),
    bodySmall = TextStyle(
        fontFamily   = SansBody,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 17.sp,
        letterSpacing = 0.4.sp,
        color        = TextSubtle
    ),

    // ─── Label ───────────────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily   = SansBody,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.1.sp,
        color        = TextHeading
    ),
    labelMedium = TextStyle(
        fontFamily   = SansBody,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.5.sp,
        color        = TextBody
    ),
    labelSmall = TextStyle(
        fontFamily   = SansBody,
        fontWeight   = FontWeight.Medium,
        fontSize     = 10.sp,
        lineHeight   = 14.sp,
        letterSpacing = 0.5.sp,
        color        = TextSubtle
    )
)