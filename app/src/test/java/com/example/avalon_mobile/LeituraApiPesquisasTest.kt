package com.example.avalon_mobile

import com.example.avalon_mobile.data.remote.LeituraApi
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Formato real devolvido pelo Back-end em 05/11/2026 (listagens completas, com relações aninhadas). */
class LeituraApiPesquisasTest {

    private fun json(texto: String) = JsonParser.parseString(texto)

    private val hoje = LocalDate.of(2026, 11, 5)

    private fun pesq(id: Int, inicio: String, fim: String, status: Int = 0) =
        """{"id":$id,"titulo":"Pesquisa $id","descricao":null,"data_inicio":"${inicio}T03:00:00.000Z","data_fim":"${fim}T03:00:00.000Z","anonimo":1,"status":$status,"publico_selecionado":"TODOS"}"""

    private fun colab(id: Int) = """{"id":$id,"nome":"Colaborador $id","cpf":"000"}"""

    private fun part(id: Int, idPesquisa: Int, idColab: Int, status: Int) =
        """{"id":$id,"status":$status,"pesquisa":${pesq(idPesquisa, "2026-11-01", "2026-11-30")},"colaborador":[${colab(idColab)}]}"""

    private fun perg(id: Int, idPesquisa: Int, ordem: Int, tipo: String = "ESCALA", obrig: Int = 1) =
        """{"id":$id,"enunciado":"Pergunta $id","tipo":"$tipo","ordem":$ordem,"obrigatorio":$obrig,"pesquisa":${pesq(idPesquisa, "2026-11-01", "2026-11-30")}}"""

    private fun envelope(chave: String, itens: List<String>) =
        json("""{"status":true,"status_code":200,"response":{"count":${itens.size},"$chave":[${itens.joinToString(",")}]}}""")

    @Test
    fun `so entram pesquisas com participacao do titular que ja comecaram`() {
        val pesquisas = envelope("pesquisas", listOf(
            pesq(1, "2026-10-01", "2026-10-31", status = 1), // encerrada, já respondida
            pesq(2, "2026-11-01", "2026-11-15"),             // aberta, sem participação do titular
            pesq(3, "2026-11-01", "2026-11-30"),             // aberta, pendente do titular
            pesq(4, "2026-12-01", "2026-12-15")              // ainda não começou
        ))
        val participacoes = envelope("participacoes", listOf(
            part(1, 1, 3, 1), part(2, 3, 3, 0), part(3, 4, 3, 0), part(4, 3, 7, 0), part(5, 2, 9, 0)
        ))
        val perguntas = envelope("perguntas", listOf(perg(1, 3, 1), perg(2, 3, 2), perg(3, 1, 1)))

        val lista = LeituraApi.pesquisas(pesquisas, perguntas, participacoes, 3, hoje)!!

        assertEquals(listOf(1, 3), lista.map { it.id })
        assertEquals(true, lista.first { it.id == 1 }.respondida)
        assertEquals(false, lista.first { it.id == 3 }.respondida)
        assertEquals(2, lista.first { it.id == 3 }.totalPerguntas)
        assertEquals("2026-11-30", lista.first { it.id == 3 }.dataFim)
        assertTrue(lista.first { it.id == 3 }.anonimo)
    }

    @Test
    fun `pendente depende do prazo e da resposta, nao do status da pesquisa`() {
        val pesquisas = envelope("pesquisas", listOf(
            pesq(1, "2026-10-01", "2026-10-31", status = 1), // status "ativo", mas prazo vencido
            pesq(3, "2026-11-01", "2026-11-30", status = 0)  // status 0, mas dentro do prazo
        ))
        val participacoes = envelope("participacoes", listOf(part(1, 1, 3, 0), part(2, 3, 3, 0)))
        val lista = LeituraApi.pesquisas(pesquisas, envelope("perguntas", emptyList()), participacoes, 3, hoje)!!

        assertFalse(lista.first { it.id == 1 }.estaPendente(hoje))
        assertTrue(lista.first { it.id == 3 }.estaPendente(hoje))
        assertFalse(lista.first { it.id == 3 }.copy(respondida = true).estaPendente(hoje))
    }

    @Test
    fun `no ultimo dia do prazo a pesquisa ainda esta pendente`() {
        val pesquisas = envelope("pesquisas", listOf(pesq(3, "2026-11-01", "2026-11-05")))
        val participacoes = envelope("participacoes", listOf(part(1, 3, 3, 0)))
        val lista = LeituraApi.pesquisas(pesquisas, envelope("perguntas", emptyList()), participacoes, 3, hoje)!!
        assertTrue(lista.single().estaPendente(hoje))
        assertFalse(lista.single().estaPendente(hoje.plusDays(1)))
    }

    @Test
    fun `perguntas da pesquisa saem em ordem e com tipo e obrigatoriedade`() {
        val perguntas = envelope("perguntas", listOf(
            perg(20, 3, 2, "SIM_NAO", 0), perg(10, 3, 1, "ESCALA", 1), perg(30, 4, 1), perg(21, 3, 3, "TEXTO", 0)
        ))
        val lista = LeituraApi.perguntas(perguntas, 3)!!

        assertEquals(listOf(10, 20, 21), lista.map { it.id })
        assertTrue(lista[0].isEscala && lista[0].obrigatorio)
        assertTrue(lista[1].isSimNao && !lista[1].obrigatorio)
        assertEquals(3, lista[0].idPesquisa)
    }

    @Test
    fun `lista vazia e resposta sem a chave esperada`() {
        val vazio = json("""{"status":true,"status_code":200,"response":{"count":0,"pesquisas":[]}}""")
        assertEquals(emptyList<Any>(), LeituraApi.pesquisas(vazio, vazio, vazio, 3, hoje))
        assertNull(LeituraApi.pesquisas(json("""{"status":true,"status_code":200,"response":{"outra":[]}}"""), vazio, vazio, 3, hoje))
        assertNull(LeituraApi.perguntas(json("""{"status":true,"status_code":200,"response":{"outra":[]}}"""), 3))
    }

    @Test
    fun `participacao de outro colaborador nao altera a situacao do titular`() {
        val pesquisas = envelope("pesquisas", listOf(pesq(3, "2026-11-01", "2026-11-30")))
        val participacoes = envelope("participacoes", listOf(part(1, 3, 3, 0), part(2, 3, 7, 1)))
        val lista = LeituraApi.pesquisas(pesquisas, envelope("perguntas", emptyList()), participacoes, 3, hoje)!!
        assertEquals(false, lista.single().respondida)
    }
}
