package com.example.avalon_mobile.model

import com.google.gson.annotations.SerializedName

data class Feedback(
    val id: Int,
    @SerializedName("id_colaborador_destinatario") val idDestinatario: Int,
    val tipo: String,
    val descricao: String? = null,
    @SerializedName("data_feedback") val dataFeedback: String? = null,
    val ativo: Boolean = true,
    @SerializedName("id_colaborador_remetente") val idRemetente: Int? = null,
    // Exibição do remetente (nome/cargo) e leitura: presentes no protótipo, ausentes no contrato.
    @SerializedName("remetente_nome") val remetenteNome: String? = null,
    @SerializedName("remetente_cargo") val remetenteCargo: String? = null,
    val lido: Boolean? = null
)

data class RespostaFeedback(
    val id: Int,
    val descricao: String,
    @SerializedName("id_feedback") val idFeedback: Int? = null,
    @SerializedName("id_colaborador") val idColaborador: Int? = null,
    @SerializedName("data_resposta") val dataResposta: String? = null
)

data class RespostaFeedbackRequest(
    val descricao: String
)
