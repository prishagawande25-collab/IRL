package com.example.irl.ui.theme

import androidx.compose.ui.graphics.Color

// ─── IRL Nature Palette ────────────────────────────────────────────────────
// Warm, dreamy, Pinterest-inspired. Light base with rich nature accents.

val WarmCream       = Color(0xFFFFF6EB) // Primary background
val CreamDeep       = Color(0xFFF5E8D6) // Slightly deeper cream for cards
val ParchmentCard   = Color(0xFFFAEFDF) // Card surface

val Peach           = Color(0xFFF6B99C) // Warm accent
val Coral           = Color(0xFFF47F79) // CTA, danger accent
val ButterYellow    = Color(0xFFF5D987) // Warning, streaks
val DustyRose       = Color(0xFFD99BB2) // Secondary accent, bedtime
val Lavender        = Color(0xFFC9B9E8) // Soft accent for achievements
val SageGreen       = Color(0xFFA9C5A4) // Success, nature

val DeepTeal        = Color(0xFF245D59) // Primary action color
val DeepTealLight   = Color(0xFF3A7A75) // Lighter teal for hover
val MutedPlum       = Color(0xFF665078) // Secondary dark, nav selected
val MidnightNavy    = Color(0xFF252743) // Darkest text, headings

// ─── Semantic Text ──────────────────────────────────────────────────────────
val TextHeading     = Color(0xFF252743) // MidnightNavy — for display/title text
val TextBody        = Color(0xFF4A4568) // Slightly lighter for body text
val TextSubtle      = Color(0xFF7A7590) // Muted text, labels
val TextHint        = Color(0xFFAA9FB8) // Very light hint text

// ─── Semantic Surfaces ───────────────────────────────────────────────────────
val SurfaceCard     = Color(0xFFFAEFDF) // Card backgrounds (parchment)
val SurfaceElevated = Color(0xFFF0E2CC) // Elevated card / input fields
val SurfaceDark     = Color(0xFF1E3D3A) // Dark section (teal-dark)
val SurfacePlum     = Color(0xFF3D2B48) // Dark plum section

// ─── Semantic States ─────────────────────────────────────────────────────────
val SuccessNature   = SageGreen
val WarningNature   = ButterYellow
val DangerNature    = Coral
val BorderLight     = Color(0xFFE8D8C6) // Subtle warm border

// ─── Legacy aliases (kept to ease migration — do not use in new code) ─────────
@Deprecated("Use DeepTeal") val ElectricBlue       = DeepTeal
@Deprecated("Use MutedPlum") val VioletAccent      = MutedPlum
@Deprecated("Use WarmCream") val NavyBackground    = WarmCream
@Deprecated("Use ParchmentCard") val NavySurface   = ParchmentCard
@Deprecated("Use SurfaceElevated") val NavySurfaceElevated = SurfaceElevated
@Deprecated("Use BorderLight") val NavyCardStroke  = BorderLight
@Deprecated("Use SuccessNature") val SuccessEmerald = SuccessNature
@Deprecated("Use WarningNature") val WarningAmber  = WarningNature
@Deprecated("Use DangerNature") val DangerCrimson  = DangerNature
@Deprecated("Use TextHeading") val TextPrimary     = TextHeading
@Deprecated("Use TextBody") val TextSecondary      = TextBody
@Deprecated("Use TextSubtle") val TextMuted        = TextSubtle