package com.example.avalon_mobile.model

import com.google.gson.annotations.SerializedName

data class Notificacao(
    val id: Int,
    @SerializedName("id_colaborador") val idColaborador: Int? = null,
    @SerializedName("tipo_origem") val tipoOrigem: String? = null,
    val titulo: String,
    val mensagem: String? = null,
    // Lacuna do DDL (pendência 6 do contrato): sem `lida` nem data/hora. Ficam opcionais.
    val lida: Boolean? = null,
    @SerializedName("data_hora") val dataHora: String? = null
)

data class Dashboard(
    val perfil: String,
    val colaborador: DashboardColaborador? = null
)

data class DashboardColaborador(
    @SerializedName("ferias_em_andamento") val feriasEmAndamento: Int = 0,
    @SerializedName("pesquisas_pendentes") val pesquisasPendentes: Int = 0,
    @SerializedName("avaliacoes_pendentes") val avaliacoesPendentes: Int = 0,
    @SerializedName("notificacoes_total") val notificacoesTotal: Int = 0
)

/** Comunicado do mural de avisos (6.3.2). Não há rota no contrato v2.1.0. */
data class Comunicado(
    val id: Int,
    val titulo: String,
    val detalhe: String
)
