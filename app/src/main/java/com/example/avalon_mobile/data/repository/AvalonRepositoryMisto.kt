package com.example.avalon_mobile.data.repository

import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.model.Colaborador
import com.example.avalon_mobile.model.DiaSemana
import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.FeriasRequest
import com.example.avalon_mobile.model.JornadaEscala

class AvalonRepositoryMisto(
    private val api: AvalonRepository,
    private val demonstracao: AvalonRepository,
    private val modulosApi: Set<String>
) : AvalonRepository by demonstracao {

    private fun usaApi(modulo: String) = modulo in modulosApi

    override suspend fun perfil(): AppResult<Colaborador> =
        if (usaApi(PERFIL)) api.perfil() else demonstracao.perfil()

    override suspend fun jornada(): AppResult<Pair<JornadaEscala, List<DiaSemana>>?> =
        if (usaApi(JORNADA)) api.jornada() else demonstracao.jornada()

    override suspend fun ferias(): AppResult<List<Ferias>> =
        if (usaApi(FERIAS)) api.ferias() else demonstracao.ferias()

    override suspend fun solicitarFerias(request: FeriasRequest): AppResult<Ferias> =
        if (usaApi(FERIAS)) api.solicitarFerias(request) else demonstracao.solicitarFerias(request)

    companion object {
        const val PERFIL = "perfil"
        const val JORNADA = "jornada"
        const val FERIAS = "ferias"

        fun modulos(texto: String): Set<String> =
            texto.split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
    }
}
