package com.example.avalon_mobile

import com.example.avalon_mobile.data.local.Aparencia
import com.example.avalon_mobile.data.local.CorrecaoLocalPrefs
import com.example.avalon_mobile.data.local.EscalasFonte
import com.example.avalon_mobile.data.local.TemaApp
import com.example.avalon_mobile.model.CorrecaoRegistrada
import com.example.avalon_mobile.util.CorrecaoRegras
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Correção cadastral (6.5.2) e preferências de aparência/acessibilidade: regras sem Android. */
class CorrecaoAparenciaTest {

    private val justificativaOk = "O sobrenome saiu errado no cadastro."

    @Test fun cpfValidoAceitaComEsemMascara() {
        assertTrue(CorrecaoRegras.cpfValido("529.982.247-25"))
        assertTrue(CorrecaoRegras.cpfValido("52998224725"))
    }

    @Test fun cpfInvalidoRejeitaDigitoVerificadorRepetidosETamanho() {
        assertFalse(CorrecaoRegras.cpfValido("529.982.247-26"))
        assertFalse(CorrecaoRegras.cpfValido("111.111.111-11"))
        assertFalse(CorrecaoRegras.cpfValido("1234"))
        assertFalse(CorrecaoRegras.cpfValido(""))
    }

    @Test fun emailValidoEInvalido() {
        assertNull(CorrecaoRegras.validar("E-mail", "maria.silva@empresa.com.br", justificativaOk))
        assertNotNull(CorrecaoRegras.validar("E-mail", "maria.silva@", justificativaOk))
        assertNotNull(CorrecaoRegras.validar("E-mail", "maria silva@empresa.com", justificativaOk))
    }

    @Test fun justificativaCurtaOuLongaDemais() {
        assertNotNull(CorrecaoRegras.validar("Nome", "Maria Souza", "curta"))
        assertNotNull(CorrecaoRegras.validar("Nome", "Maria Souza", "x".repeat(CorrecaoRegras.JUSTIFICATIVA_MAX + 1)))
        assertNull(CorrecaoRegras.validar("Nome", "Maria Souza", "x".repeat(CorrecaoRegras.JUSTIFICATIVA_MIN)))
    }

    @Test fun espacosNasPontasNaoContamComoConteudo() {
        assertNotNull(CorrecaoRegras.validar("Cargo", "   ", justificativaOk))
        assertNotNull(CorrecaoRegras.validar("Nome", "Maria Souza", "        curta  "))
    }

    @Test fun campoDesconhecidoEhRejeitado() {
        assertNotNull(CorrecaoRegras.validar("Salário", "10000", justificativaOk))
    }

    @Test fun matriculaAceitaLetrasDigitosEhifen() {
        assertNull(CorrecaoRegras.validar("Matrícula", "RH-2024-017", justificativaOk))
        assertNotNull(CorrecaoRegras.validar("Matrícula", "12 3", justificativaOk))
    }

    private fun reg(id: Long, campo: String, status: String = CorrecaoRegistrada.EM_ANALISE) =
        CorrecaoRegistrada(id, campo, "x", "y", "2026-11-10T09:00:00", status)

    @Test fun pendenteSoConsideraEmAnaliseDoMesmoCampo() {
        val h = listOf(reg(1, "Nome"), reg(2, "CPF", status = "RESOLVIDA"))
        assertTrue(CorrecaoRegras.pendentePara("Nome", h))
        assertFalse(CorrecaoRegras.pendentePara("CPF", h))
        assertFalse(CorrecaoRegras.pendentePara("Setor", h))
    }

    @Test fun historicoMantemMaisRecentePrimeiroELimite() {
        var h = emptyList<CorrecaoRegistrada>()
        for (i in 1L..(CorrecaoRegras.HISTORICO_MAX + 5L)) h = CorrecaoRegras.adicionar(h, reg(i, "Nome"))
        assertEquals(CorrecaoRegras.HISTORICO_MAX, h.size)
        assertEquals(CorrecaoRegras.HISTORICO_MAX + 5L, h.first().id)
    }

    @Test fun historicoJsonRoundTripEJsonCorrompido() {
        val gson = Gson()
        val lista = listOf(reg(7, "E-mail"))
        assertEquals(lista, CorrecaoLocalPrefs.desserializar(gson.toJson(lista), gson))
        assertTrue(CorrecaoLocalPrefs.desserializar(null, gson).isEmpty())
        assertTrue(CorrecaoLocalPrefs.desserializar("", gson).isEmpty())
        assertTrue(CorrecaoLocalPrefs.desserializar("{não é json", gson).isEmpty())
    }

    @Test fun escalaDeFonteNormalizaParaOpcaoMaisProxima() {
        assertEquals(1.0f, EscalasFonte.normalizar(null), 0f)
        assertEquals(1.0f, EscalasFonte.normalizar(Float.NaN), 0f)
        assertEquals(1.3f, EscalasFonte.normalizar(5f), 0f)
        assertEquals(0.85f, EscalasFonte.normalizar(-3f), 0f)
        assertEquals(1.15f, EscalasFonte.normalizar(1.2f), 0f)
        assertEquals("Muito grande", EscalasFonte.rotulo(1.3f))
    }

    @Test fun temaDesconhecidoCaiNoSistemaEPadraoDaAparencia() {
        assertEquals(TemaApp.SISTEMA, TemaApp.deChave(null))
        assertEquals(TemaApp.SISTEMA, TemaApp.deChave("roxo"))
        assertEquals(TemaApp.ESCURO, TemaApp.deChave("escuro"))
        val padrao = Aparencia()
        assertEquals(TemaApp.SISTEMA, padrao.tema)
        assertFalse(padrao.altoContraste)
        assertEquals(1.0f, padrao.escalaFonte, 0f)
    }
}
