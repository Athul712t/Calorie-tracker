package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design tokens taken from the designs in docs/design/reference.
 * New UI should use these instead of the legacy values in Color.kt.
 */
object AppColors {
    // Surfaces
    val Background = Color(0xFFF7F8FA)
    val Surface = Color(0xFFFFFFFF)
    val Border = Color(0xFFECEEF2)
    val BorderStrong = Color(0xFFE3E7EC)
    val Track = Color(0xFFE9ECF1)
    val Chip = Color(0xFFF1F3F6)
    val Segment = Color(0xFFECEFF3)

    // Text
    val Ink = Color(0xFF14181F)
    val InkSoft = Color(0xFF3A4352)
    val Muted = Color(0xFF5F6B7A)
    val Ghost = Color(0xFF8A94A3)

    // Accents
    val Green = Color(0xFF22A672)
    val GreenDeep = Color(0xFF14784C)
    val GreenTint = Color(0xFFE8F6EF)
    val Blue = Color(0xFF3B82F6)
    val Amber = Color(0xFFD97706)
    val AmberDeep = Color(0xFF8A4F05)
    val AmberTint = Color(0xFFFDF3E3)

    // Macros
    val Protein = Color(0xFF3B6FE8)
    val ProteinDeep = Color(0xFF2548B8)
    val ProteinTint = Color(0xFFEEF3FE)
    val Carbs = Color(0xFFD97706)
    val CarbsDeep = Color(0xFF8A4F05)
    val CarbsTint = Color(0xFFFDF3E3)
    val Fat = Color(0xFF8B5CF6)
    val FatDeep = Color(0xFF6D3BD2)
    val FatTint = Color(0xFFF3EEFE)
}

object AppDimens {
    val ScreenGutter = 20.dp
    val CardRadius = 24.dp
    val CardRadiusLarge = 28.dp
    val SheetRadius = 32.dp
    val ButtonHeight = 56.dp
    val FieldHeight = 60.dp
    val IconButton = 44.dp
}
