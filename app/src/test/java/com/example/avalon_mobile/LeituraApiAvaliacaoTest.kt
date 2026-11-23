package com.example.avalon_mobile

import com.example.avalon_mobile.data.remote.LeituraApi
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/** Formato real devolvido pelo Back-end em 23/11/2026: listagens abertas, com relações aninhadas. */
class LeituraApiAvaliacaoTest {

    private val hoje = LocalDate.of(2026, 11, 23)

    private fun json(texto: String) = JsonParser.parseString(texto)

    private fun envelope(chave: String, itens: List<String>) =
        json("""{"status":true,"status_code":200,"response":{"count":${itens.size},"$chave":[${itens.joinToString(",")}]}}""")

    private fun av(id: Int, inicio: String, fim: String, status: Int = 0, publico: String = "TODOS") =
        """{"id":$id,"titulo":"Avaliação $id","descricao":"Descrição $id","data_inicio":"${inicio}T03:00:00.000Z","data_fim":"${fim}T03:00:00.000Z","anonimo":1,"status":$status,"publico_selecionado":"$publico"}"""

    private fun vinculoCargo(id: Int, avaliacao: Int, cargo: Int) =
        """{"id":$id,"avaliacao":${av(avaliacao, "2026-11-01", "2026-11-30", publico = "CARGO")},"cargo":{"id":$cargo,"nome":"Cargo $cargo"}}"""

    private fun vinculoSetor(id: Int, avaliacao: Int, setor: Int) =
        """{"id":$id,"avaliacao":${av(avaliacao, "2026-11-01", "2026-11-30", publico = "SETOR")},"setor":[{"id":$setor,"nome":"Setor $setor"}]}"""

    private fun avaliacoes(itens: List<String>, cargos: List<String> = emptyList(), setores: List<String> = emptyList(), idCargo: Int? = 2, idSetor: Int? = 2, respondidas: Set<Int> = emptySet()) =
        LeituraApi.avaliacoes(
            envelope("avaliacoes", itens),
            envelope("avaliacaoCargos", cargos),
            envelope("avaliacao_setor", setores),
            idCargo, idSetor, respondidas, hoje
        )!!

    @Test
    fun `publico TODOS, em aberto (status 0) e no prazo, aparece`() {
        val r = avaliacoes(listOf(av(1, "2026-11-20", "2026-12-20")))
        assertEquals(listOf(1), r.map { it.id })
        assertEquals("Avaliação 1", r.single().titulo)
        assertEquals("2026-12-20", r.single().dataFim)
    }

    @Test
    fun `encerrada (status 1), que ainda nao comecou ou vencida nao aparece`() {
        val r = avaliacoes(listOf(
            av(1, "2026-11-01", "2026-12-01", status = 1),
            av(2, "2026-11-24", "2026-12-20"),
            av(3, "2026-10-01", "2026-11-22")
        ))
        assertEquals(emptyList<Int>(), r.map { it.id })
    }

    @Test
    fun `no primeiro e no ultimo dia ainda vale`() {
        val r = avaliacoes(listOf(av(1, "2026-11-23", "2026-12-20"), av(2, "2026-11-01", "2026-11-23")))
        assertEquals(listOf(2, 1), r.map { it.id })
    }

    @Test
    fun `ja enviada neste aparelho nao aparece`() {
        val r = avaliacoes(listOf(av(1, "2026-11-20", "2026-12-20"), av(2, "2026-11-20", "2026-12-21")), respondidas = setOf(1))
        assertEquals(listOf(2), r.map { it.id })
    }

    @Test
    fun `publico CARGO so para quem tem o cargo`() {
        val itens = listOf(av(5, "2026-11-01", "2026-11-30", publico = "CARGO"))
        val cargos = listOf(vinculoCargo(1, 5, cargo = 2))
        assertEquals(listOf(5), avaliacoes(itens, cargos = cargos, idCargo = 2).map { it.id })
        assertEquals(emptyList<Int>(), avaliacoes(itens, cargos = cargos, idCargo = 3).map { it.id })
        assertEquals(emptyList<Int>(), avaliacoes(itens, cargos = cargos, idCargo = null).map { it.id })
    }

