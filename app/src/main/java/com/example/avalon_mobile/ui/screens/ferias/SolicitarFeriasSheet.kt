package com.example.avalon_mobile.ui.screens.ferias

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avalon_mobile.ui.components.AvalonTextField
import com.example.avalon_mobile.ui.theme.Danger
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.StatusApprovedText
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.util.Datas
import com.example.avalon_mobile.viewmodel.SolicitacaoState
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Formulário de solicitação de férias (o protótipo só tem o botão; os campos seguem o contrato `FeriasInput`).
 * Usa apenas componentes estáveis: AlertDialog do Compose e o DatePickerDialog nativo do Android.
 */
@Composable
fun SolicitarFeriasSheet(
    state: SolicitacaoState,
    onEnviar: (LocalDate, LocalDate, String) -> Unit,
    onFechar: () -> Unit
) {
    val context = LocalContext.current
    var inicioIso by rememberSaveable { mutableStateOf<String?>(null) }
    var fimIso by rememberSaveable { mutableStateOf<String?>(null) }
    var observacao by rememberSaveable { mutableStateOf("") }
    val inicio = inicioIso?.let(LocalDate::parse)
    val fim = fimIso?.let(LocalDate::parse)

    fun escolherData(inicial: LocalDate, minima: LocalDate, aoEscolher: (LocalDate) -> Unit) {
        DatePickerDialog(
            context,
            { _, ano, mes, dia -> aoEscolher(LocalDate.of(ano, mes + 1, dia)) },
            inicial.year, inicial.monthValue - 1, inicial.dayOfMonth
        ).apply {
            datePicker.minDate = minima.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.show()
    }

    val amanha = LocalDate.now().plusDays(1)

    AlertDialog(
        onDismissRequest = onFechar,
        containerColor = Color.White,
        title = {
            Text(
                if (state is SolicitacaoState.Enviada) "Solicitação enviada" else "Solicitar férias",
                fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary
            )
        },
        text = {
            if (state is SolicitacaoState.Enviada) {
                Text("Seu pedido segue para aprovação do gestor e, depois, do RH. Você será notificado.", fontFamily = Inter, color = StatusApprovedText)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoData("Início", inicio, Modifier.weight(1f)) {
                            escolherData(inicio ?: amanha, amanha) { d ->
                                inicioIso = d.toString()
                                if (fim != null && fim.isBefore(d)) fimIso = null
                            }
                        }
                        CampoData("Fim", fim, Modifier.weight(1f)) {
                            val minimo = inicio ?: amanha
                            escolherData(fim ?: minimo.plusDays(4), minimo) { d -> fimIso = d.toString() }
                        }
                    }
                    if (inicio != null && fim != null) {
                        Text("${ChronoUnit.DAYS.between(inicio, fim) + 1} dias selecionados", fontFamily = Inter, fontWeight = FontWeight.SemiBold, color = Navy)
                    }
                    AvalonTextField("Observação (opcional)", observacao, { observacao = it.take(255) }, singleLine = false)
                    if (state is SolicitacaoState.Erro) Text(state.mensagem, color = Danger, fontFamily = Inter, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            if (state is SolicitacaoState.Enviada) {
                TextButton(onClick = onFechar) { Text("Fechar", color = Navy, fontFamily = Inter) }
            } else {
                TextButton(
                    onClick = { if (inicio != null && fim != null) onEnviar(inicio, fim, observacao) },
                    enabled = inicio != null && fim != null && state !is SolicitacaoState.Enviando
                ) { Text(if (state is SolicitacaoState.Enviando) "Enviando…" else "Enviar", color = Navy, fontFamily = Inter) }
            }
        },
        dismissButton = {
            if (state !is SolicitacaoState.Enviada) TextButton(onClick = onFechar) { Text("Cancelar", color = TextSecondary, fontFamily = Inter) }
        }
    )
}

@Composable
private fun CampoData(rotulo: String, data: LocalDate?, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier) {
        Text(rotulo, fontFamily = Inter, fontSize = 12.sp, color = TextSecondary)
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
            Text(data?.let { Datas.dataCurta(it.toString()) } ?: "Selecionar", fontFamily = Inter, color = TextPrimary)
        }
    }
}
