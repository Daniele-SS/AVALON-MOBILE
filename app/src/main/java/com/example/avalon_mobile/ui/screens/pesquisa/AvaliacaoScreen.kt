package com.example.avalon_mobile.ui.screens.pesquisa

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.R
import com.example.avalon_mobile.ui.components.AvalonPrimaryButton
import com.example.avalon_mobile.ui.components.EmptyState
import com.example.avalon_mobile.ui.components.LoadStateContent
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.BadgeBlueBg
import com.example.avalon_mobile.ui.theme.BadgeGreenBg
import com.example.avalon_mobile.ui.theme.BadgeGreenText
import com.example.avalon_mobile.ui.theme.BadgeOrangeBg
import com.example.avalon_mobile.ui.theme.BadgeOrangeText
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.viewmodel.AvaliacaoUi
import com.example.avalon_mobile.viewmodel.AvaliacaoViewModel
import com.example.avalon_mobile.viewmodel.EtapaAvaliacao
import com.example.avalon_mobile.viewmodel.avalonViewModel

/** Avaliação de fatores de risco psicossocial — NR-1 (Sprint 4 — 6.6). */
@Composable
fun AvaliacaoScreen(
    onSair: () -> Unit,
    viewModel: AvaliacaoViewModel = avalonViewModel { c, _ -> AvaliacaoViewModel(c.avalonRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler { if (!viewModel.voltar()) onSair() }

    Column(Modifier.fillMaxSize().background(Background)) {
        FluxoTopBar("Riscos psicossociais", onFechar = onSair)
        LoadStateContent(state, onRetry = viewModel::carregar) { ui ->
            when {
                ui == null -> EmptyState("Não há avaliação pendente no momento.")
                ui.etapa == EtapaAvaliacao.INTRO -> Introducao(ui, viewModel::iniciar)
                ui.etapa == EtapaAvaliacao.PERGUNTAS -> Perguntas(ui, viewModel)
                else -> Concluido(
                    titulo = "Respostas enviadas",
                    texto = "Obrigado por participar. Suas respostas serão analisadas de forma consolidada, sem identificação individual.",
                    onVoltar = onSair
                )
            }
        }
    }
}

@Composable
private fun Introducao(ui: AvaliacaoUi, onIniciar: () -> Unit) {
    Column(
        Modifier.fillMaxSize().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(Modifier.size(70.dp).background(BadgeBlueBg, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_feather), null, tint = Navy, modifier = Modifier.size(40.dp))
        }
        Text("Fatores de risco psicossocial", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = TextPrimary)
        Text(
            ui.avaliacao.descricao ?: "Esta avaliação faz parte das ações da empresa para acompanhar o ambiente de trabalho, conforme a NR-1. Leva cerca de 5 minutos.",
            fontFamily = Inter, fontSize = 17.sp, color = TextSecondary
        )
        Quadro(R.drawable.ic_shield_check, BadgeGreenBg, BadgeGreenText,
            "Suas respostas são analisadas apenas de forma consolidada, junto com as de outros colaboradores.")
        // RF 1.1.28: nenhuma classificação individual de condição psicológica/médica.
        Quadro(R.drawable.ic_shield_alert, BadgeOrangeBg, BadgeOrangeText,
            "Este questionário não gera diagnóstico, classificação ou avaliação individual. Não substitui acompanhamento profissional de saúde.")
        ui.erro?.let { Aviso(it, erro = true) }
        AvalonPrimaryButton("Iniciar avaliação", onClick = onIniciar, loading = ui.carregandoPerguntas)
    }
}

@Composable
private fun Quadro(icon: Int, bg: Color, fg: Color, texto: String) {
    Row(Modifier.fillMaxWidth().background(bg, RoundedCornerShape(16.dp)).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), null, tint = fg, modifier = Modifier.size(36.dp))
        Spacer(Modifier.width(14.dp))
        Text(texto, fontFamily = Inter, fontSize = 15.sp, color = TextPrimary)
    }
}

@Composable
private fun Perguntas(ui: AvaliacaoUi, viewModel: AvaliacaoViewModel) {
    val pergunta = ui.atual ?: return
    Column(
        Modifier.fillMaxSize().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProgressoSegmentado(ui.progresso)
        Text("Pergunta ${ui.indice + 1} de ${ui.perguntas.size}", fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
        Text("SEM RESPOSTA CERTA OU ERRADA", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextSecondary)
        Text(pergunta.enunciado, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = TextPrimary)
        AvaliacaoViewModel.FREQUENCIA.forEach { (valor, rotulo) ->
            OpcaoResposta(rotulo, ui.respostas[pergunta.id] == valor) { viewModel.responder(valor) }
        }
        if (ui.alertaObrigatoria) Aviso("Selecione uma opção para continuar.", erro = true)
        ui.erro?.let { Aviso(it, erro = true) }
        Spacer(Modifier.height(4.dp))
        AvalonPrimaryButton(
            if (ui.indice == ui.perguntas.lastIndex) "Enviar respostas" else "Continuar",
            onClick = viewModel::continuar,
            loading = ui.enviando
        )
        Text(
            "Respostas anônimas: enviadas em conjunto, sem vínculo com sua identidade.",
            Modifier.fillMaxWidth().padding(top = 4.dp),
            fontFamily = Inter, fontSize = 12.sp, color = TextSecondary
        )
    }
}
