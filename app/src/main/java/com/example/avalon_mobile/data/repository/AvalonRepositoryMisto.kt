package com.example.avalon_mobile.data.repository

import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.model.Colaborador
import com.example.avalon_mobile.model.BeneficioItem
import com.example.avalon_mobile.model.Dashboard
import com.example.avalon_mobile.model.DashboardColaborador
import com.example.avalon_mobile.model.DiaSemana
import com.example.avalon_mobile.model.Documento
import com.example.avalon_mobile.model.Notificacao
import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.FeriasRequest
import com.example.avalon_mobile.model.JornadaEscala

class AvalonRepositoryMisto(
    private val api: AvalonRepository,
    private val demonstracao: AvalonRepository,
    private val modulosApi: Set<String>
) : AvalonRepository by demonstracao {

    private fun usaApi(modulo: String) = modulo in modulosApi

    override val homeSoDadosReais: Boolean get() = usaApi(FERIAS)

    override suspend fun notificacoes(): AppResult<List<Notificacao>> =
        if (homeSoDadosReais) AppResult.Success(emptyList()) else demonstracao.notificacoes()

    override suspend fun dashboard(): AppResult<Dashboard> {
        if (!usaApi(FERIAS)) return demonstracao.dashboard()
        return when (val r = api.ferias()) {
            is AppResult.Failure -> r
            is AppResult.Success -> AppResult.Success(
                Dashboard(
                    perfil = "Colaborador",
                    colaborador = DashboardColaborador(
                        feriasEmAndamento = r.data.count { it.status == "Pendente" || it.status == "Aprovado pelo Gestor" }
                    )
                )
            )
        }
    }

    override suspend fun perfil(): AppResult<Colaborador> =
        if (usaApi(PERFIL)) api.perfil() else demonstracao.perfil()

    override suspend fun jornada(): AppResult<Pair<JornadaEscala, List<DiaSemana>>?> =
        if (usaApi(JORNADA)) api.jornada() else demonstracao.jornada()

    override suspend fun ferias(): AppResult<List<Ferias>> =
        if (usaApi(FERIAS)) api.ferias() else demonstracao.ferias()

    override suspend fun solicitarFerias(request: FeriasRequest): AppResult<Ferias> =
        if (usaApi(FERIAS)) api.solicitarFerias(request) else demonstracao.solicitarFerias(request)

    override suspend fun beneficios(): AppResult<List<BeneficioItem>> =
        if (usaApi(BENEFICIO)) api.beneficios() else demonstracao.beneficios()

    override suspend fun documentos(): AppResult<List<Documento>> =
        if (usaApi(DOCUMENTO)) api.documentos() else demonstracao.documentos()

    override suspend fun baixarDocumento(documento: Documento): AppResult<ByteArray> =
        if (usaApi(DOCUMENTO)) api.baixarDocumento(documento) else demonstracao.baixarDocumento(documento)

    companion object {
        const val PERFIL = "perfil"
        const val JORNADA = "jornada"
        const val FERIAS = "ferias"
        const val BENEFICIO = "beneficio"
        const val DOCUMENTO = "documento"

        fun modulos(texto: String): Set<String> =
            texto.split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
    }
}
