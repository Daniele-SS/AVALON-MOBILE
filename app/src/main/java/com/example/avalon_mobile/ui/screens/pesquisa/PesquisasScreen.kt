package com.example.avalon_mobile.ui.screens.pesquisa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.avalon_mobile.model.Pesquisa
import com.example.avalon_mobile.ui.components.AvalonCard
import com.example.avalon_mobile.ui.components.AvalonTopBar
import com.example.avalon_mobile.ui.components.EmptyState
import com.example.avalon_mobile.ui.components.LoadStateContent
import com.example.avalon_mobile.ui.components.StatusPill
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.BadgeBlueBg
import com.example.avalon_mobile.ui.theme.BadgeBlueText
import com.example.avalon_mobile.ui.theme.BadgeGreenBg
import com.example.avalon_mobile.ui.theme.BadgeGreenText
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.util.Datas
import com.example.avalon_mobile.viewmodel.PesquisasViewModel
import com.example.avalon_mobile.viewmodel.avalonViewModel

/** Pesquisas e avaliações disponíveis (Sprint 4 — 6.6). */
@Composable
fun PesquisasScreen(
    onBack: (() -> Unit)?,
    onAbrirPesquisa: (Int) -> Unit,
    onAbrirAvaliacao: () -> Unit,
    viewModel: PesquisasViewModel = avalonViewModel { c, _ -> PesquisasViewModel(c.avalonRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var aba by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { viewModel.carregar() }

    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Pesquisas", onBack = onBack)
        LoadStateContent(state, onRetry = viewModel::carregar) { ui ->
            TabRow(
                selectedTabIndex = aba,
                containerColor = Background,
                contentColor = Navy,
                indicator = { posicoes -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(posicoes[aba]), color = Navy) }
            ) {
                Tab(aba == 0, { aba = 0 }, text = { Text("Pendentes (${ui.totalPendentes})", fontFamily = Inter, fontSize = 16.sp) })
                Tab(aba == 1, { aba = 1 }, text = { Text("Respondidas", fontFamily = Inter, fontSize = 16.sp) })
            }
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (aba == 0) {
                    ui.avaliacao?.let { av ->
                        item(key = "avaliacao") {
                            CartaoPendente(av.titulo, "Encerra em ${Datas.diaMes(av.dataFim)}", anonima = av.anonimo, onClick = onAbrirAvaliacao)
                        }
                    }
                    items(ui.pendentes, key = { it.id }) { p ->
                        CartaoPendente(
                            p.titulo,
                            listOfNotNull(p.totalPerguntas?.let { "$it perguntas" }, "encerra em ${Datas.diaMes(p.dataFim)}").joinToString(" - "),
                            anonima = p.anonimo,
                            onClick = { onAbrirPesquisa(p.id) }
                        )
                    }
                    if (ui.totalPendentes == 0) item { EmptyState("Nenhuma pesquisa pendente. Obrigado por participar!") }
                } else {
                    items(ui.respondidas, key = { it.id }) { CartaoRespondida(it) }
                    if (ui.respondidas.isEmpty()) item { EmptyState("Nenhuma pesquisa respondida ainda.") }
                }
            }
        }
    }
}

@Composable
private fun CartaoPendente(titulo: String, detalhe: String, anonima: Boolean, onClick: () -> Unit) {
    AvalonCard(onClick = onClick, radius = 16.dp) {
        Row(verticalAlignment = Alignment.Top) {
            Text(titulo, Modifier.weight(1f), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
            if (anonima) StatusPill("Anônima", BadgeGreenBg, BadgeGreenText) else StatusPill("Identificada", BadgeBlueBg, BadgeBlueText)
        }
        Spacer(Modifier.height(6.dp))
        Text(detalhe, fontFamily = Inter, fontSize = 15.sp, color = TextSecondary)
    }
}

@Composable
private fun CartaoRespondida(p: Pesquisa) {
    val respondida = p.respondida == true
    AvalonCard(radius = 16.dp, container = if (respondida) Color.White else Color(0xFFF7F7F7)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(p.titulo, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
                Text(
                    if (respondida) "Respondida em ${Datas.diaMes(p.dataResposta)}" else "Encerrada em ${Datas.diaMes(p.dataFim)}",
                    fontFamily = Inter, fontSize = 15.sp, color = TextSecondary
                )
            }
            if (!respondida) Icon(painterResource(R.drawable.ic_lock), "Encerrada", tint = TextSecondary, modifier = Modifier.size(22.dp))
            else Icon(painterResource(R.drawable.ic_circle_check), "Respondida", tint = BadgeGreenText, modifier = Modifier.size(22.dp))
        }
    }
}
