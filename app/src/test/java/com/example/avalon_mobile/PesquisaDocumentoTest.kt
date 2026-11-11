package com.example.avalon_mobile

import com.example.avalon_mobile.model.Documento
import com.example.avalon_mobile.model.Pergunta
import com.example.avalon_mobile.util.PesquisaRegras
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PesquisaDocumentoTest {
    private val perguntas = listOf(
        Pergunta(1, "Opcional", "TEXTO", 1, obrigatorio = false),
        Pergunta(2, "Obrigatória A", "ESCALA", 2, obrigatorio = true),
        Pergunta(3, "Obrigatória B", "TEXTO", 3, obrigatorio = true)
    )

    @Test fun obrigatoriaSemRespostaEPendente() {
        val p = PesquisaRegras.pendentes(perguntas, mapOf(1 to "x"))
        assertEquals(listOf(2, 3), p.map { it.id })
    }

    @Test fun respostaSoComEspacosNaoAtendeObrigatoriedade() {
        val p = PesquisaRegras.pendentes(perguntas, mapOf(2 to "4", 3 to "   "))
        assertEquals(listOf(3), p.map { it.id })
    }

    @Test fun opcionalEmBrancoNaoBloqueia() {
        assertTrue(PesquisaRegras.pendentes(perguntas, mapOf(2 to "4", 3 to "ok")).isEmpty())
    }

    @Test fun primeiraPendenteRetornaPosicaoNaListaENaoOId() {
        assertEquals(1, PesquisaRegras.indiceDaPrimeiraPendente(perguntas, emptyMap()))
        assertEquals(2, PesquisaRegras.indiceDaPrimeiraPendente(perguntas, mapOf(2 to "5")))
        assertNull(PesquisaRegras.indiceDaPrimeiraPendente(perguntas, mapOf(2 to "5", 3 to "ok")))
    }

    private fun doc(data: String?, ativo: Boolean = true) = Documento(1, 4, "Contrato", "Contrato", ativo = ativo, dataDisponibilizacao = data)

    @Test fun documentoSemDataNaoMostraDesdeTraco() {
        assertEquals("Contrato • Disponível", doc(null).legenda)
        assertEquals("Contrato • Disponível", doc("").legenda)
        assertEquals("Contrato • Disponível", doc("lixo").legenda)
    }

    @Test fun documentoComDataMostraDisponivelDesde() {
        assertEquals("Contrato • Disponível desde 15/03/2024", doc("2024-03-15").legenda)
        assertEquals("Contrato • Disponível desde 15/03/2024", doc("2024-03-15T10:00:00.000Z").legenda)
    }

    @Test fun documentoIndisponivelIgnoraData() {
        assertEquals("Contrato • Indisponível", doc("2024-03-15", ativo = false).legenda)
    }

    @Test fun legendaDeAssinadoSoCitaDataQuandoExiste() {
        assertEquals("Assinado • PDF", doc(null).legendaAssinado)
        assertEquals("Assinado em 15/03/2024 • PDF", doc("2024-03-15").legendaAssinado)
    }
}
