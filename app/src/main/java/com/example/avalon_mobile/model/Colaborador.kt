package com.example.avalon_mobile.model

import com.google.gson.annotations.SerializedName

data class Colaborador(
    val id: Int,
    @SerializedName("id_setor") val idSetor: Int,
    @SerializedName("id_cargo") val idCargo: Int,
    @SerializedName("id_jornada_escala") val idJornadaEscala: Int,
    val matricula: String,
    val nome: String,
    val cpf: String,
    @SerializedName("data_nascimento") val dataNascimento: String? = null,
    val email: String,
    val telefone: String? = null,
    @SerializedName("data_admissao") val dataAdmissao: String,
    @SerializedName("data_desligamento") val dataDesligamento: String? = null,
    val status: String,
    @SerializedName("tipo_vinculo") val tipoVinculo: String? = null,
    @SerializedName("setor_nome") val setorNome: String? = null,
    @SerializedName("cargo_nome") val cargoNome: String? = null,
    // GHE aparece no protótipo, mas não existe no contrato v2.1.0: fica nulo até o Back-end expor.
    val ghe: String? = null
) {
    /** CPF mascarado no formato do protótipo (•••.482.•••-10), mesmo para o titular. */
    val cpfMascarado: String
        get() {
            val d = cpf.filter(Char::isDigit)
            return if (d.length == 11) "•••.${d.substring(3, 6)}.•••-${d.substring(9)}" else "•••"
        }

    val iniciais: String
        get() = nome.split(" ").filter { it.isNotBlank() }.let { partes ->
            (partes.firstOrNull()?.take(1).orEmpty() + partes.drop(1).lastOrNull()?.take(1).orEmpty()).uppercase()
        }
}

/**
 * Solicitação de correção cadastral (RF 1.1.5 / RF-006). Campos com cadeado no perfil
 * só podem ser alterados por esse fluxo, que o RH analisa.
 */
data class SolicitacaoCorrecao(
    val campo: String,
    @SerializedName("valor_sugerido") val valorSugerido: String,
    val justificativa: String
)
