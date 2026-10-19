package com.example.avalon_mobile

import com.example.avalon_mobile.viewmodel.CodigoRecuperacaoViewModel
import com.example.avalon_mobile.viewmodel.validarEmailCorporativo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LoginEmailCorporativoTest {

    @Test
    fun `e-mail corporativo valido nao gera erro`() {
        assertNull(validarEmailCorporativo("maria.silva@empresa.com.br"))
        assertNull(validarEmailCorporativo("  maria.silva@empresa.com.br  "))
    }

    @Test
    fun `campo vazio ou formato invalido gera erro`() {
        assertEquals("Informe seu e-mail corporativo.", validarEmailCorporativo(""))
        assertEquals("Informe seu e-mail corporativo para recuperar a senha.", validarEmailCorporativo(" ", recuperacao = true))
        assertNotNull(validarEmailCorporativo("maria.silva"))
        assertNotNull(validarEmailCorporativo("maria@empresa"))
        assertNotNull(validarEmailCorporativo("maria silva@empresa.com"))
    }

    @Test
    fun `tempo de reenvio e exibido em minutos e segundos`() {
        assertEquals("2:00", CodigoRecuperacaoViewModel.formatarTempo(120))
        assertEquals("1:05", CodigoRecuperacaoViewModel.formatarTempo(65))
        assertEquals("0:09", CodigoRecuperacaoViewModel.formatarTempo(9))
    }
}
