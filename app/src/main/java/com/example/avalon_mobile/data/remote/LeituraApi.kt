package com.example.avalon_mobile.data.remote

import com.example.avalon_mobile.model.Colaborador
import com.example.avalon_mobile.model.DiaSemana
import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.JornadaEscala
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

object LeituraApi {

    fun colaborador(json: JsonElement): Colaborador? {
        val o = registro(json, "colaborador") ?: return null
        val nome = o.texto("nome") ?: return null
        return Colaborador(
            id = o.inteiro("id") ?: return null,
            idSetor = o.inteiro("id_setor") ?: relacionado(o, "setor")?.inteiro("id"),
            idCargo = o.inteiro("id_cargo") ?: relacionado(o, "cargo")?.inteiro("id"),
            idJornadaEscala = o.inteiro("id_jornada_escala") ?: relacionado(o, "jornada_escala")?.inteiro("id"),
            matricula = o.texto("matricula").orEmpty(),
            nome = nome,
            cpf = o.texto("cpf").orEmpty(),
            dataNascimento = o.data("data_nascimento"),
            email = o.texto("email").orEmpty(),
            telefone = o.texto("telefone"),
            dataAdmissao = o.data("data_admissao").orEmpty(),
            dataDesligamento = o.data("data_desligamento"),
            status = o.texto("status").orEmpty(),
            tipoVinculo = o.texto("tipo_vinculo"),
            setorNome = o.texto("setor_nome") ?: relacionado(o, "setor")?.texto("nome"),
            cargoNome = o.texto("cargo_nome") ?: relacionado(o, "cargo")?.texto("nome"),
            ghe = o.texto("ghe")
        )
    }

    fun jornada(json: JsonElement): JornadaEscala? {
        val o = registro(json, "jornadaEscala") ?: registro(json, "jornada_escala") ?: return null
        return JornadaEscala(
            id = o.inteiro("id") ?: return null,
            nome = o.texto("nome").orEmpty(),
            descricao = o.texto("descricao"),
            horaInicio = o.texto("hora_inicio") ?: return null,
            horaFim = o.texto("hora_fim") ?: return null,
            status = o.logico("status") ?: true
        )
    }

    fun dias(json: JsonElement, idJornada: Int): List<DiaSemana>? {
        val lista = lista(json, "diaSemana") ?: lista(json, "dias") ?: return null
        return lista.mapNotNull { e ->
            val o = e.objeto() ?: return@mapNotNull null
            val jornadaDoDia = o.inteiro("id_jornada_escala") ?: relacionado(o, "jornada_escala")?.inteiro("id")
            if (jornadaDoDia != null && jornadaDoDia != idJornada) return@mapNotNull null
            DiaSemana(
                id = o.inteiro("id") ?: return@mapNotNull null,
                diaSemana = o.texto("dia_semana").orEmpty(),
                diaSigla = o.texto("dia_sigla")?.take(3)?.uppercase() ?: return@mapNotNull null,
                ativo = o.logico("ativo") ?: true,
                idJornadaEscala = jornadaDoDia ?: idJornada
            )
        }
    }

    fun ferias(json: JsonElement, idColaborador: Int): List<Ferias>? {
        val itens = lista(json, "ferias") ?: return null
        return itens.mapNotNull { e ->
            val o = e.objeto() ?: return@mapNotNull null
            val dono = o.inteiro("id_colaborador") ?: relacionado(o, "colaborador")?.inteiro("id")
            if (dono != idColaborador) return@mapNotNull null
            feriasDe(o).let { it?.copy(idColaborador = dono) }
        }
    }

    fun feriasCriada(json: JsonElement): Ferias? {
        val o = registro(json, "ferias") ?: return null
        return feriasDe(o)
    }

    private fun feriasDe(o: JsonObject): Ferias? {
        return Ferias(
            id = o.inteiro("id") ?: return null,
            dataInicio = o.data("data_inicio") ?: return null,
            dataFim = o.data("data_fim") ?: return null,
            quantidadeDias = o.inteiro("quantidade_dias"),
            observacao = o.texto("observacao"),
            idColaborador = o.inteiro("id_colaborador"),
            status = o.texto("status")
        )
    }

    private fun conteudo(json: JsonElement): JsonElement {
        val o = json.objeto() ?: return json
        return if (o.has("response") && (o.has("status") || o.has("status_code"))) o.get("response") else json
    }

    private fun registro(json: JsonElement, chave: String): JsonObject? {
        val c = conteudo(json)
        val o = c.objeto() ?: return c.primeiro()
        return when {
            o.has(chave) -> o.get(chave).primeiro()
            o.has("id") -> o
            else -> null
        }
    }

    private fun lista(json: JsonElement, chave: String): JsonArray? {
        val c = conteudo(json)
        if (c.isJsonArray) return c.asJsonArray
        val o = c.objeto() ?: return null
        val v = o.get(chave) ?: return null
        return if (v.isJsonArray) v.asJsonArray else null
    }

    private fun relacionado(o: JsonObject, chave: String): JsonObject? {
        val v = o.get(chave) ?: return null
        val alvo = v.primeiro() ?: return null
        if (alvo.has("id") || alvo.has("nome")) return alvo
        return alvo.entrySet().firstNotNullOfOrNull { (_, valor) -> valor.primeiro() }
    }

    private fun JsonElement.objeto(): JsonObject? = if (isJsonObject) asJsonObject else null

    private fun JsonElement.primeiro(): JsonObject? = when {
        isJsonObject -> asJsonObject
        isJsonArray && asJsonArray.size() > 0 -> asJsonArray[0].objeto()
        else -> null
    }

    private fun JsonObject.valor(chave: String): JsonElement? = get(chave)?.takeUnless { it.isJsonNull }

    private fun JsonObject.texto(chave: String): String? =
        valor(chave)?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }

    private fun JsonObject.inteiro(chave: String): Int? =
        valor(chave)?.takeIf { it.isJsonPrimitive }?.asString?.toIntOrNull()

    private fun JsonObject.data(chave: String): String? = texto(chave)?.take(10)

    private fun JsonObject.logico(chave: String): Boolean? {
        val p = valor(chave)?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive ?: return null
        return when {
            p.isBoolean -> p.asBoolean
            p.isNumber -> p.asInt != 0
            else -> p.asString.lowercase() in setOf("1", "true", "ativo")
        }
    }
}
