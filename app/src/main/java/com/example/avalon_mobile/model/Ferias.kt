package com.example.avalon_mobile.model

import com.google.gson.annotations.SerializedName

data class Ferias(
    val id: Int,
    @SerializedName("data_inicio") val dataInicio: String,
    @SerializedName("data_fim") val dataFim: String,
    @SerializedName("quantidade_dias") val quantidadeDias: Int? = null,
    val observacao: String? = null,
    @SerializedName("id_colaborador") val idColaborador: Int? = null,
    val status: String? = null,
    // Data do pedido: o protótipo mostra "Solicitado em", mas o contrato não traz o campo.
    @SerializedName("data_solicitacao") val dataSolicitacao: String? = null
) {
    val situacao: SituacaoFerias get() = SituacaoFerias.from(status)
}

data class FeriasRequest(
    @SerializedName("data_inicio") val dataInicio: String,
    @SerializedName("data_fim") val dataFim: String,
    @SerializedName("quantidade_dias") val quantidadeDias: Int,
    val observacao: String? = null,
    @SerializedName("id_colaborador") val idColaborador: Int? = null
)

/** Fluxo em duas etapas (Gestor → RH). O protótipo agrupa em três rótulos. */
enum class SituacaoFerias(val rotulo: String) {
    EM_ANALISE("Em análise"),
    APROVADO("Aprovado"),
    NEGADO("Negado");

    companion object {
        fun from(status: String?): SituacaoFerias = when (status) {
            "Aprovado pelo RH" -> APROVADO
            "Recusado pelo Gestor", "Recusado pelo RH" -> NEGADO
            else -> EM_ANALISE // Pendente, Aprovado pelo Gestor (aguarda RH)
        }
    }
}
