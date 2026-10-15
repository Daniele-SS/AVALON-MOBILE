package com.example.avalon_mobile.ui.screens.ferias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avalon_mobile.ui.components.AvalonPrimaryButton
import com.example.avalon_mobile.ui.components.AvalonTextField
import com.example.avalon_mobile.ui.theme.DayWork
import com.example.avalon_mobile.ui.theme.Danger
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.StatusApprovedText
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.viewmodel.SolicitacaoState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/** Formulário de solicitação de férias (o protótipo só tem o botão; o formulário segue o contrato `FeriasInput`). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarFeriasSheet(
    state: SolicitacaoState,
    onEnviar: (LocalDate, LocalDate, String) -> Unit,
    onFechar: () -> Unit
) {
    val hojeUtc = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    val pickerState = rememberDateRangePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis > hojeUtc
        }
    )
    var observacao by rememberSaveable { mutableStateOf("") }
    val inicio = pickerState.selectedStartDateMillis?.toLocalDate()
    val fim = pickerState.selectedEndDateMillis?.toLocalDate()

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state is SolicitacaoState.Enviada) {
                Text("Solicitação enviada", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Text("Seu pedido segue para aprovação do gestor e, depois, do RH. Você será notificado.", fontFamily = Inter, color = StatusApprovedText)
                AvalonPrimaryButton("Fechar", onClick = onFechar)
            } else {
                DateRangePicker(
                    state = pickerState,
                    modifier = Modifier.fillMaxWidth().height(440.dp),
                    title = { Text("Selecione o período", Modifier.padding(start = 16.dp, top = 8.dp), fontFamily = Inter, color = TextPrimary) },
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White,
                        selectedDayContainerColor = Navy,
                        todayDateBorderColor = Navy,
                        dayInSelectionRangeContainerColor = DayWork
                    )
                )
                if (inicio != null && fim != null) {
                    Text(
                        "${ChronoUnit.DAYS.between(inicio, fim) + 1} dias selecionados",
                        fontFamily = Inter, fontWeight = FontWeight.SemiBold, color = Navy
                    )
                }
                AvalonTextField("Observação (opcional)", observacao, { observacao = it.take(255) }, singleLine = false)
                if (state is SolicitacaoState.Erro) Text(state.mensagem, color = Danger, fontFamily = Inter, fontSize = 13.sp)
                AvalonPrimaryButton(
                    "Enviar solicitação",
                    onClick = { if (inicio != null && fim != null) onEnviar(inicio, fim, observacao) },
                    enabled = inicio != null && fim != null,
                    loading = state is SolicitacaoState.Enviando
                )
            }
        }
    }
}

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
