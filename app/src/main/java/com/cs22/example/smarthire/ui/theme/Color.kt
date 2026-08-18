package com.cs22.example.smarthire.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// SmartHire Stitch Design System – Color Tokens
// Sourced from F:\stitch_smart_hire_recruitment_app HTML mockups
// Primary brand: Cobalt Royal Blue | Background: Soft Blue-White Canvas
// ─────────────────────────────────────────────────────────────────────────────

// Primary Brand
val Primary            = Color(0xFF0057C0)
val PrimaryVariant     = Color(0xFF2F75E8)
val PrimaryContainer   = Color(0xFF2870E3)
val OnPrimary          = Color(0xFFFFFFFF)
val OnPrimaryContainer = Color(0xFFFEFCFF)
val PrimaryFixed       = Color(0xFFD8E2FF)
val PrimaryFixedDim    = Color(0xFFAEC6FF)
val InversePrimary     = Color(0xFFAEC6FF)

// Stitch Light Design Tokens
val StBackground          = Color(0xFFF3F7FF)
val StSurface             = Color(0xFFFFFFFF)
val StSurfaceContainer    = Color(0xFFECEDF7)
val StSurfaceContainerLow = Color(0xFFF2F3FD)
val StPrimary             = Color(0xFF0057C0)
val StOnSurface           = Color(0xFF191B22)
val StOnSurfaceVariant    = Color(0xFF424753)
val StTextSecondary       = Color(0xFF68738A)
val StOutlineVariant      = Color(0xFFDCE5F3)
val StMatchBadgeBg        = Color(0xFFEAF2FF)
val StSuccess             = Color(0xFF20B26B)
val StWarning             = Color(0xFFF5A623)
val StError               = Color(0xFFBA1A1A)

// Secondary
val Secondary              = Color(0xFF1658C6)
val SecondaryContainer     = Color(0xFF5D8FFF)
val OnSecondary            = Color(0xFFFFFFFF)
val OnSecondaryContainer   = Color(0xFF002868)
val SecondaryFixed         = Color(0xFFDAE2FF)
val SecondaryFixedDim      = Color(0xFFB1C5FF)

// Tertiary
val Tertiary              = Color(0xFF964400)
val TertiaryContainer     = Color(0xFFBC5700)
val OnTertiary            = Color(0xFFFFFFFF)
val OnTertiaryContainer   = Color(0xFFFFFBFF)
val TertiaryFixed         = Color(0xFFFFDBC9)
val TertiaryFixedDim      = Color(0xFFFFB68C)

// Backgrounds & Surfaces (Light)
val Background              = Color(0xFFF3F7FF)
val Surface                 = Color(0xFFFAF9FF)
val SurfaceBright           = Color(0xFFFAF9FF)
val SurfaceContainerLowest  = Color(0xFFFFFFFF)
val SurfaceContainerLow     = Color(0xFFF2F3FD)
val SurfaceContainer        = Color(0xFFECEDF7)
val SurfaceContainerHigh    = Color(0xFFE7E7F2)
val SurfaceContainerHighest = Color(0xFFE1E2EC)
val SurfaceVariant          = Color(0xFFE1E2EC)
val SurfaceDim              = Color(0xFFD8D9E3)
val SurfaceTint             = Color(0xFF0059C5)

// Text on Surfaces (Light)
val OnBackground       = Color(0xFF191B22)
val OnSurface          = Color(0xFF191B22)
val OnSurfaceVariant   = Color(0xFF424753)
val TextSecondary      = Color(0xFF68738A)

// Outlines & Borders
val Outline            = Color(0xFF727785)
val OutlineVariant     = Color(0xFFDCE5F3)
val OutlineVariantAlt  = Color(0xFFC2C6D6)

// Match / Accent badges
val MatchBadgeBg       = Color(0xFFEAF2FF)
val MatchBadgeText     = Color(0xFF0057C0)

// Semantic State Colors
val Success            = Color(0xFF20B26B)
val SuccessContainer   = Color(0xFFD4F5E5)
val Warning            = Color(0xFFF5A623)
val WarningContainer   = Color(0xFFFFF3DC)
val Error              = Color(0xFFBA1A1A)
val ErrorContainer     = Color(0xFFFFDAD6)
val OnError            = Color(0xFFFFFFFF)
val OnErrorContainer   = Color(0xFF93000A)

// Inverse
val InverseSurface    = Color(0xFF2E3038)
val InverseOnSurface  = Color(0xFFEFF0FA)

// ─────────────────────────────────────────────────────────────────────────────
// Dark Theme Surfaces
// ─────────────────────────────────────────────────────────────────────────────
val DarkBackground              = Color(0xFF0D1117)
val DarkSurface                 = Color(0xFF161B22)
val DarkSurfaceContainerLowest  = Color(0xFF0D1117)
val DarkSurfaceContainerLow     = Color(0xFF161B22)
val DarkSurfaceContainer        = Color(0xFF1C2130)
val DarkSurfaceContainerHigh    = Color(0xFF21262D)
val DarkSurfaceContainerHighest = Color(0xFF30363D)
val DarkSurfaceVariant          = Color(0xFF1E2432)
val DarkOnSurface               = Color(0xFFE6EDF3)
val DarkOnSurfaceVariant        = Color(0xFF8B949E)
val DarkOutline                 = Color(0xFF30363D)
val DarkOutlineVariant          = Color(0xFF21262D)

// ─────────────────────────────────────────────────────────────────────────────
// Alias tokens kept for backward-compat with existing screens that referenced old names
// ─────────────────────────────────────────────────────────────────────────────
val SmartHirePrimary          = Primary
val SmartHireSecondary        = Secondary
val SmartHireBackground       = Background
val SmartHireSurface          = SurfaceContainerLowest
val SmartHireSurfaceContainer = SurfaceContainer
val SmartHireOnSurface        = OnSurface
val SmartHireOnSurfaceVariant = OnSurfaceVariant
val SmartHireOutline          = Outline
val SmartHireError            = Error
val SmartHireSuccess          = Success
val PremiumBg                 = DarkBackground
val PremiumSurface            = DarkSurface
val PremiumPrimary            = Primary

// Legacy colors kept for compatibility
val BackgroundLight  = Background
val SurfaceLight     = SurfaceContainerLowest
val PrimaryAccent    = Color(0xFF1D4ED8)
val SuccessEmerald   = Success
val DarkText         = OnSurface
val SlateText        = OnSurfaceVariant
val LightOutline     = OutlineVariant
