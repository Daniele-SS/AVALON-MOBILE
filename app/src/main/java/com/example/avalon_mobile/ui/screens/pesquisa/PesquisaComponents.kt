package com.example.avalon_mobile.ui.screens.pesquisa

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avalon_mobile.R
import com.example.avalon_mobile.ui.theme.Border
import com.example.avalon_mobile.ui.theme.DayWork
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextPrimary

/** Cabeçalho dos fluxos de resposta: botão fechar (X) e título. */
@Composable
fun FluxoTopBar(titulo: String, onFechar: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Navy).statusBarsPadding().padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onFechar) {
            Icon(painterResource(R.drawable.ic_x), contentDescription = "Fechar", tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Text(titulo, Modifier.weight(1f), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Color.White)
    }
}

/** Barra de progresso segmentada (Figma: 5 segmentos de 50×5). */
@Composable
fun ProgressoSegmentado(progresso: Float, modifier: Modifier = Modifier, segmentos: Int = 5) {
    val cheios = (progresso * segmentos).let { kotlin.math.ceil(it).toInt() }.coerceIn(0, segmentos)
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(segmentos) { i ->
            Box(Modifier.weight(1f).height(5.dp).background(if (i < cheios) Navy else DayWork, RoundedCornerShape(3.dp)))
        }
    }
}

/** Opção de resposta em cartão com marcador circular (escala Likert). */
@Composable
fun OpcaoResposta(texto: String, selecionada: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .selectable(selected = selecionada, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (selecionada) Color(0xFFEAF3FF) else Color.White),
        border = BorderStroke(if (selecionada) 2.dp else 1.dp, if (selecionada) Navy else Border)
    ) {
        Row(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(texto, Modifier.weight(1f), fontFamily = Inter, fontSize = 18.sp, color = TextPrimary)
            Box(Modifier.size(25.dp).border(2.dp, if (selecionada) Navy else Border, CircleShape), contentAlignment = Alignment.Center) {
                if (selecionada) Box(Modifier.size(11.dp).background(Navy, CircleShape))
            }
        }
    }
}
