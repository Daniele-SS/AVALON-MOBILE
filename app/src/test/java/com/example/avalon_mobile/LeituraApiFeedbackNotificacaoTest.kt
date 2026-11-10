package com.example.avalon_mobile

import com.example.avalon_mobile.data.remote.LeituraApi
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Formato real devolvido pelo Back-end em 06/11/2026: listagens de todos os colaboradores, com relações aninhadas. */
class LeituraApiFeedbackNotificacaoTest {

    private fun json(texto: String) = JsonParser.parseString(texto)

    private fun colab(id: Int, nome: String, cargo: String = "Analista") =
        """{"id":$id,"nome":"$nome","cpf":"000","cargo":{"id":1,"nome":"$cargo"},"setor":[{"id":1,"nome":"RH"}]}"""

    private fun fb(id: Int, dest: Int, rem: Int, tipo: String, data: String, ativo: Int = 1) =
        """{"id":$id,"id_colaborador_remetente":$rem,"id_colaborador_destinatario":$dest,"tipo":"$tipo","descricao":"Texto $id","data_feedback":"${data}T03:00:00.000Z","ativo":$ativo,"colaborador_remetente":[${colab(rem, "Remetente $rem", "Supervisora")}],"colaborador_destinatario":[${colab(dest, "Dest $dest")}]}"""

    private fun nt(id: Int, col: Int, tipo: String = "FERIAS") =
        """{"id":$id,"id_colaborador":$col,"tipo_origem":"$tipo","titulo":"Título $id","mensagem":"Msg $id","colaborador":[${colab(col, "Colab $col")}]}"""

    private fun envelope(chave: String, itens: List<String>) =
        json("""{"status":true,"status_code":200,"response":{"count":${itens.size},"$chave":[${itens.joinToString(",")}]}}""")

    @Test
    fun `feedbacks - so os do titular, ativos, mais recentes primeiro`() {
        val r = LeituraApi.feedbacks(envelope("feedbacks", listOf(
            fb(1, dest = 3, rem = 1, tipo = "RECONHECIMENTO", data = "2026-08-20"),
            fb(2, dest = 9, rem = 1, tipo = "FORMAL", data = "2026-09-01"),
            fb(3, dest = 3, rem = 2, tipo = "DESENVOLVIMENTO", data = "2026-10-05"),
            fb(4, dest = 3, rem = 2, tipo = "FORMAL", data = "2026-10-10", ativo = 0)
        )), idColaborador = 3, vistos = emptySet())!!
        assertEquals(listOf(3, 1), r.map { it.id })
    }

    @Test
    fun `feedbacks - remetente, cargo, tipo capitalizado e data`() {
        val f = LeituraApi.feedbacks(envelope("feedbacks", listOf(fb(7, 3, 5, "RECONHECIMENTO", "2026-08-20"))), 3, emptySet())!!.single()
        assertEquals("Reconhecimento", f.tipo)
        assertEquals("Remetente 5", f.remetenteNome)
        assertEquals("Supervisora", f.remetenteCargo)
        assertEquals(5, f.idRemetente)
        assertEquals("2026-08-20", f.dataFeedback)
        assertEquals("Texto 7", f.descricao)
    }

    @Test
    fun `feedbacks - lido vem dos ids vistos no aparelho`() {
        val itens = envelope("feedbacks", listOf(fb(1, 3, 1, "FORMAL", "2026-08-01"), fb(2, 3, 1, "FORMAL", "2026-08-02")))
        val r = LeituraApi.feedbacks(itens, 3, vistos = setOf(1))!!
        assertEquals(true, r.first { it.id == 1 }.lido)
        assertEquals(false, r.first { it.id == 2 }.lido)
    }

    @Test
    fun `feedbacks - lista vazia e formato inesperado`() {
        assertTrue(LeituraApi.feedbacks(envelope("feedbacks", emptyList()), 3, emptySet())!!.isEmpty())
        assertNull(LeituraApi.feedbacks(json("""{"status":true,"response":{"outra":[]}}"""), 3, emptySet()))
    }

    @Test
    fun `notificacoes - so as do titular, ordem por id decrescente e lida local`() {
        val r = LeituraApi.notificacoes(envelope("notificacoes", listOf(nt(1, 3), nt(2, 8), nt(5, 3, "PESQUISA"), nt(4, 3))), 3, lidas = setOf(4))!!
        assertEquals(listOf(5, 4, 1), r.map { it.id })
        assertEquals(listOf(false, true, false), r.map { it.lida })
        assertEquals("PESQUISA", r.first().tipoOrigem)
        assertEquals("Título 5", r.first().titulo)
        assertNull(r.first().dataHora)
    }

    @Test
    fun `notificacoes - sem titulo e descartada, formato inesperado devolve null`() {
        val semTitulo = """{"id":9,"id_colaborador":3,"tipo_origem":"FERIAS"}"""
        assertTrue(LeituraApi.notificacoes(envelope("notificacoes", listOf(semTitulo)), 3, emptySet())!!.isEmpty())
        assertNull(LeituraApi.notificacoes(json("""{"status":true,"response":{"feedbacks":[]}}"""), 3, emptySet()))
        assertFalse(LeituraApi.notificacoes(envelope("notificacoes", listOf(nt(1, 3))), 3, emptySet())!!.isEmpty())
        assertNotNull(LeituraApi.notificacoes(json("[]"), 3, emptySet()))
    }
}
