package com.example.avalon_mobile

import com.example.avalon_mobile.data.AppError
import com.example.avalon_mobile.data.repository.ARQUIVO_INDISPONIVEL
import com.example.avalon_mobile.data.repository.erroDownloadDocumento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/** TS108, passo 3: arquivo removido do servidor não pode aparecer como "funcionalidade não disponível". */
class DownloadDocumentoErroTest {

    @Test
    fun `404 do download vira arquivo indisponivel`() {
        val original = AppError(AppError.NAO_IMPLEMENTADO, "Funcionalidade ainda não disponível no servidor.", 404)
        val erro = erroDownloadDocumento(original)
        assertEquals(ARQUIVO_INDISPONIVEL, erro.codigo)
        assertEquals("Arquivo indisponível no servidor. Procure o RH.", erro.mensagem)
        assertEquals(404, erro.httpStatus)
    }

    @Test
    fun `outros erros seguem como vieram`() {
        val semPermissao = AppError("SEM_PERMISSAO", "Você não tem permissão para ver estes dados.", 403)
        val semConexao = AppError(AppError.SEM_CONEXAO, "Não foi possível conectar ao servidor. Verifique sua conexão.")
        assertSame(semPermissao, erroDownloadDocumento(semPermissao))
        assertSame(semConexao, erroDownloadDocumento(semConexao))
    }
}
