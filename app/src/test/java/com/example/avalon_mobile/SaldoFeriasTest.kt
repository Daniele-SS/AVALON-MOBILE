package com.example.avalon_mobile

import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.util.SaldoFerias
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SaldoFeriasTest {
    private val hoje = LocalDate.of(2026, 11, 11)
    private val admissao = LocalDate.of(2024, 1, 10) // período 10/01/2025–10/01/2026, limite 10/01/2027

    private fun f(ini: String, fim: String, dias: Int?, status: String? = "Pendente") =
        Ferias(1, ini, fim, dias, status = status)

    @Test fun semPedidosSaldoIntegral() {
        val s = SaldoFerias.calcular(admissao, emptyList(), hoje)!!
        assertEquals(30, s.diasDisponiveis)
        assertEquals(LocalDate.of(2027, 1, 10), s.limiteGozo)
    }

    @Test fun pedidosDepoisDoLimiteContamNoSaldo() {
        // O caso reportado: 19 + 11 + 11 = 41 dias pedidos e o saldo mostrava 11.
        val pedidos = listOf(
            f("2027-01-04", "2027-01-22", 19),
            f("2027-03-23", "2027-04-02", 11),
            f("2027-05-03", "2027-05-13", 11)
        )
        assertEquals(0, SaldoFerias.calcular(admissao, pedidos, hoje)!!.diasDisponiveis)
    }

    @Test fun saldoParcial() {
        val pedidos = listOf(f("2026-12-01", "2026-12-19", 19))
        assertEquals(11, SaldoFerias.calcular(admissao, pedidos, hoje)!!.diasDisponiveis)
    }

    @Test fun pedidoNegadoNaoConsome() {
        val pedidos = listOf(f("2026-12-01", "2026-12-19", 19, "Recusado pelo RH"))
        assertEquals(30, SaldoFerias.calcular(admissao, pedidos, hoje)!!.diasDisponiveis)
    }

    @Test fun pedidoDoPeriodoAnteriorNaoConsome() {
        val pedidos = listOf(f("2025-06-01", "2025-06-20", 20))
        assertEquals(30, SaldoFerias.calcular(admissao, pedidos, hoje)!!.diasDisponiveis)
    }

    @Test fun semQuantidadeDeDiasUsaIntervaloDasDatas() {
        val p = f("2026-12-01", "2026-12-10", null)
        assertEquals(10, p.dias)
        assertEquals(20, SaldoFerias.calcular(admissao, listOf(p), hoje)!!.diasDisponiveis)
    }

    @Test fun intervaloInvalidoNaoSomaDias() {
        assertEquals(0, f("2026-12-10", "2026-12-01", null).dias)
        assertEquals(0, f("lixo", "2026-12-01", null).dias)
    }

    @Test fun saldoNuncaFicaNegativo() {
        val pedidos = listOf(f("2026-12-01", "2027-01-05", 36))
        assertEquals(0, SaldoFerias.calcular(admissao, pedidos, hoje)!!.diasDisponiveis)
    }

    @Test fun inicioPrecisaSerAnteriorAoLimite() {
        val s = SaldoFerias.calcular(admissao, emptyList(), hoje)!!
        assertTrue(s.inicioDentroDoLimite(LocalDate.of(2027, 1, 9)))
        assertFalse(s.inicioDentroDoLimite(LocalDate.of(2027, 1, 10)))
        assertFalse(s.inicioDentroDoLimite(LocalDate.of(2027, 3, 23)))
    }

    @Test fun primeiroPeriodoAindaEmAquisicao() {
        assertNull(SaldoFerias.calcular(LocalDate.of(2026, 3, 1), emptyList(), hoje))
        assertNotNull(SaldoFerias.calcular(admissao, emptyList(), hoje))
    }
}