    @Test
    fun `publico SETOR le o setor aninhado em lista`() {
        val itens = listOf(av(6, "2026-11-01", "2026-11-30", publico = "SETOR"))
        val setores = listOf(vinculoSetor(1, 6, setor = 2))
        assertEquals(listOf(6), avaliacoes(itens, setores = setores, idSetor = 2).map { it.id })
        assertEquals(emptyList<Int>(), avaliacoes(itens, setores = setores, idSetor = 1).map { it.id })
    }

    @Test
    fun `segmentada sem lista de cargos legivel fica de fora`() {
        val r = LeituraApi.avaliacoes(envelope("avaliacoes", listOf(av(5, "2026-11-01", "2026-11-30", publico = "CARGO"))), null, null, 2, 2, emptySet(), hoje)!!
        assertEquals(emptyList<Int>(), r.map { it.id })
    }

    @Test
    fun `mais proxima do prazo primeiro`() {
        val r = avaliacoes(listOf(av(1, "2026-11-01", "2026-12-30"), av(2, "2026-11-01", "2026-12-05")))
        assertEquals(listOf(2, 1), r.map { it.id })
    }

    @Test
    fun `resposta que nao e lista devolve nulo`() {
        assertNull(LeituraApi.avaliacoes(json("""{"response":{"count":0}}"""), null, null, 2, 2, emptySet(), hoje))
    }

    private fun fator(id: Int, avaliacao: Int, fatorId: Int) =
        """{"id":$id,"avaliacao":${av(avaliacao, "2026-11-01", "2026-11-30")},"psicossocial_fator":{"id":$fatorId,"nome":"Fator $fatorId"}}"""

    private fun pergunta(id: Int, fatorId: Int, ordem: Int, ativo: Int = 1, obrigatorio: Int = 1) =
        """{"id":$id,"enunciado":"Pergunta $id","tipo":"ESCALA","ordem":$ordem,"obrigatorio":$obrigatorio,"ativo":$ativo,"psicossocial_fator":{"id":$fatorId,"nome":"Fator $fatorId"}}"""

    @Test
    fun `perguntas - so dos fatores da avaliacao, ativas, por fator e ordem`() {
        val fatores = envelope("avaliacaoFatores", listOf(fator(1, 7, 9), fator(2, 7, 3), fator(3, 8, 4)))
        val perguntas = envelope("avaliacaoPerguntas", listOf(
            pergunta(10, fatorId = 9, ordem = 2),
            pergunta(11, fatorId = 9, ordem = 1),
            pergunta(12, fatorId = 3, ordem = 1),
            pergunta(13, fatorId = 4, ordem = 1),
            pergunta(14, fatorId = 9, ordem = 3, ativo = 0),
            pergunta(15, fatorId = 5, ordem = 1)
        ))
        val r = LeituraApi.perguntasAvaliacao(fatores, perguntas, 7)!!
        assertEquals(listOf(12, 11, 10), r.map { it.id })
        assertEquals(true, r.first().obrigatorio)
        assertEquals("ESCALA", r.first().tipo)
    }

    @Test
    fun `perguntas - avaliacao sem fatores fica vazia`() {
        val r = LeituraApi.perguntasAvaliacao(envelope("avaliacaoFatores", listOf(fator(1, 7, 9))), envelope("avaliacaoPerguntas", listOf(pergunta(10, 9, 1))), 99)!!
        assertEquals(emptyList<Int>(), r.map { it.id })
    }

    @Test
    fun `perguntas - 72 viram 12 em rodizio por fator`() {
        val fatores = envelope("avaliacaoFatores", (1..9).map { fator(it, 7, it) })
        val perguntas = envelope("avaliacaoPerguntas", (1..9).flatMap { f -> (1..8).map { o -> pergunta(f * 100 + o, f, o) } })
        val r = LeituraApi.perguntasAvaliacao(fatores, perguntas, 7)!!
        assertEquals(12, r.size)
        assertEquals(9, r.map { it.idFator }.toSet().size)
        assertEquals(listOf(101, 102, 201, 202, 301, 302, 401, 501, 601, 701, 801, 901), r.map { it.id })
    }

    @Test
    fun `perguntas - ate 12 nao sao cortadas`() {
        val fatores = envelope("avaliacaoFatores", listOf(fator(1, 7, 1), fator(2, 7, 2)))
        val perguntas = envelope("avaliacaoPerguntas", (1..2).flatMap { f -> (1..6).map { o -> pergunta(f * 100 + o, f, o) } })
        assertEquals(12, LeituraApi.perguntasAvaliacao(fatores, perguntas, 7)!!.size)
    }
}
