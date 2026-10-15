package com.example.avalon_mobile.ui.screens.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.model.Feedback
import com.example.avalon_mobile.ui.components.AvalonCard
import com.example.avalon_mobile.ui.components.AvalonPrimaryButton
import com.example.avalon_mobile.ui.components.AvalonTextField
import com.example.avalon_mobile.ui.components.AvalonTopBar
import com.example.avalon_mobile.ui.components.EmptyState
import com.example.avalon_mobile.ui.components.LoadStateContent
import com.example.avalon_mobile.ui.components.SectionTitle
import com.example.avalon_mobile.ui.components.StatusPill
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.BadgeBlueBg
import com.example.avalon_mobile.ui.theme.BadgeBlueText
import com.example.avalon_mobile.ui.theme.BadgeGreenBg
import com.example.avalon_mobile.ui.theme.BadgeGreenText
import com.example.avalon_mobile.ui.theme.BadgeOrangeBg
import com.example.avalon_mobile.ui.theme.BadgeOrangeText
import com.example.avalon_mobile.ui.theme.BorderSoft
import com.example.avalon_mobile.ui.theme.Danger
import com.example.avalon_mobile.ui.theme.DayWork
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Link
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.StatusApprovedText
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.util.Datas
import com.example.avalon_mobile.viewmodel.FeedbackDetalheViewModel
import com.example.avalon_mobile.viewmodel.FeedbacksViewModel
import com.example.avalon_mobile.viewmodel.avalonViewModel

/** Lista de feedbacks recebidos (Sprint 3/4 — 6.7). */
@Composable
fun FeedbacksScreen(
    onBack: () -> Unit,
    onAbrir: (Int) -> Unit,
    viewModel: FeedbacksViewModel = avalonViewModel { c, _ -> FeedbacksViewModel(c.avalonRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Feedbacks", onBack = onBack)
        LoadStateContent(state, onRetry = viewModel::carregar) { lista ->
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item { SectionTitle("Recebidos") }
                if (lista.isEmpty()) item { EmptyState("Você ainda não recebeu feedbacks.") }
                items(lista, key = { it.id }) { fb ->
                    AvalonCard(onClick = { onAbrir(fb.id) }, radius = 16.dp, border = BorderSoft) {
                        Remetente(fb, data = Datas.diaMes(fb.dataFeedback))
                        Spacer(Modifier.height(10.dp))
                        Text(fb.descricao.orEmpty(), maxLines = 2, overflow = TextOverflow.Ellipsis, fontFamily = Inter, fontSize = 14.sp, color = TextPrimary)
                        if (fb.lido == false) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).background(Link, CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text("Não lido", fontFamily = Inter, fontSize = 12.sp, color = Link)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Detalhe com resposta do destinatário (Figma "Feedbacks" — responder). */
@Composable
fun FeedbackDetalheScreen(
    onBack: () -> Unit,
    viewModel: FeedbackDetalheViewModel = avalonViewModel { c, h -> FeedbackDetalheViewModel(c.avalonRepository, h) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Feedbacks", onBack = onBack)
        LoadStateContent(state.feedback, onRetry = viewModel::carregar) { fb ->
            Column(
                Modifier
                    .fillMaxSize()
                    .imePadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Remetente(fb, data = "registrado em ${Datas.dataCurta(fb.dataFeedback)}")
                AvalonCard(radius = 16.dp, border = BorderSoft) {
                    Text(fb.descricao.orEmpty(), fontFamily = Inter, fontSize = 15.sp, color = TextPrimary)
                }
                state.respostas.forEach { r ->
                    AvalonCard(radius = 16.dp, border = BorderSoft, container = Color(0xFFF0F6FF)) {
                        Text("Sua resposta • ${Datas.dataCurta(r.dataResposta)}", fontFamily = Inter, fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(r.descricao, fontFamily = Inter, fontSize = 14.sp, color = TextPrimary)
                    }
                }
                Text("RESPONDER FEEDBACK", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextSecondary)
                AvalonTextField(
                    label = "",
                    value = state.texto,
                    onValueChange = viewModel::onTexto,
                    placeholder = "Escreva sua resposta...",
                    singleLine = false,
                    minHeight = 190,
                    error = state.erro
                )
                if (state.enviado) Text("Resposta enviada.", fontFamily = Inter, color = StatusApprovedText)
                AvalonPrimaryButton("Enviar resposta", onClick = viewModel::enviar, loading = state.enviando)
            }
        }
    }
}

@Composable
private fun Remetente(fb: Feedback, data: String) {
    val nome = fb.remetenteNome ?: "Gestor"
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(50.dp).background(DayWork, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Text(iniciais(nome), fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Navy)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(nome, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary)
            Text(listOfNotNull(fb.remetenteCargo, data).joinToString(" - "), fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
        }
        TipoPill(fb.tipo)
    }
}

@Composable
private fun TipoPill(tipo: String) {
    val (bg, fg) = when (tipo.lowercase()) {
        "reconhecimento" -> BadgeGreenBg to BadgeGreenText
        "desenvolvimento" -> BadgeOrangeBg to BadgeOrangeText
        "formal" -> BadgeBlueBg to BadgeBlueText
        else -> BadgeBlueBg to Danger
    }
    if (tipo.lowercase() != "formal") StatusPill(tipo, bg, fg, fontSize = 12)
}

private fun iniciais(nome: String): String =
    nome.split(" ").filter { it.isNotBlank() }.let { p ->
        if (nome.equals("Recursos Humanos", ignoreCase = true)) "RH"
        else (p.firstOrNull()?.take(1).orEmpty() + p.drop(1).lastOrNull()?.take(1).orEmpty()).uppercase()
    }
