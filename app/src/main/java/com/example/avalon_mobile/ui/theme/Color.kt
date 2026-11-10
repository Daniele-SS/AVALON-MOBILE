package com.example.avalon_mobile.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Tokens extraídos do protótipo AVALON (Figma).
//
// Cada token é uma propriedade com getter: lê a paleta ativa (estado do Compose), então os pontos de uso
// continuam iguais (`color = Navy`) e a tela recompõe quando o usuário troca tema ou alto contraste.
// A paleta é escolhida em AVALONMOBILETheme (Theme.kt). Os valores claros são os do protótipo original.
//
// Navy       = cor de CONTEÚDO (texto, ícone, borda de foco): clara no tema escuro, para contrastar com o fundo.
// NavyFundo  = cor de FUNDO de barras e botões com texto branco por cima.
// Todas as combinações texto/fundo foram verificadas (>= 4,5:1; >= 7:1 em alto contraste), ver docs/DOCUMENTACAO.md.

@Immutable
data class Paleta(
    val navy: Color,
    val navyLight: Color,
    val background: Color,
    val surface: Color,
    val border: Color,
    val borderSoft: Color,
    val borderStrong: Color,
    val inputBorder: Color,
    val textPrimary: Color,
    val textStrong: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textMutedAlpha: Color,
    val textSubtle: Color,
    val tabInactive: Color,
    val accent: Color,
    val link: Color,
    val dayWork: Color,
    val dayOff: Color,
    val dayOffText: Color,
    val infoContainer: Color,
    val infoBorder: Color,
    val warningContainer: Color,
    val warningBorder: Color,
    val warning: Color,
    val warningText: Color,
    val successContainer: Color,
    val success: Color,
    val successText: Color,
    val statusPendingBg: Color,
    val statusPendingText: Color,
    val statusApprovedBg: Color,
    val statusApprovedText: Color,
    val statusDeniedBg: Color,
    val statusDeniedText: Color,
    val danger: Color,
    val dangerContainer: Color,
    val badgeGreenBg: Color,
    val badgeGreenText: Color,
    val badgeBlueBg: Color,
    val badgeBlueText: Color,
    val badgeOrangeBg: Color,
    val badgeOrangeText: Color,
    val navyFundo: Color,
    val surfaceInfo: Color,
    val surfaceMuted: Color,
    val surfaceSelected: Color,
    val diaHoje: Color,
    val diaHojeTexto: Color
)

object Paletas {
    val Claro = Paleta(
        navy = Color(0xFF001F3F),
        navyLight = Color(0xFF03305E),
        background = Color(0xFFF4F7FA),
        surface = Color(0xFFFFFFFF),
        border = Color(0xFFD9E2EC),
        borderSoft = Color(0xFFE4E9F0),
        borderStrong = Color(0xFFA7A7A7),
        inputBorder = Color(0xFF7092B7),
        textPrimary = Color(0xFF102A43),
        textStrong = Color(0xFF1A2530),
        textSecondary = Color(0xFF5B748D),
        textMuted = Color(0xFF717171),
        textMutedAlpha = Color(0xFF717171),
        textSubtle = Color(0xFF6B7280),
        tabInactive = Color(0xFF85B7EB),
        accent = Color(0xFFACC6F5),
        link = Color(0xFF006EDF),
        dayWork = Color(0xFFBADAFF),
        dayOff = Color(0xFFD0E69A),
        dayOffText = Color(0xFF4C6A00),
        infoContainer = Color(0x24198EFF),
        infoBorder = Color(0x300095FF),
        warningContainer = Color(0x24FFAE00),
        warningBorder = Color(0x30FF8000),
        warning = Color(0xFFFFBC50),
        warningText = Color(0xFF9F6200),
        successContainer = Color(0x2450AF08),
        success = Color(0xFF50AF08),
        successText = Color(0xFF397C06),
        statusPendingBg = Color(0x61F39E00),
        statusPendingText = Color(0xFF694500),
        statusApprovedBg = Color(0x6134A300),
        statusApprovedText = Color(0xFF216600),
        statusDeniedBg = Color(0x38C00000),
        statusDeniedText = Color(0xFFB20000),
        danger = Color(0xFFD32F2F),
        dangerContainer = Color(0xFFFFF3F3),
        badgeGreenBg = Color(0xFFE6F4EA),
        badgeGreenText = Color(0xFF1E7B34),
        badgeBlueBg = Color(0xFFE3F0FB),
        badgeBlueText = Color(0xFF1565C0),
        badgeOrangeBg = Color(0xFFFFF1DC),
        badgeOrangeText = Color(0xFF8A5300),
        navyFundo = Color(0xFF001F3F),
        surfaceInfo = Color(0xFFF0F6FF),
        surfaceMuted = Color(0xFFF7F7F7),
        surfaceSelected = Color(0xFFEAF3FF),
        diaHoje = Color(0xFF001F3F),
        diaHojeTexto = Color(0xFFFFFFFF)
    )

