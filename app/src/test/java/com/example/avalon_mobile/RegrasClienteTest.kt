package com.example.avalon_mobile

import com.example.avalon_mobile.model.Colaborador
import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.SituacaoFerias
import com.example.avalon_mobile.util.Datas
import com.example.avalon_mobile.util.SaldoFerias
import com.example.avalon_mobile.viewmodel.JornadaViewModel
import com.example.avalon_mobile.viewmodel.RedefinirSenhaViewModel
import com.example.avalon_mobile.viewmodel.TipoDia
import com.example.avalon_mobile.model.DiaSemana
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RegrasClienteTest {

    @Test
    fun `status de ferias do contrato vira o rotulo do prototipo`() {
        assertEquals(SituacaoFerias.EM_ANALISE, SituacaoFerias.from("Pendente"))
        assertEquals(SituacaoFerias.EM_ANALISE, SituacaoFerias.from("Aprovado pelo Gestor"))
        assertEquals(SituacaoFerias.APROVADO, SituacaoFerias.from("Aprovado pelo RH"))
        assertEquals(SituacaoFerias.NEGADO, SituacaoFerias.from("Recusado pelo Gestor"))
        assertEquals(SituacaoFerias.NEGADO, SituacaoFerias.from("Recusado pelo RH"))
    }

    @Test
    fun `saldo desconta pedidos nao recusados do periodo concessivo`() {
        val pedidos = listOf(
            Ferias(1, "2026-12-15", "2026-12-24", 10, status = "Pendente"),
            Ferias(2, "2026-06-10", "2026-06-24", 15, status = "Aprovado pelo RH"),
            Ferias(3, "2026-03-02", "2026-03-06", 5, status = "Recusado pelo Gestor"),
            Ferias(4, "2025-12-01", "2025-12-10", 10, status = "Aprovado pelo RH") // período anterior
        )
        val saldo = SaldoFerias.calcular(LocalDate.of(2025, 3, 10), pedidos, hoje = LocalDate.of(2026, 10, 15))
        assertNotNull(saldo)
        assertEquals(5, saldo!!.diasDisponiveis)
        assertEquals(LocalDate.of(2025, 3, 10), saldo.aquisitivoInicio)
        assertEquals(LocalDate.of(2026, 3, 10), saldo.aquisitivoFim)
        assertEquals(LocalDate.of(2027, 3, 10), saldo.limiteGozo)
    }

    @Test
    fun `sem periodo aquisitivo completo nao ha saldo`() {
        assertNull(SaldoFerias.calcular(LocalDate.of(2026, 1, 5), emptyList(), hoje = LocalDate.of(2026, 10, 15)))
    }

    @Test
    fun `semana da escala marca hoje, trabalho e folga`() {
        val dias = listOf("SEG", "TER", "QUA", "QUI", "SEX").mapIndexed { i, s -> DiaSemana(i, s, s, true) } +
            DiaSemana(6, "Sábado", "SAB", false) + DiaSemana(7, "Domingo", "DOM", false)
        val semana = JornadaViewModel.montarSemana(dias, hoje = LocalDate.of(2026, 10, 15)) // quinta
        assertEquals(LocalDate.of(2026, 10, 12), semana.first().data)
        assertEquals(TipoDia.HOJE, semana[3].tipo)
        assertEquals(TipoDia.TRABALHO, semana[0].tipo)
        assertEquals(TipoDia.FOLGA, semana[5].tipo)
        assertEquals(TipoDia.FOLGA, semana[6].tipo)
    }

    @Test
    fun `politica minima de senha`() {
        assertNotNull(RedefinirSenhaViewModel.validarPolitica("abc12"))
        assertNotNull(RedefinirSenhaViewModel.validarPolitica("somenteletras"))
        assertNotNull(RedefinirSenhaViewModel.validarPolitica("12345678"))
        assertNull(RedefinirSenhaViewModel.validarPolitica("N0vaSenha2026"))
    }

    @Test
    fun `cpf mascarado e iniciais`() {
        val c = Colaborador(1, 1, 1, 1, "AV-1", "Ana Lucia Souza", "123.482.789-10", email = "a@b.c", dataAdmissao = "2025-03-10", status = "Ativo")
        assertEquals("•••.482.•••-10", c.cpfMascarado)
        assertEquals("AS", c.iniciais)
    }

    @Test
    fun `intervalo de datas no formato do prototipo`() {
        assertEquals("15 a 24/12", Datas.intervalo("2026-12-15", "2026-12-24"))
        assertEquals("28/12 a 05/01", Datas.intervalo("2026-12-28", "2027-01-05"))
        assertEquals("08:00", Datas.hora("08:00:00"))
    }
}
