package com.example.avalon_mobile

import com.example.avalon_mobile.viewmodel.validarEmailCorporativo
import com.example.avalon_mobile.viewmodel.validarIdentificadorLogin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LoginIdentificadorTest {

    @Test
    fun `e-mail ou login sao aceitos na entrada`() {
        assertNull(validarIdentificadorLogin("maria.silva@empresa.com.br"))
        assertNull(validarIdentificadorLogin("marcos.lima"))
        assertNull(validarIdentificadorLogin("  marcos.lima  "))
        assertNull(validarIdentificadorLogin("daniele.silva@nexusrh"))
    }

    @Test
    fun `campo vazio ou com espaco no meio gera erro`() {
        assertEquals("Informe seu e-mail corporativo ou login.", validarIdentificadorLogin(""))
        assertEquals("Informe seu e-mail corporativo ou login.", validarIdentificadorLogin("   "))
        assertNotNull(validarIdentificadorLogin("marcos lima"))
    }

    @Test
    fun `recuperacao de senha continua exigindo e-mail`() {
        assertNull(validarEmailCorporativo("marcos.lima@empresa.com", recuperacao = true))
        assertEquals(
            "Para recuperar a senha, informe o e-mail corporativo (não o login).",
            validarEmailCorporativo("marcos.lima", recuperacao = true)
        )
    }
}
