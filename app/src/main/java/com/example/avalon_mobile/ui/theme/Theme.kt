package com.example.avalon_mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.avalon_mobile.data.local.Aparencia
import com.example.avalon_mobile.data.local.TemaApp

// Identidade visual do AVALON. O padrão (tema "Seguir o aparelho" com o aparelho em modo claro) é idêntico
// ao protótipo e à aplicação Web; tema escuro, alto contraste e escala de fonte são preferências do usuário
// (Sprint 3 — acessibilidade, etapas 2 a 4).
private fun esquemaDe(p: Paleta, escuro: Boolean): ColorScheme {
    val primaria = p.navy
    val sobrePrimaria = if (escuro) p.background else Color.White
    return if (escuro) {
        darkColorScheme(
            primary = primaria, onPrimary = sobrePrimaria,
            primaryContainer = p.dayWork, onPrimaryContainer = p.textPrimary,
            secondary = p.navyLight, onSecondary = sobrePrimaria,
            background = p.background, onBackground = p.textPrimary,
            surface = p.surface, onSurface = p.textPrimary,
            surfaceVariant = p.background, onSurfaceVariant = p.textSecondary,
            outline = p.border, outlineVariant = p.borderSoft,
            error = p.danger, onError = p.background
        )
    } else {
        lightColorScheme(
            primary = primaria, onPrimary = sobrePrimaria,
            primaryContainer = p.dayWork, onPrimaryContainer = p.textPrimary,
            secondary = p.navyLight, onSecondary = sobrePrimaria,
            background = p.background, onBackground = p.textPrimary,
            surface = p.surface, onSurface = p.textPrimary,
            surfaceVariant = p.background, onSurfaceVariant = p.textSecondary,
            outline = p.border, outlineVariant = p.borderSoft,
            error = p.danger, onError = Color.White
        )
    }
}

@Composable
fun AVALONMOBILETheme(
    aparencia: Aparencia = Aparencia(),
    content: @Composable () -> Unit
) {
    val escuro = when (aparencia.tema) {
        TemaApp.ESCURO -> true
        TemaApp.CLARO -> false
        TemaApp.SISTEMA -> isSystemInDarkTheme()
    }
    val paleta = Paletas.escolher(escuro, aparencia.altoContraste)
    // Os tokens (Navy, Surface…) leem Paletas.atual. A escrita precede os filhos na mesma composição; este
    // escopo não lê o estado, e valores iguais (data class) não invalidam nada.
    Paletas.atual = paleta
    val esquema = remember(paleta, escuro) { esquemaDe(paleta, escuro) }

    // A escala multiplica a fonte do aparelho (Configurações > Tamanho da fonte); layouts em dp não mudam.
    val densidade = LocalDensity.current
    val escala = aparencia.escalaFonte
    val densidadeAjustada = remember(densidade.density, densidade.fontScale, escala) {
        Density(densidade.density, densidade.fontScale * escala)
    }

    CompositionLocalProvider(
        LocalDensity provides densidadeAjustada,
        LocalContentColor provides paleta.textPrimary
    ) {
        MaterialTheme(
            colorScheme = esquema,
            typography = Typography,
            content = content
        )
    }
}
