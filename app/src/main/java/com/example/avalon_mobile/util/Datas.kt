package com.example.avalon_mobile.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object Datas {
    val ptBR: Locale = Locale.forLanguageTag("pt-BR")
    private val curta = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val diaMes = DateTimeFormatter.ofPattern("dd/MM")
    private val mesAno = DateTimeFormatter.ofPattern("MM/yyyy")

    fun parse(iso: String?): LocalDate? = iso?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() }

    fun dataCurta(iso: String?): String = parse(iso)?.format(curta) ?: "—"
    fun diaMes(iso: String?): String = parse(iso)?.format(diaMes) ?: "—"
    fun mesAno(date: LocalDate): String = date.format(mesAno)

    /** "15 a 24/12" (mesmo mês) ou "28/12 a 05/01". */
    fun intervalo(inicioIso: String, fimIso: String): String {
        val i = parse(inicioIso) ?: return "—"
        val f = parse(fimIso) ?: return "—"
        return if (i.month == f.month && i.year == f.year) "%02d a %s".format(i.dayOfMonth, f.format(diaMes))
        else "${i.format(diaMes)} a ${f.format(diaMes)}"
    }

    /** "08:00:00" → "08:00". */
    fun hora(hhmmss: String): String = runCatching { LocalTime.parse(hhmmss).toString().take(5) }.getOrDefault(hhmmss.take(5))

    fun nomeDia(day: DayOfWeek): String =
        day.getDisplayName(TextStyle.FULL, ptBR).substringBefore("-").replaceFirstChar { it.titlecase(ptBR) }

    /** Siglas do contrato (DiaSemana.dia_sigla). */
    fun sigla(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "SEG"
        DayOfWeek.TUESDAY -> "TER"
        DayOfWeek.WEDNESDAY -> "QUA"
        DayOfWeek.THURSDAY -> "QUI"
        DayOfWeek.FRIDAY -> "SEX"
        DayOfWeek.SATURDAY -> "SAB"
        DayOfWeek.SUNDAY -> "DOM"
    }
}
