package com.example.avalon_mobile.model

import com.example.avalon_mobile.util.Datas
import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class Pesquisa(
    val id: Int,
    val titulo: String,
    val descricao: String? = null,
    @SerializedName("data_inicio") val dataInicio: String,
    @SerializedName("data_fim") val dataFim: String,
    val anonimo: Boolean,
    val status: Boolean,
    @SerializedName("publico_selecionado") val publicoSelecionado: String = "TODOS",
    // Contadores de apresentação (protótipo): total de perguntas e situação do titular.
    @SerializedName("total_perguntas") val totalPerguntas: Int? = null,
    val respondida: Boolean? = null,
    @SerializedName("data_resposta") val dataResposta: String? = null
) {
    /** Pode ser respondida agora: o titular ainda não respondeu, a pesquisa está ativa e o prazo não acabou. */
    fun estaPendente(hoje: LocalDate = LocalDate.now()): Boolean =
        respondida != true && status && Datas.parse(dataFim)?.isBefore(hoje) != true
}

data class Pergunta(
    val id: Int,
    val enunciado: String,
    val tipo: String,
    val ordem: Int,
    val obrigatorio: Boolean = false,
    @SerializedName("id_pesquisa") val idPesquisa: Int? = null
) {
    val isEscala: Boolean get() = tipo.equals("ESCALA", ignoreCase = true)
    val isSimNao: Boolean get() = tipo.equals("SIM_NAO", ignoreCase = true)
}

data class RespostaPesquisaItem(
    @SerializedName("id_pergunta") val idPergunta: Int,
    val valor: String
)

data class RespostaPesquisaLote(
    val respostas: List<RespostaPesquisaItem>
)

data class Avaliacao(
    val id: Int,
    val titulo: String,
    val descricao: String? = null,
    @SerializedName("data_inicio") val dataInicio: String,
    @SerializedName("data_fim") val dataFim: String,
    val anonimo: Boolean,
    val status: Boolean,
    @SerializedName("publico_selecionado") val publicoSelecionado: String = "TODOS"
)

data class AvaliacaoPergunta(
    val id: Int,
    @SerializedName("id_psicossocial_fator") val idFator: Int,
    val enunciado: String,
    val tipo: String = "ESCALA",
    val ordem: Int,
    val obrigatorio: Boolean = false,
    val ativo: Boolean = true
)

data class RespostaAvaliacaoItem(
    @SerializedName("id_avaliacao_pergunta") val idAvaliacaoPergunta: Int,
    val valor: Int
)

data class RespostaAvaliacaoLote(
    val respostas: List<RespostaAvaliacaoItem>
)

/** Confirmação sem identificadores (anonimato). */
data class RespostaConfirmacao(
    @SerializedName("total_respostas") val totalRespostas: Int,
    @SerializedName("participacao_concluida") val participacaoConcluida: Boolean
)
