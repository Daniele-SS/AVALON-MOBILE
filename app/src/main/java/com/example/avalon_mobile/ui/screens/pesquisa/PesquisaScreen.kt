package com.example.avalon_mobile.ui.screens.pesquisa

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.R
import com.example.avalon_mobile.model.Pergunta
import com.example.avalon_mobile.ui.components.AvalonPrimaryButton
import com.example.avalon_mobile.ui.components.AvalonSecondaryButton
import com.example.avalon_mobile.ui.components.AvalonTextField
import com.example.avalon_mobile.ui.components.LoadStateContent
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.BadgeGreenBg
import com.example.avalon_mobile.ui.theme.BadgeGreenText
import com.example.avalon_mobile.ui.theme.Border
import com.example.avalon_mobile.ui.theme.Danger
import com.example.avalon_mobile.ui.theme.DangerContainer
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.viewmodel.EtapaPesquisa
import com.example.avalon_mobile.viewmodel.PesquisaUi
import com.example.avalon_mobile.viewmodel.PesquisaViewModel
import com.example.avalon_mobile.viewmodel.avalonViewModel

/** Responder pesquisa com alerta de perguntas obrigatórias e revisão (Sprint 4 — 6.6). */
@Composable
fun PesquisaScreen(
    onSair: () -> Unit,
    viewModel: PesquisaViewModel = avalonViewModel { c, h -> PesquisaViewModel(c.avalonRepository, h) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmarSaida by rememberSaveable { mutableStateOf(false) }
    val sair = { if ((state as? com.example.avalon_mobile.ui.components.LoadState.Content)?.data?.respostas?.isNotEmpty() == true) confirmarSaida = true else onSair() }

    BackHandler { if (!viewModel.anterior()) sair() }

    Column(Modifier.fillMaxSize().background(Background)) {
        LoadStateContent(state, onRetry = viewModel::carregar) { ui ->
            when (ui.etapa) {
                EtapaPesquisa.PERGUNTAS -> {
                    FluxoTopBar(ui.pesquisa.titulo, onFechar = sair)
                    Pergunta(ui, viewModel)
                }
                EtapaPesquisa.REVISAO -> {
                    FluxoTopBar("Revisar respostas", onFechar = sair)
                    Revisao(ui, viewModel)
                }
                EtapaPesquisa.ENVIADA -> {
                    FluxoTopBar(ui.pesquisa.titulo, onFechar = onSair)
                    Concluido(
                        titulo = "Respostas enviadas",
                        texto = if (ui.pesquisa.anonimo) "Obrigado por participar. Suas respostas são anônimas e serão analisadas apenas de forma consolidada."
                        else "Obrigado por participar. Suas respostas foram registradas.",
                        onVoltar = onSair
                    )
                }
            }
            if (ui.confirmarEnvio) {
                AlertDialog(
                    onDismissRequest = viewModel::cancelarConfirmacao,
                    containerColor = Color.White,
                    title = { Text("Confirmar envio?", fontFamily = Inter, fontWeight = FontWeight.Bold, color = TextPrimary) },
                    text = {
                        Text(
                            if (ui.pesquisa.anonimo) "Após o envio, esta resposta anônima não poderá ser alterada." else "Após o envio, as respostas não poderão ser alteradas.",
                            fontFamily = Inter, color = TextPrimary
                        )
                    },
                    confirmButton = { TextButton(onClick = viewModel::enviar) { Text("Confirmar", color = Navy, fontFamily = Inter) } },
                    dismissButton = { TextButton(onClick = viewModel::cancelarConfirmacao) { Text("Cancelar", color = TextSecondary, fontFamily = Inter) } }
                )
            }
        }
    }

    if (confirmarSaida) {
        AlertDialog(
            onDismissRequest = { confirmarSaida = false },
            containerColor = Color.White,
            title = { Text("Sair da pesquisa?", fontFamily = Inter, fontWeight = FontWeight.Bold) },
            text = { Text("As respostas preenchidas serão descartadas.", fontFamily = Inter) },
            confirmButton = { TextButton(onClick = { confirmarSaida = false; onSair() }) { Text("Sair", color = Danger, fontFamily = Inter) } },
            dismissButton = { TextButton(onClick = { confirmarSaida = false }) { Text("Continuar respondendo", color = Navy, fontFamily = Inter) } }
        )
    }
}

@Composable
private fun Pergunta(ui: PesquisaUi, viewModel: PesquisaViewModel) {
    val pergunta = ui.atual ?: return
    Column(
        Modifier.fillMaxSize().imePadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Pergunta ${ui.indice + 1} de ${ui.perguntas.size}", fontFamily = Inter, fontSize = 14.sp, color = TextSecondary)
        ProgressoSegmentado((ui.indice + 1f) / ui.perguntas.size, segmentos = ui.perguntas.size.coerceAtMost(10))
        Spacer(Modifier.height(8.dp))
        Text(
            pergunta.enunciado + if (pergunta.obrigatorio) " *" else "",
            fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = TextPrimary
        )
        if (pergunta.isEscala) {
            PesquisaViewModel.ESCALA.forEach { (valor, rotulo) ->
                OpcaoResposta(rotulo, ui.respostas[pergunta.id] == valor) { viewModel.responder(pergunta.id, valor) }
            }
        } else {
            AvalonTextField(
                label = "",
                value = ui.respostas[pergunta.id].orEmpty(),
                onValueChange = { viewModel.responder(pergunta.id, it.take(1000)) },
                placeholder = "Escreva sua resposta...",
                singleLine = false,
                minHeight = 150
            )
        }
        if (ui.alertaObrigatoria) {
            Aviso("Esta pergunta é obrigatória. Responda para continuar.", erro = true)
        } else {
            Aviso("Perguntas com * são obrigatórias", erro = false)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AvalonSecondaryButton("Voltar", onClick = { viewModel.anterior() }, modifier = Modifier.weight(1f), enabled = ui.indice > 0)
            Column(Modifier.weight(1f)) {
                AvalonPrimaryButton(if (ui.indice == ui.perguntas.lastIndex || ui.voltarParaRevisao) "Revisar" else "Próxima", onClick = viewModel::proxima)
            }
        }
    }
}

@Composable
private fun Revisao(ui: PesquisaUi, viewModel: PesquisaViewModel) {
    Column(
        Modifier.fillMaxSize().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (ui.pesquisa.anonimo) {
            Row(
                Modifier.fillMaxWidth().background(BadgeGreenBg, RoundedCornerShape(14.dp)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(R.drawable.ic_shield_check), null, tint = BadgeGreenText, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text("Resposta anônima - sua identidade não será associada", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = BadgeGreenText)
            }
        }
        ui.perguntas.forEachIndexed { i, p ->
            ItemRevisao(p, ui.respostas[p.id], onEditar = { viewModel.editar(i) })
            HorizontalDivider(color = Border, thickness = 2.dp)
        }
        ui.erro?.let { Aviso(it, erro = true) }
        AvalonPrimaryButton("Enviar respostas", onClick = viewModel::pedirConfirmacao, loading = ui.enviando)
    }
}

@Composable
private fun ItemRevisao(pergunta: Pergunta, resposta: String?, onEditar: () -> Unit) {
    val texto = when {
        resposta.isNullOrBlank() -> if (pergunta.obrigatorio) "Obrigatória — sem resposta" else "Sem resposta"
        pergunta.isEscala -> PesquisaViewModel.ESCALA.firstOrNull { it.first == resposta }?.second ?: resposta
        else -> resposta
    }
    Column(Modifier.fillMaxWidth().clickable(onClick = onEditar)) {
        Text(pergunta.enunciado + if (pergunta.obrigatorio) " *" else "", fontFamily = Inter, fontSize = 15.sp, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                texto, Modifier.weight(1f), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
                color = if (resposta.isNullOrBlank() && pergunta.obrigatorio) Danger else TextPrimary
            )
            Icon(painterResource(R.drawable.ic_pen_line), contentDescription = "Editar resposta", tint = Navy, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
internal fun Aviso(texto: String, erro: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (erro) DangerContainer else Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, if (erro) Danger.copy(alpha = 0.4f) else Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(R.drawable.ic_info), null, tint = if (erro) Danger else Navy, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(texto, fontFamily = Inter, fontSize = 15.sp, color = if (erro) Danger else TextPrimary)
    }
}

@Composable
internal fun Concluido(titulo: String, texto: String, onVoltar: () -> Unit) {
    Column(
        Modifier.fillMaxSize().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.foundation.layout.Box(
            Modifier.size(100.dp).background(BadgeGreenBg, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_check), null, tint = BadgeGreenText, modifier = Modifier.size(60.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(titulo, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = TextPrimary)
        Spacer(Modifier.height(12.dp))
        Text(texto, fontFamily = Inter, fontSize = 17.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        AvalonPrimaryButton("Voltar ao início", onClick = onVoltar)
    }
}
