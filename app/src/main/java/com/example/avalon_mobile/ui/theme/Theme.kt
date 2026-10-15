package com.example.avalon_mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Identidade visual fixa do AVALON: sem cor dinâmica nem tema escuro, para manter
// a aparência idêntica ao protótipo e à aplicação Web (RNF de consistência Web/Mobile).
private val AvalonColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = DayWork,
    onPrimaryContainer = TextPrimary,
    secondary = NavyLight,
    onSecondary = Color.White,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Background,
    onSurfaceVariant = TextSecondary,
    outline = Border,
    outlineVariant = BorderSoft,
    error = Danger,
    onError = Color.White,
)

@Composable
fun AVALONMOBILETheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AvalonColorScheme,
        typography = Typography,
        content = content
    )
}
