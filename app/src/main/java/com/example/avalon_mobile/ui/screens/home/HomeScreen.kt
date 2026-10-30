package com.example.avalon_mobile.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.R
import com.example.avalon_mobile.ui.components.AvalonCard
import com.example.avalon_mobile.ui.components.AvalonWordmark
import com.example.avalon_mobile.ui.components.GradeFixa
import com.example.avalon_mobile.ui.components.LoadStateContent
import com.example.avalon_mobile.ui.components.SectionTitle
import com.example.avalon_mobile.ui.theme.Actor
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.Danger
import com.example.avalon_mobile.ui.theme.InfoBorder
import com.example.avalon_mobile.ui.theme.InfoContainer
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.Success
import com.example.avalon_mobile.ui.theme.SuccessContainer
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.ui.theme.Warning
import com.example.avalon_mobile.ui.theme.WarningBorder
import com.example.avalon_mobile.ui.theme.WarningContainer
import com.example.avalon_mobile.viewmodel.HomeViewModel
import com.example.avalon_mobile.viewmodel.avalonViewModel

enum class Atalho(val label: String, val icon: Int) {
    JORNADA("Jornada", R.drawable.ic_clock),
    FERIAS("Férias", R.drawable.ic_umbrella_off),
    BENEFICIOS("Benefícios", R.drawable.ic_gift),
    DOCUMENTOS("Documentos", R.drawable.ic_file_text),
    FEEDBACKS("Feedbacks", R.drawable.ic_message_circle),
    PESQUISAS("Pesquisas", R.drawable.ic_clipboard_list),
    MAIS("Mais", R.drawable.ic_layout_grid)
}

@Composable
fun HomeScreen(
    onAtalho: (Atalho) -> Unit,
    onNotificacoes: () -> Unit,
    onComunicados: () -> Unit,
    viewModel: HomeViewModel = avalonViewModel { c, _ -> HomeViewModel(c.avalonRepository, c.authRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.carregar() }

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(
            Modifier.fillMaxWidth().background(Navy).statusBarsPadding().padding(start = 26.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvalonWordmark(fontSize = 20.sp, modifier = Modifier.weight(1f))
            val naoLidas = (state as? com.example.avalon_mobile.ui.components.LoadState.Content)?.data?.notificacoesNaoLidas ?: 0
            IconButton(onClick = onNotificacoes) {
                Box {
                    Icon(painterResource(R.drawable.ic_bell), contentDescription = "Notificações", tint = Color.White, modifier = Modifier.size(30.dp))
                    if (naoLidas > 0) Box(Modifier.size(10.dp).align(Alignment.TopEnd).background(Danger, CircleShape))
                }
            }
        }
        LoadStateContent(state, onRetry = viewModel::carregar) { ui ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 28.dp)) {
                Saudacao("Olá, ${ui.nomeCompleto}", Modifier.fillMaxWidth().padding(horizontal = 26.dp))
                Spacer(Modifier.height(32.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 26.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (ui.exibirPesquisas) Indicador(R.drawable.ic_clipboard_list, ui.pesquisasPendentes, "Pesquisas\nPendentes", TextPrimary, InfoContainer, InfoBorder) { onAtalho(Atalho.PESQUISAS) }
                    Indicador(R.drawable.ic_umbrella_off, ui.feriasPendentes, "Férias\nPendentes", Warning, WarningContainer, WarningBorder) { onAtalho(Atalho.FERIAS) }
                    if (ui.exibirFeedbacks) Indicador(R.drawable.ic_message_circle, ui.feedbacksNovos, "Novo\nFeedback", Success, SuccessContainer, Success.copy(alpha = 0.19f)) { onAtalho(Atalho.FEEDBACKS) }
                }
                Spacer(Modifier.height(28.dp))
                SectionTitle("Acessos rápidos", Modifier.padding(horizontal = 26.dp))
                Spacer(Modifier.height(8.dp))
                AtalhosGrid(onAtalho)
                Spacer(Modifier.height(24.dp))
                SectionTitle("Comunicados", Modifier.padding(horizontal = 26.dp))
                Spacer(Modifier.height(6.dp))
                Column(Modifier.padding(horizontal = 26.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ui.comunicados.isEmpty()) Text("Nenhum comunicado no momento.", fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
                    ui.comunicados.take(2).forEach { c ->
                        AvalonCard(onClick = onComunicados) {
                            Text(c.titulo, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(R.drawable.ic_megaphone), null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(14.dp))
                                Text(c.detalhe, fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Saudacao(texto: String, modifier: Modifier = Modifier) {
    var tamanho by remember(texto) { mutableStateOf(36.sp) }
    var pronto by remember(texto) { mutableStateOf(false) }
    Text(
        texto,
        modifier = modifier.drawWithContent { if (pronto) drawContent() },
        fontFamily = Actor,
        fontSize = tamanho,
        lineHeight = (tamanho.value * 1.17f).sp,
        color = Navy,
        maxLines = 1,
        softWrap = false,
        overflow = if (tamanho.value > 20f) TextOverflow.Clip else TextOverflow.Ellipsis,
        onTextLayout = { layout ->
            if (layout.didOverflowWidth && tamanho.value > 20f) tamanho = (tamanho.value - 1f).sp
            else pronto = true
        }
    )
}

@Composable
private fun Indicador(icon: Int, valor: Int, rotulo: String, cor: Color, fundo: Color, borda: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(168.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = fundo),
        border = BorderStroke(1.dp, borda)
    ) {
        Column(Modifier.fillMaxSize().padding(top = 16.dp, bottom = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painterResource(icon), null, tint = cor, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(6.dp))
            Text("$valor", fontFamily = Inter, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = cor)
            Text(rotulo, fontFamily = Actor, fontSize = 20.sp, lineHeight = 24.sp, textAlign = TextAlign.Center, color = cor)
        }
    }
}

@Composable
private fun AtalhosGrid(onAtalho: (Atalho) -> Unit) {
    GradeFixa(Atalho.entries, colunas = 4, espaco = 10.dp, modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp)) { atalho ->
            Card(
                onClick = { onAtalho(atalho) },
                modifier = Modifier.weight(1f).height(78.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painterResource(atalho.icon), null, tint = TextPrimary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(atalho.label, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = TextPrimary, maxLines = 1)
                }
            }
    }
}