    val Escuro = Paleta(
        navy = Color(0xFF8DB7E8),
        navyLight = Color(0xFF5C93D1),
        background = Color(0xFF0B1622),
        surface = Color(0xFF132233),
        border = Color(0xFF2E4660),
        borderSoft = Color(0xFF223750),
        borderStrong = Color(0xFF8A9BAD),
        inputBorder = Color(0xFF7FA3CC),
        textPrimary = Color(0xFFE6EEF7),
        textStrong = Color(0xFFF2F6FA),
        textSecondary = Color(0xFFA9BDD1),
        textMuted = Color(0xFFA3AFBC),
        textMutedAlpha = Color(0xFFA3AFBC),
        textSubtle = Color(0xFFA7B2C0),
        tabInactive = Color(0xFFC4DDF7),
        accent = Color(0xFFACC6F5),
        link = Color(0xFF6CB2FF),
        dayWork = Color(0xFF1E3A5C),
        dayOff = Color(0xFF2B4A12),
        dayOffText = Color(0xFFC5E18A),
        infoContainer = Color(0x24198EFF),
        infoBorder = Color(0x300095FF),
        warningContainer = Color(0x24FFAE00),
        warningBorder = Color(0x30FF8000),
        warning = Color(0xFFFFBC50),
        warningText = Color(0xFFFFCB6B),
        successContainer = Color(0x2450AF08),
        success = Color(0xFF5FBF2A),
        successText = Color(0xFF8FDC5A),
        statusPendingBg = Color(0x61F39E00),
        statusPendingText = Color(0xFFFFD27A),
        statusApprovedBg = Color(0x6134A300),
        statusApprovedText = Color(0xFF9BE870),
        statusDeniedBg = Color(0x38C00000),
        statusDeniedText = Color(0xFFFF9A9A),
        danger = Color(0xFFFF6B6B),
        dangerContainer = Color(0xFF3A1A1F),
        badgeGreenBg = Color(0xFF173A24),
        badgeGreenText = Color(0xFF7EDC9A),
        badgeBlueBg = Color(0xFF14324F),
        badgeBlueText = Color(0xFF8CC4FF),
        badgeOrangeBg = Color(0xFF3D2A0E),
        badgeOrangeText = Color(0xFFFFC36B),
        navyFundo = Color(0xFF17417A),
        surfaceInfo = Color(0xFF182F47),
        surfaceMuted = Color(0xFF0F1C2A),
        surfaceSelected = Color(0xFF1B3A5C),
        diaHoje = Color(0xFF8DB7E8),
        diaHojeTexto = Color(0xFF0B1622)
    )

