package com.example.avalon_mobile.ui.screens.ferias

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.R
import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.SituacaoFerias
import com.example.avalon_mobile.ui.components.AvalonCard
import com.example.avalon_mobile.ui.components.AvalonPrimaryButton
import com.example.avalon_mobile.ui.components.AvalonTopBar
import com.example.avalon_mobile.ui.components.EmptyState
import com.example.avalon_mobile.ui.components.LoadStateContent
import com.example.avalon_mobile.ui.components.StatusPill
import com.example.avalon_mobile.ui.theme.Accent
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.BorderStrong
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.NavyLight
import com.example.avalon_mobile.ui.theme.StatusApprovedBg
import com.example.avalon_mobile.ui.theme.StatusApprovedText
import com.example.avalon_mobile.ui.theme.StatusDeniedBg
import com.example.avalon_mobile.ui.theme.StatusDeniedText
import com.example.avalon_mobile.ui.theme.StatusPendingBg
import com.example.avalon_mobile.ui.theme.StatusPendingText
import com.example.avalon_mobile.ui.theme.TextMutedAlpha
import com.example.avalon_mobile.util.Datas
import com.example.avalon_mobile.util.SaldoFerias
import com.example.avalon_mobile.viewmodel.FeriasViewModel
import com.example.avalon_mobile.viewmodel.avalonViewModel

@Composable
fun FeriasScreen(
    onBack: () -> Unit,
    viewModel: FeriasViewModel = avalonViewModel { c, _ -> FeriasViewModel(c.avalonRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val solicitacao by viewModel.solicitacao.collectAsStateWithLifecycle()
    var mostrarFormulario by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Férias", onBack = onBack)
        LoadStateContent(state, onRetry = viewModel::carregar) { ui ->
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { CartaoSaldo(ui.saldo) }
                item {
                    AvalonPrimaryButton(
                        "Solicitar férias",
                        onClick = { viewModel.resetSolicitacao(); mostrarFormulario = true },
                        container = NavyLight,
                        leadingIcon = R.drawable.ic_calendar_plus,
                        enabled = ui.saldo?.diasDisponiveis != 0
                    )
                }
                item { Text("Minhas solicitações", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextMutedAlpha) }
                if (ui.pedidos.isEmpty()) item { EmptyState("Você ainda não fez solicitações de férias.") }
                items(ui.pedidos, key = { it.id }) { PedidoCard(it) }
            }
        }
    }

    if (mostrarFormulario) {
        SolicitarFeriasSheet(
            state = solicitacao,
            onEnviar = viewModel::solicitar,
            onFechar = { mostrarFormulario = false; viewModel.resetSolicitacao() }
        )
    }
}

@Composable
private fun CartaoSaldo(saldo: SaldoFerias?) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Navy, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Text("Saldo disponível", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Accent)
        Text(
            saldo?.let { "${it.diasDisponiveis} dias" } ?: "Em aquisição",
            fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 40.sp, color = Color.White
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 2.dp, color = Accent)
        Row {
            Column(Modifier.weight(1f)) {
                Text("Período aquisitivo", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Accent)
                Spacer(Modifier.height(16.dp))
                Text(
                    saldo?.let { "${Datas.mesAno(it.aquisitivoInicio)} - ${Datas.mesAno(it.aquisitivoFim)}" } ?: "—",
                    fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Limite p/ férias", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Accent)
                Spacer(Modifier.height(16.dp))
                Text(saldo?.let { Datas.mesAno(it.limiteGozo) } ?: "—", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun PedidoCard(pedido: Ferias) {
    val (bg, fg) = when (pedido.situacao) {
        SituacaoFerias.EM_ANALISE -> StatusPendingBg to StatusPendingText
        SituacaoFerias.APROVADO -> StatusApprovedBg to StatusApprovedText
        SituacaoFerias.NEGADO -> StatusDeniedBg to StatusDeniedText
    }
    AvalonCard(radius = 20.dp, border = BorderStrong, contentPadding = 17.dp) {
        Row(Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${pedido.quantidadeDias ?: "—"} dias - ${Datas.intervalo(pedido.dataInicio, pedido.dataFim)}",
                    fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black
                )
                pedido.dataSolicitacao?.let {
                    Text("Solicitado em ${Datas.diaMes(it)}", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextMutedAlpha)
                }
                // Etapa intermediária do fluxo em duas etapas.
                if (pedido.status == "Aprovado pelo Gestor") {
                    Text("Aprovado pelo gestor • aguardando RH", fontFamily = Inter, fontSize = 12.sp, color = TextMutedAlpha)
                }
            }
            StatusPill(pedido.situacao.rotulo, bg, fg, fontSize = 14)
        }
    }
}
