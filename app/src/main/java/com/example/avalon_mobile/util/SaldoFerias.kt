package com.example.avalon_mobile.util

import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.SituacaoFerias
import java.time.LocalDate

/**
 * Estimativa de saldo (CLT: 30 dias por período aquisitivo de 12 meses, a gozar nos 12 meses seguintes).
 * O contrato v2.1.0 não expõe saldo nem período aquisitivo; quando o Back-end expuser, este cálculo
 * deve ser substituído pelo valor do servidor, que é a regra oficial.
 */
data class SaldoFerias(
    val diasDisponiveis: Int,
    val aquisitivoInicio: LocalDate,
    val aquisitivoFim: LocalDate,
    val limiteGozo: LocalDate
) {
    companion object {
        private const val DIAS_POR_PERIODO = 30

        fun calcular(dataAdmissao: LocalDate, pedidos: List<Ferias>, hoje: LocalDate = LocalDate.now()): SaldoFerias? {
            var inicio = dataAdmissao
            if (inicio.plusYears(1).isAfter(hoje)) return null // ainda no primeiro período aquisitivo
            // Último período aquisitivo completo.
            while (!inicio.plusYears(2).isAfter(hoje)) inicio = inicio.plusYears(1)
            val fim = inicio.plusYears(1)
            val limite = fim.plusYears(1)
            val reservados = pedidos
                .filter { it.situacao != SituacaoFerias.NEGADO }
                .filter { p -> Datas.parse(p.dataInicio)?.let { !it.isBefore(fim) && it.isBefore(limite) } == true }
                .sumOf { it.quantidadeDias ?: 0 }
            return SaldoFerias((DIAS_POR_PERIODO - reservados).coerceAtLeast(0), inicio, fim, limite)
        }
    }
}
