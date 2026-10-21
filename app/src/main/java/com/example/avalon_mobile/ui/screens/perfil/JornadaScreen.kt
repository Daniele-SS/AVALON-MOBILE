package com.example.avalon_mobile.ui.screens.perfil

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.ui.components.EmptyState
import com.example.avalon_mobile.ui.components.AvalonCard
import com.example.avalon_mobile.ui.components.AvalonTopBar
import com.example.avalon_mobile.ui.components.LoadStateContent
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.DayOff
import com.example.avalon_mobile.ui.theme.DayOffText
import com.example.avalon_mobile.ui.theme.DayWork
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TabInactive
import com.example.avalon_mobile.ui.theme.TextMuted
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.util.Datas
import com.example.avalon_mobile.viewmodel.DiaEscala
import com.example.avalon_mobile.viewmodel.JornadaViewModel
import com.example.avalon_mobile.viewmodel.TipoDia
import com.example.avalon_mobile.viewmodel.avalonViewModel

/** Consulta de jornada e escala (Sprint 2 — 6.5.3). Somente leitura: a edição é exclusiva do RH. */
@Composable
fun JornadaScreen(
    onBack: () -> Unit,
    viewModel: JornadaViewModel = avalonViewModel { c, _ -> JornadaViewModel(c.avalonRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Jornada e escala", onBack = onBack)
        LoadStateContent(state, onRetry = viewModel::carregar) { ui ->
            val jornada = ui.jornada
            if (jornada == null) {
                EmptyState("Nenhuma jornada vinculada ao seu cadastro. Procure o RH.")
                return@LoadStateContent
            }
            val horario = "${Datas.hora(jornada.horaInicio)} — ${Datas.hora(jornada.horaFim)}"
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("ESTA SEMANA", fontFamily = Inter, fontSize = 18.sp, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ui.semana.forEach { DiaChip(it, Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Legenda(DayWork, "Trabalha")
                        Legenda(Navy, "Hoje")
                        Legenda(DayOff, "Folga")
                    }
                }
                item { Text("Horários da semana", fontFamily = Inter, fontSize = 18.sp, color = TextPrimary) }
                items(ui.semana, key = { it.data.toString() }) { dia ->
                    AvalonCard {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text("${Datas.nomeDia(dia.data.dayOfWeek)}, ${dia.data.dayOfMonth}", fontFamily = Inter, fontSize = 15.sp, color = TextPrimary)
                                Text(jornada.nome, fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
                            }
                            Text(if (dia.trabalha) horario else "Folga", fontFamily = Inter, fontSize = 13.sp, color = Navy)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaChip(dia: DiaEscala, modifier: Modifier) {
    val (bg, fg) = when (dia.tipo) {
        TipoDia.HOJE -> Navy to Color.White
        TipoDia.TRABALHO -> DayWork to TextPrimary
        TipoDia.FOLGA -> DayOff to DayOffText
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            if (dia.sigla == "SAB") "SÁB" else dia.sigla,
            fontFamily = Inter, fontSize = 10.sp,
            color = if (dia.tipo == TipoDia.HOJE) TabInactive else TextSecondary
        )
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(36.dp).background(bg, RoundedCornerShape(5.dp)), contentAlignment = Alignment.Center) {
            Text("${dia.data.dayOfMonth}", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = fg)
        }
    }
}

@Composable
private fun Legenda(cor: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(cor, RoundedCornerShape(1.dp)))
        Spacer(Modifier.width(4.dp))
        Text(texto, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextMuted)
    }
}
