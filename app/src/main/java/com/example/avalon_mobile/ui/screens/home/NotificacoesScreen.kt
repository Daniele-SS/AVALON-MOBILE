package com.example.avalon_mobile.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.R
import com.example.avalon_mobile.model.Notificacao
import com.example.avalon_mobile.ui.components.AvalonCard
import com.example.avalon_mobile.ui.components.AvalonTopBar
import com.example.avalon_mobile.ui.components.EmptyState
import com.example.avalon_mobile.ui.components.IconTile
import com.example.avalon_mobile.ui.components.LoadStateContent
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.BadgeBlueBg
import com.example.avalon_mobile.ui.theme.BorderSoft
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Link
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.viewmodel.ComunicadosViewModel
import com.example.avalon_mobile.viewmodel.NotificacoesViewModel
import com.example.avalon_mobile.viewmodel.avalonViewModel

/** Central de notificações (Sprint 4 — 6.3.4). Toque leva ao módulo de origem. */
@Composable
fun NotificacoesScreen(
    onBack: () -> Unit,
    onAbrirOrigem: (tipoOrigem: String?) -> Unit,
    viewModel: NotificacoesViewModel = avalonViewModel { c, _ -> NotificacoesViewModel(c.avalonRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Notificações", onBack = onBack)
        LoadStateContent(state, onRetry = viewModel::carregar) { grupos ->
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (grupos.isEmpty()) item { EmptyState("Você não tem notificações.") }
                grupos.forEach { (grupo, lista) ->
                    item(key = "g$grupo") {
                        Text(grupo, Modifier.padding(top = 4.dp), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextSecondary)
                    }
                    items(lista, key = { it.id }) { NotificacaoCard(it) { onAbrirOrigem(it.tipoOrigem) } }
                }
            }
        }
    }
}

private fun iconeOrigem(tipo: String?): Int = when (tipo?.uppercase()) {
    "PESQUISA" -> R.drawable.ic_clipboard_list
    "AVALIACAO" -> R.drawable.ic_shield
    "FERIAS" -> R.drawable.ic_calendar
    "FEEDBACK" -> R.drawable.ic_message_circle
    "BENEFICIO" -> R.drawable.ic_gift
    else -> R.drawable.ic_file_text
}

@Composable
private fun NotificacaoCard(n: Notificacao, onClick: () -> Unit) {
    AvalonCard(onClick = onClick, border = BorderSoft, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.Top) {
            IconTile(iconeOrigem(n.tipoOrigem), Navy, BadgeBlueBg)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(n.titulo, Modifier.weight(1f), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    if (n.lida == false) Box(Modifier.size(8.dp).background(Link, CircleShape))
                }
                n.mensagem?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
                }
                n.dataHora?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, fontFamily = Inter, fontSize = 11.sp, color = TextSecondary)
                }
            }
        }
    }
}

/** Mural de avisos (Sprint 4 — 6.3.2). */
@Composable
fun ComunicadosScreen(
    onBack: () -> Unit,
    viewModel: ComunicadosViewModel = avalonViewModel { c, _ -> ComunicadosViewModel(c.avalonRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Mural de avisos", onBack = onBack)
        LoadStateContent(state, onRetry = viewModel::carregar) { lista ->
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (lista.isEmpty()) item { EmptyState("Nenhum comunicado no momento.") }
                items(lista, key = { it.id }) { c ->
                    AvalonCard {
                        Text(c.titulo, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text(c.detalhe, fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
