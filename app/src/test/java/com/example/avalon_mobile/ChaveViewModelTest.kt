package com.example.avalon_mobile

import com.example.avalon_mobile.viewmodel.chaveViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** TS108, passo 6: o ViewModel guardado de um usuário não pode ser reaproveitado por outro. */
class ChaveViewModelTest {

    @Test
    fun `usuarios diferentes recebem chaves diferentes para a mesma tela`() {
        val rafael = chaveViewModel("DocumentosViewModel", 3)
        val juliana = chaveViewModel("DocumentosViewModel", 4)
        assertNotEquals(rafael, juliana)
    }

    @Test
    fun `mesmo usuario e mesma tela mantem a chave`() {
        assertEquals(chaveViewModel("DocumentosViewModel", 3), chaveViewModel("DocumentosViewModel", 3))
    }

    @Test
    fun `telas diferentes do mesmo usuario nao colidem`() {
        assertNotEquals(chaveViewModel("DocumentosViewModel", 3), chaveViewModel("PerfilViewModel", 3))
    }

    @Test
    fun `sem sessao a chave usa zero`() {
        assertEquals("LoginViewModel:usuario-0", chaveViewModel("LoginViewModel", null))
    }
}
