package com.example.avalon_mobile.ui.screens.ferias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avalon_mobile.ui.components.AvalonTextField
import com.example.avalon_mobile.ui.components.ConfirmacaoEnvioDialog
import com.example.avalon_mobile.ui.components.bordaDialogo
import com.example.avalon_mobile.ui.theme.BorderSoft
import com.example.avalon_mobile.ui.theme.Danger
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.Surface
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextMuted
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.util.Datas
import com.example.avalon_mobile.viewmodel.SolicitacaoState
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Formulário de solicitação de férias (o protótipo só tem o botão; os campos seguem o contrato `FeriasInput`).
 * O período (início e fim) é escolhido de uma vez em um único calendário (DateRangePicker do Material 3).
 */
@Composable
fun SolicitarFeriasSheet(
    state: SolicitacaoState,
    saldoDias: Int?,
    onEnviar: (LocalDate, LocalDate, String) -> Unit,
    onFechar: () -> Unit
) {
    var inicioIso by rememberSaveable { mutableStateOf<String?>(null) }
    var fimIso by rememberSaveable { mutableStateOf<String?>(null) }
    var observacao by rememberSaveable { mutableStateOf("") }
    val inicio = inicioIso?.let(LocalDate::parse)
    val fim = fimIso?.let(LocalDate::parse)

    var calendarioAberto by rememberSaveable { mutableStateOf(false) }
    val amanha = LocalDate.now().plusDays(1)

    if (state is SolicitacaoState.Enviada) {
        ConfirmacaoEnvioDialog(
            titulo = "Solicitação enviada",
            mensagem = "Seu pedido segue para aprovação do gestor e, depois, do RH. Você será notificado.",
            onFechar = onFechar
        )
        return
    }

    if (calendarioAberto) {
        CalendarioPeriodo(
            inicio = inicio,
            fim = fim,
            primeiroDiaPermitido = amanha,
            onConfirmar = { i, f -> inicioIso = i.toString(); fimIso = f.toString(); calendarioAberto = false },
            onCancelar = { calendarioAberto = false }
        )
    }

    AlertDialog(
        modifier = Modifier.bordaDialogo(),
        onDismissRequest = onFechar,
        containerColor = Surface,
        title = { Text("Solicitar férias", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoPeriodo(inicio, fim) { calendarioAberto = true }
                if (inicio != null && fim != null) {
                    val dias = ChronoUnit.DAYS.between(inicio, fim) + 1
                    // Vermelho quando o período pedido passa do saldo disponível (o envio também é barrado no ViewModel).
                    val acimaDoSaldo = saldoDias != null && dias > saldoDias
                    Text("$dias dias selecionados", fontFamily = Inter, fontWeight = FontWeight.SemiBold, color = if (acimaDoSaldo) Danger else Navy)
                }
                AvalonTextField("Observação (opcional)", observacao, { observacao = it.take(255) }, singleLine = false)
                if (state is SolicitacaoState.Erro) Text(state.mensagem, color = Danger, fontFamily = Inter, fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (inicio != null && fim != null) onEnviar(inicio, fim, observacao) },
                enabled = inicio != null && fim != null && state !is SolicitacaoState.Enviando
            ) { Text(if (state is SolicitacaoState.Enviando) "Enviando…" else "Enviar", color = Navy, fontFamily = Inter) }
        },
        dismissButton = {
            TextButton(onClick = onFechar) { Text("Cancelar", color = TextSecondary, fontFamily = Inter) }
        }
    )
}

@Composable
private fun CampoPeriodo(inicio: LocalDate?, fim: LocalDate?, onClick: () -> Unit) {
    Column {
        Text("Período", fontFamily = Inter, fontSize = 12.sp, color = TextSecondary)
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
            val texto = if (inicio != null && fim != null) "${Datas.dataCurta(inicio.toString())}  a  ${Datas.dataCurta(fim.toString())}"
            else "Selecionar período"
            Text(texto, fontFamily = Inter, color = TextPrimary)
        }
    }
}

/**
 * Calendário único com seleção de intervalo: toque na data inicial e, em seguida, na final.
 * As datas do componente vêm em UTC (meia-noite); a conversão usa [ZoneOffset.UTC] para não deslocar o dia.
 * Dias anteriores a [primeiroDiaPermitido] ficam desabilitados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarioPeriodo(
    inicio: LocalDate?,
    fim: LocalDate?,
    primeiroDiaPermitido: LocalDate,
    onConfirmar: (LocalDate, LocalDate) -> Unit,
    onCancelar: () -> Unit
) {
    fun LocalDate.emMillis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    fun Long.emData() = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

    val estado = rememberDateRangePickerState(
        initialSelectedStartDateMillis = inicio?.emMillis(),
        initialSelectedEndDateMillis = fim?.emMillis(),
        initialDisplayedMonthMillis = (inicio ?: primeiroDiaPermitido).emMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = !utcTimeMillis.emData().isBefore(primeiroDiaPermitido)
            override fun isSelectableYear(year: Int) = year >= primeiroDiaPermitido.year
        }
    )
    val inicioEscolhido = estado.selectedStartDateMillis?.emData()
    val fimEscolhido = estado.selectedEndDateMillis?.emData()

    // O tema do app não define surfaceContainerHigh/secondaryContainer, que o calendário usa por padrão (lilás):
    // as cores são passadas explicitamente, para o corpo, o rodapé e o intervalo seguirem as 4 paletas.
    val cores = DatePickerDefaults.colors(
        containerColor = Surface,
        titleContentColor = TextPrimary,
        headlineContentColor = TextPrimary,
        weekdayContentColor = TextSecondary,
        subheadContentColor = TextSecondary,
        navigationContentColor = TextPrimary,
        dayContentColor = TextPrimary,
        disabledDayContentColor = TextMuted,
        selectedDayContainerColor = Navy,
        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
        todayContentColor = TextPrimary,
        todayDateBorderColor = Navy,
        dayInSelectionRangeContainerColor = Navy.copy(alpha = 0.16f),
        dayInSelectionRangeContentColor = TextPrimary,
        dividerColor = BorderSoft
    )

    DatePickerDialog(
        onDismissRequest = onCancelar,
        colors = cores,
        confirmButton = {
            TextButton(
                onClick = { if (inicioEscolhido != null && fimEscolhido != null) onConfirmar(inicioEscolhido, fimEscolhido) },
                enabled = inicioEscolhido != null && fimEscolhido != null
            ) { Text("Confirmar", color = Navy, fontFamily = Inter) }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar", color = TextSecondary, fontFamily = Inter) } }
    ) {
        DateRangePicker(
            state = estado,
            modifier = Modifier.height(500.dp),
            colors = cores,
            title = { Text("Selecione o período", fontFamily = Inter, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.padding(start = 24.dp, top = 16.dp)) },
            // Cabeçalho próprio: o padrão do Material 3 quebra "Data de término" em duas linhas em telas estreitas.
            headline = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        inicioEscolhido?.let { Datas.dataCurta(it.toString()) } ?: "Data de início",
                        modifier = Modifier.weight(1f), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis,
                        fontFamily = Inter, fontSize = 15.sp, color = TextPrimary
                    )
                    Text("–", fontFamily = Inter, fontSize = 15.sp, color = TextSecondary)
                    Text(
                        fimEscolhido?.let { Datas.dataCurta(it.toString()) } ?: "Data de término",
                        modifier = Modifier.weight(1f), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis,
                        fontFamily = Inter, fontSize = 15.sp, color = TextPrimary
                    )
                }
            },
            showModeToggle = false
        )
    }
}
