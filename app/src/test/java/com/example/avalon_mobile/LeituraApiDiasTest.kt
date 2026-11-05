package com.example.avalon_mobile

import com.example.avalon_mobile.data.remote.LeituraApi
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LeituraApiDiasTest {

    private fun json(texto: String) = JsonParser.parseString(texto)

    @Test
    fun `lista de dias na chave plural diasSemana, como o Back-end devolve hoje`() {
        val resposta = json(
            """{"status":true,"status_code":200,"response":{"count":3,"diasSemana":[
                {"id":1,"id_jornada_escala":1,"dia_semana":"Segunda-feira","dia_sigla":"SEG","ativo":1},
                {"id":2,"id_jornada_escala":1,"dia_semana":"Terça-feira","dia_sigla":"TER","ativo":1},
                {"id":9,"id_jornada_escala":2,"dia_semana":"Segunda-feira","dia_sigla":"SEG","ativo":1}]}}"""
        )
        val dias = LeituraApi.dias(resposta, 1)
        assertNotNull(dias)
        assertEquals(listOf("SEG", "TER"), dias!!.map { it.diaSigla })
    }

    @Test
    fun `continua aceitando as chaves diaSemana e dias`() {
        val item = """{"id":1,"id_jornada_escala":1,"dia_semana":"Segunda-feira","dia_sigla":"SEG","ativo":1}"""
        assertEquals(1, LeituraApi.dias(json("""{"status":true,"status_code":200,"response":{"diaSemana":[$item]}}"""), 1)!!.size)
        assertEquals(1, LeituraApi.dias(json("""{"status":true,"status_code":200,"response":{"dias":[$item]}}"""), 1)!!.size)
    }

    @Test
    fun `resposta sem lista de dias continua sendo inesperada`() {
        assertEquals(null, LeituraApi.dias(json("""{"status":true,"status_code":200,"response":{"outraChave":[]}}"""), 1))
    }
}
