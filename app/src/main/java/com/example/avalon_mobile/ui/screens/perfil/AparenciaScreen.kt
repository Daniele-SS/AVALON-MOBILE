package com.example.avalon_mobile.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avalon_mobile.data.local.EscalasFonte
import com.example.avalon_mobile.data.local.TemaApp
import com.example.avalon_mobile.ui.components.AvalonCard
import com.example.avalon_mobile.ui.components.AvalonTopBar
import com.example.avalon_mobile.ui.theme.AparenciaState
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.BorderSoft
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.ui.theme.TextStrong

/**
 * Aparência e acessibilidade (Sprint 3 — acessibilidade, etapas 2 a 4): tema claro/escuro, alto contraste e
 * tamanho da fonte. A mudança é imediata e fica salva no aparelho.
 */
@Composable
fun AparenciaScreen(aparencia: AparenciaState, onBack: () -> Unit) {
    val atual = aparencia.atual
    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Aparência e acessibilidade", onBack = onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvalonCard(radius = 16.dp, border = BorderSoft, contentPadding = 15.dp) {
                Titulo("Tema")
                Column(Modifier.selectableGroup()) {
                    TemaApp.values().forEach { tema ->
                        OpcaoUnica(tema.rotulo, atual.tema == tema) { aparencia.definirTema(tema) }
                    }
                }
            }

            AvalonCard(radius = 16.dp, border = BorderSoft, contentPadding = 15.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .toggleable(
                            value = atual.altoContraste,
                            role = Role.Switch,
                            onValueChange = { aparencia.definirAltoContraste(it) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Alto contraste", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextStrong)
                        Text(
                            "Texto e bordas com contraste mínimo de 7:1 (WCAG AAA).",
                            fontFamily = Inter, fontSize = 12.sp, color = TextSecondary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(checked = atual.altoContraste, onCheckedChange = null)
                }
            }

            AvalonCard(radius = 16.dp, border = BorderSoft, contentPadding = 15.dp) {
                Titulo("Tamanho da fonte")
                Text(
                    "Soma-se ao tamanho definido nas configurações do aparelho.",
                    fontFamily = Inter, fontSize = 12.sp, color = TextSecondary
                )
                Column(Modifier.selectableGroup()) {
                    EscalasFonte.opcoes.forEach { (escala, rotulo) ->
                        OpcaoUnica(rotulo, atual.escalaFonte == escala) { aparencia.definirEscalaFonte(escala) }
                    }
                }
                Text(
                    "Exemplo: sua solicitação de férias foi aprovada.",
                    Modifier.padding(top = 8.dp),
                    fontFamily = Inter, fontSize = 15.sp, color = TextPrimary
                )
            }

            TextButton(onClick = aparencia::restaurarPadrao, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Restaurar padrão", fontFamily = Inter, fontSize = 15.sp, color = Navy)
            }
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(
        texto, Modifier.semantics { heading() }.padding(bottom = 4.dp),
        fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextStrong
    )
}

@Composable
private fun OpcaoUnica(rotulo: String, selecionada: Boolean, onSelecionar: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selecionada, role = Role.RadioButton, onClick = onSelecionar),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selecionada, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(rotulo, fontFamily = Inter, fontSize = 15.sp, color = TextPrimary)
    }
}
