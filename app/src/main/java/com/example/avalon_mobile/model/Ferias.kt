package com.example.avalon_mobile.model

import com.example.avalon_mobile.util.Datas
import com.google.gson.annotations.SerializedName
import java.time.temporal.ChronoUnit

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

    /** Dias do pedido: o campo do servidor ou, na falta dele, o intervalo entre as datas (inclusivo). */
    val dias: Int
        get() = quantidadeDias ?: run {
            val i = Datas.parse(dataInicio)
            val f = Datas.parse(dataFim)
            if (i != null && f != null && !f.isBefore(i)) (ChronoUnit.DAYS.between(i, f) + 1).toInt() else 0
        }
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
