package com.raye.donmoeuja.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF22252B)
val Paper = Color(0xFFF6F5F1)
val PaperDeep = Color(0xFFECEAE3)
val Card = Color(0xFFFDFCFA)
val Accent = Color(0xFFF2B33D)
val AccentSoft = Color(0xFFFCEFD2)
val Muted = Color(0xFF6B6963)
val Line = Color(0xFFC9C7C0)
val Danger = Color(0xFFB4442E)

private val Colors = lightColorScheme(
    primary = Ink,
    onPrimary = Paper,
    primaryContainer = Accent,
    onPrimaryContainer = Ink,
    secondary = Accent,
    onSecondary = Ink,
    secondaryContainer = AccentSoft,
    onSecondaryContainer = Ink,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperDeep,
    onSurfaceVariant = Muted,
    surfaceContainer = Card,
    surfaceContainerLow = Card,
    surfaceContainerHigh = Card,
    surfaceContainerHighest = PaperDeep,
    outline = Line,
    outlineVariant = Color(0xFFE2E0D9),
    error = Danger,
)

@Composable
fun DonTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