    val ClaroAltoContraste = Paleta(
        navy = Color(0xFF00112B),
        navyLight = Color(0xFF00112B),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        border = Color(0xFF4A4A4A),
        borderSoft = Color(0xFF4A4A4A),
        borderStrong = Color(0xFF000000),
        inputBorder = Color(0xFF000000),
        textPrimary = Color(0xFF000000),
        textStrong = Color(0xFF000000),
        textSecondary = Color(0xFF1F2933),
        textMuted = Color(0xFF2B2B2B),
        textMutedAlpha = Color(0xFF2B2B2B),
        textSubtle = Color(0xFF2B2B2B),
        tabInactive = Color(0xFFD6E6F7),
        accent = Color(0xFFACC6F5),
        link = Color(0xFF0033A0),
        dayWork = Color(0xFFBADAFF),
        dayOff = Color(0xFFD0E69A),
        dayOffText = Color(0xFF2E4000),
        infoContainer = Color(0x24198EFF),
        infoBorder = Color(0x300095FF),
        warningContainer = Color(0x24FFAE00),
        warningBorder = Color(0x30FF8000),
        warning = Color(0xFFFFBC50),
        warningText = Color(0xFF5C3900),
        successContainer = Color(0x2450AF08),
        success = Color(0xFF2E7D00),
        successText = Color(0xFF1F4D03),
        statusPendingBg = Color(0x61F39E00),
        statusPendingText = Color(0xFF3D2800),
        statusApprovedBg = Color(0x6134A300),
        statusApprovedText = Color(0xFF123B00),
        statusDeniedBg = Color(0x38C00000),
        statusDeniedText = Color(0xFF7A0000),
        danger = Color(0xFFA00000),
        dangerContainer = Color(0xFFFFFFFF),
        badgeGreenBg = Color(0xFFFFFFFF),
        badgeGreenText = Color(0xFF0F4D1F),
        badgeBlueBg = Color(0xFFFFFFFF),
        badgeBlueText = Color(0xFF002A6B),
        badgeOrangeBg = Color(0xFFFFFFFF),
        badgeOrangeText = Color(0xFF5C3900),
        navyFundo = Color(0xFF00112B),
        surfaceInfo = Color(0xFFFFFFFF),
        surfaceMuted = Color(0xFFFFFFFF),
        surfaceSelected = Color(0xFFE3EEFF),
        diaHoje = Color(0xFF00112B),
        diaHojeTexto = Color(0xFFFFFFFF)
    )

    val EscuroAltoContraste = Paleta(
        navy = Color(0xFFFFFFFF),
        navyLight = Color(0xFFFFFFFF),
        background = Color(0xFF000000),
        surface = Color(0xFF000000),
        border = Color(0xFFFFFFFF),
        borderSoft = Color(0xFFBFBFBF),
        borderStrong = Color(0xFFFFFFFF),
        inputBorder = Color(0xFFFFFFFF),
        textPrimary = Color(0xFFFFFFFF),
        textStrong = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFF0F0F0),
        textMuted = Color(0xFFE6E6E6),
        textMutedAlpha = Color(0xFFE6E6E6),
        textSubtle = Color(0xFFE6E6E6),
        tabInactive = Color(0xFFFFFFFF),
        accent = Color(0xFFFFFFFF),
        link = Color(0xFF8CC8FF),
        dayWork = Color(0xFF0B2D52),
        dayOff = Color(0xFF1F3A0A),
        dayOffText = Color(0xFFD8F29B),
        infoContainer = Color(0x24198EFF),
        infoBorder = Color(0x300095FF),
        warningContainer = Color(0x24FFAE00),
        warningBorder = Color(0x30FF8000),
        warning = Color(0xFFFFD27A),
        warningText = Color(0xFFFFD27A),
        successContainer = Color(0x2450AF08),
        success = Color(0xFF8FE05A),
        successText = Color(0xFFB5F28A),
        statusPendingBg = Color(0x61F39E00),
        statusPendingText = Color(0xFFFFD98F),
        statusApprovedBg = Color(0x6134A300),
        statusApprovedText = Color(0xFFB5F28A),
        statusDeniedBg = Color(0x38C00000),
        statusDeniedText = Color(0xFFFFB3B3),
        danger = Color(0xFFFF8A8A),
        dangerContainer = Color(0xFF000000),
        badgeGreenBg = Color(0xFF000000),
        badgeGreenText = Color(0xFFB5F28A),
        badgeBlueBg = Color(0xFF000000),
        badgeBlueText = Color(0xFFB5DBFF),
        badgeOrangeBg = Color(0xFF000000),
        badgeOrangeText = Color(0xFFFFD98F),
        navyFundo = Color(0xFF00112B),
        surfaceInfo = Color(0xFF000000),
        surfaceMuted = Color(0xFF000000),
        surfaceSelected = Color(0xFF0B2D52),
        diaHoje = Color(0xFFFFFFFF),
        diaHojeTexto = Color(0xFF000000)
    )

    fun escolher(escuro: Boolean, altoContraste: Boolean): Paleta = when {
        escuro && altoContraste -> EscuroAltoContraste
        escuro -> Escuro
        altoContraste -> ClaroAltoContraste
        else -> Claro
    }

    val altoContraste: Boolean get() = atual === ClaroAltoContraste || atual === EscuroAltoContraste

    /** Paleta em uso. Escrita apenas por AVALONMOBILETheme. */
    var atual by mutableStateOf(Claro)
}

val Navy: Color get() = Paletas.atual.navy
val NavyLight: Color get() = Paletas.atual.navyLight
val Background: Color get() = Paletas.atual.background
val Surface: Color get() = Paletas.atual.surface
val Border: Color get() = Paletas.atual.border
val BorderSoft: Color get() = Paletas.atual.borderSoft
val BorderStrong: Color get() = Paletas.atual.borderStrong
val InputBorder: Color get() = Paletas.atual.inputBorder // borda de campo de texto: >= 3:1 (WCAG 1.4.11)
val TextPrimary: Color get() = Paletas.atual.textPrimary
val TextStrong: Color get() = Paletas.atual.textStrong
val TextSecondary: Color get() = Paletas.atual.textSecondary // contraste >= 4,5:1 sobre Surface e Background (WCAG AA)
val TextMuted: Color get() = Paletas.atual.textMuted // contraste >= 4,5:1 sobre Surface e Background (WCAG AA)
val TextMutedAlpha: Color get() = Paletas.atual.textMutedAlpha // opaco: com transparência o contraste caía abaixo de 4,5:1
val TextSubtle: Color get() = Paletas.atual.textSubtle
val TabInactive: Color get() = Paletas.atual.tabInactive
val Accent: Color get() = Paletas.atual.accent
val Link: Color get() = Paletas.atual.link // contraste >= 4,5:1 sobre Surface e Background (WCAG AA)
val DayWork: Color get() = Paletas.atual.dayWork
val DayOff: Color get() = Paletas.atual.dayOff
val DayOffText: Color get() = Paletas.atual.dayOffText
val InfoContainer: Color get() = Paletas.atual.infoContainer
val InfoBorder: Color get() = Paletas.atual.infoBorder
val WarningContainer: Color get() = Paletas.atual.warningContainer
val WarningBorder: Color get() = Paletas.atual.warningBorder
val Warning: Color get() = Paletas.atual.warning
val WarningText: Color get() = Paletas.atual.warningText // texto e ícone sobre WarningContainer (>= 4,5:1)
val SuccessContainer: Color get() = Paletas.atual.successContainer
val Success: Color get() = Paletas.atual.success
val SuccessText: Color get() = Paletas.atual.successText // texto e ícone sobre SuccessContainer (>= 4,5:1)
val StatusPendingBg: Color get() = Paletas.atual.statusPendingBg
val StatusPendingText: Color get() = Paletas.atual.statusPendingText
val StatusApprovedBg: Color get() = Paletas.atual.statusApprovedBg
val StatusApprovedText: Color get() = Paletas.atual.statusApprovedText
val StatusDeniedBg: Color get() = Paletas.atual.statusDeniedBg
val StatusDeniedText: Color get() = Paletas.atual.statusDeniedText
val Danger: Color get() = Paletas.atual.danger
val DangerContainer: Color get() = Paletas.atual.dangerContainer
val BadgeGreenBg: Color get() = Paletas.atual.badgeGreenBg
val BadgeGreenText: Color get() = Paletas.atual.badgeGreenText
val BadgeBlueBg: Color get() = Paletas.atual.badgeBlueBg
val BadgeBlueText: Color get() = Paletas.atual.badgeBlueText
val BadgeOrangeBg: Color get() = Paletas.atual.badgeOrangeBg
val BadgeOrangeText: Color get() = Paletas.atual.badgeOrangeText
val NavyFundo: Color get() = Paletas.atual.navyFundo
val SurfaceInfo: Color get() = Paletas.atual.surfaceInfo
val SurfaceMuted: Color get() = Paletas.atual.surfaceMuted
val SurfaceSelected: Color get() = Paletas.atual.surfaceSelected

// Atalhos da Home e da tela Mais: no alto contraste viram botões brancos com contorno preto e conteúdo preto
// (no tema escuro de alto contraste o fundo da tela é preto, então o botão branco se destaca).
val AtalhoFundo: Color get() = if (Paletas.altoContraste) Color.White else Surface
val AtalhoConteudo: Color get() = if (Paletas.altoContraste) Color.Black else TextPrimary

// Dia de hoje na Jornada: azul-marinho com texto branco no tema claro; no escuro, preenchimento claro com texto escuro,
// para se destacar dos dias de trabalho e de folga, que ficam escuros.
val DiaHoje: Color get() = Paletas.atual.diaHoje
val DiaHojeTexto: Color get() = Paletas.atual.diaHojeTexto
