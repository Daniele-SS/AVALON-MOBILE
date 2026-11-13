package com.example.avalon_mobile.data.remote

import com.example.avalon_mobile.model.Colaborador
import com.example.avalon_mobile.model.Beneficio
import com.example.avalon_mobile.model.BeneficioItem
import com.example.avalon_mobile.model.DiaSemana
import com.example.avalon_mobile.model.Documento
import com.example.avalon_mobile.model.Feedback
import com.example.avalon_mobile.model.RespostaFeedback
import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.JornadaEscala
import com.example.avalon_mobile.model.Notificacao
import com.example.avalon_mobile.model.Pergunta
import com.example.avalon_mobile.model.Pesquisa
import com.example.avalon_mobile.util.Datas
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.time.LocalDate

object LeituraApi {

    /**
     * Falha embutida no corpo de uma resposta HTTP 2xx. O envio de respostas do Back-end devolve o conflito
     * como {"status":409,"field":"PESQUISA_JA_RESPONDIDA","message":...} com HTTP 201; sem esta checagem o app
     * mostraria "enviado" para uma pesquisa que o servidor recusou. Retorna (código, mensagem) ou null se não houve falha.
     */
    fun falhaNoCorpo(json: JsonElement): Pair<String, String?>? {
        val o = json.objeto() ?: return null
        val status = o.get("status")?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive ?: return null
        val falhou = when {
            status.isBoolean -> !status.asBoolean
            status.isNumber -> status.asInt !in 200..299
            else -> false
        }
        if (!falhou) return null
        val http = if (status.isNumber) status.asInt else o.inteiro("status_code")
        return Pair(o.texto("field") ?: "HTTP_${http ?: 0}", o.texto("message"))
    }

    fun colaborador(json: JsonElement): Colaborador? {
        val o = registro(json, "colaborador") ?: return null
        val nome = o.texto("nome") ?: o.texto("nome_colaborador") ?: return null
        return Colaborador(
            id = o.inteiro("id") ?: o.inteiro("id_colaborador") ?: return null,
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
            status = (o.texto("status") ?: o.texto("status_colaborador")).orEmpty(),
            tipoVinculo = o.texto("tipo_vinculo"),
            setorNome = o.texto("setor_nome") ?: relacionado(o, "setor")?.texto("nome") ?: o.texto("setor"),
            cargoNome = o.texto("cargo_nome") ?: relacionado(o, "cargo")?.texto("nome") ?: o.texto("cargo")
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
        // O Back-end devolve "diasSemana" na listagem e "diaSemana" na consulta por id (padronização dos DAOs, 28/10).
        val lista = lista(json, "diasSemana") ?: lista(json, "diaSemana") ?: lista(json, "dias") ?: return null
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
            val dono = o.inteiro("id_colaborador") ?: relacionado(o, "colaborador")?.let { it.inteiro("id") ?: it.inteiro("id_colaborador") }
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

    fun beneficios(concessoes: JsonElement, catalogo: JsonElement, idColaborador: Int): List<BeneficioItem>? {
        val itens = lista(concessoes, "beneficioColaborador") ?: return null
        val catalogoPorId = (lista(catalogo, "beneficios") ?: JsonArray()).mapNotNull { e ->
            val o = e.objeto() ?: return@mapNotNull null
            beneficioDe(o)?.let { it.id to it }
        }.toMap()
        return itens.mapNotNull { e ->
            val o = e.objeto() ?: return@mapNotNull null
            val dono = o.inteiro("id_colaborador") ?: relacionado(o, "colaborador")?.let { it.inteiro("id") ?: it.inteiro("id_colaborador") }
            if (dono != idColaborador) return@mapNotNull null
            val beneficio = relacionado(o, "beneficio")?.let { beneficioDe(it) }
                ?: o.inteiro("id_beneficio")?.let { catalogoPorId[it] }
                ?: return@mapNotNull null
            BeneficioItem(beneficio, aderido = o.logico("ativo") ?: true)
        }
    }

    private fun beneficioDe(o: JsonObject): Beneficio? {
        return Beneficio(
            id = o.inteiro("id") ?: return null,
            nome = o.texto("nome") ?: return null,
            descricao = o.texto("descricao"),
            status = o.logico("status") ?: true,
            disponibilidade = o.texto("disponibilidade") ?: "TODOS"
        )
    }

    fun documentos(json: JsonElement, idColaborador: Int): List<Documento>? {
        val itens = lista(json, "documentos") ?: return null
        return itens.mapNotNull { e ->
            val o = e.objeto() ?: return@mapNotNull null
            val dono = o.inteiro("id_colaborador") ?: relacionado(o, "colaborador")?.let { it.inteiro("id") ?: it.inteiro("id_colaborador") }
            if (dono != idColaborador) return@mapNotNull null
            if (o.logico("ativo") == false) return@mapNotNull null
            Documento(
                id = o.inteiro("id") ?: return@mapNotNull null,
                idColaborador = idColaborador,
                nome = o.texto("nome") ?: return@mapNotNull null,
                tipoDocumento = o.texto("tipo_documento").orEmpty(),
                caminhoArquivo = o.texto("caminho_arquivo"),
                ativo = true
            )
        }
    }

    /**
     * Pesquisas do titular. O Back-end devolve as listagens de todos os colaboradores; só entram as pesquisas em que o
     * titular tem participação e que já começaram. `participacao.status` 1 = respondida. O `status` da pesquisa não é
     * usado: no banco ele não acompanha as datas (há pesquisa "ativa" já encerrada), então o prazo decide.
     */
    fun pesquisas(
        pesquisas: JsonElement,
        perguntas: JsonElement,
        participacoes: JsonElement,
        idColaborador: Int,
        hoje: LocalDate = LocalDate.now()
    ): List<Pesquisa>? {
        val itens = lista(pesquisas, "pesquisas") ?: return null
        val totais = (lista(perguntas, "perguntas") ?: JsonArray())
            .mapNotNull { it.objeto()?.let(::idPesquisa) }
            .groupingBy { it }.eachCount()
        val minhas = HashMap<Int, Boolean>()
        (lista(participacoes, "participacoes") ?: JsonArray()).forEach { e ->
            val o = e.objeto() ?: return@forEach
            val dono = o.inteiro("id_colaborador") ?: relacionado(o, "colaborador")?.inteiro("id")
            val pesquisa = idPesquisa(o)
            if (dono == idColaborador && pesquisa != null) minhas[pesquisa] = o.logico("status") == true
        }
        return itens.mapNotNull { e ->
            val o = e.objeto() ?: return@mapNotNull null
            val id = o.inteiro("id") ?: return@mapNotNull null
            val respondida = minhas[id] ?: return@mapNotNull null
            val inicio = o.data("data_inicio") ?: return@mapNotNull null
            val fim = o.data("data_fim") ?: return@mapNotNull null
            if (Datas.parse(inicio)?.isAfter(hoje) == true) return@mapNotNull null
            Pesquisa(
                id = id,
                titulo = o.texto("titulo") ?: return@mapNotNull null,
                descricao = o.texto("descricao"),
                dataInicio = inicio,
                dataFim = fim,
                anonimo = o.logico("anonimo") ?: false,
                status = true,
                publicoSelecionado = o.texto("publico_selecionado") ?: "TODOS",
                totalPerguntas = totais[id],
                respondida = respondida
            )
        }
    }

    fun perguntas(json: JsonElement, idPesquisa: Int): List<Pergunta>? {
        val itens = lista(json, "perguntas") ?: return null
        return itens.mapNotNull { e ->
            val o = e.objeto() ?: return@mapNotNull null
            if (idPesquisa(o) != idPesquisa) return@mapNotNull null
            Pergunta(
                id = o.inteiro("id") ?: return@mapNotNull null,
                enunciado = o.texto("enunciado") ?: return@mapNotNull null,
                tipo = o.texto("tipo") ?: "TEXTO",
                ordem = o.inteiro("ordem") ?: 0,
                obrigatorio = o.logico("obrigatorio") ?: false,
                idPesquisa = idPesquisa
            )
        }.sortedBy { it.ordem }
    }

    /**
     * Feedbacks recebidos pelo titular. O Back-end devolve os de todos os colaboradores (sem escopo por perfil), então só
     * entram os do destinatário. O banco não tem o campo "lido": [vistos] são os ids já abertos neste aparelho.
     * Feedback com `ativo` = 0 foi desativado pelo RH e não aparece. Mais recentes primeiro.
     */
    fun feedbacks(json: JsonElement, idColaborador: Int, vistos: Set<Int>): List<Feedback>? {
        val itens = lista(json, "feedbacks") ?: return null
        return itens.mapNotNull { e ->
            val o = e.objeto() ?: return@mapNotNull null
            val destinatario = o.inteiro("id_colaborador_destinatario") ?: relacionado(o, "colaborador_destinatario")?.inteiro("id")
            if (destinatario != idColaborador) return@mapNotNull null
            if (o.logico("ativo") == false) return@mapNotNull null
            val id = o.inteiro("id") ?: return@mapNotNull null
            val remetente = relacionado(o, "colaborador_remetente")
            Feedback(
                id = id,
                idDestinatario = idColaborador,
                tipo = o.texto("tipo")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Formal",
                descricao = o.texto("descricao"),
                dataFeedback = o.data("data_feedback"),
                ativo = true,
                idRemetente = o.inteiro("id_colaborador_remetente") ?: remetente?.inteiro("id"),
                remetenteNome = remetente?.texto("nome"),
                remetenteCargo = relacionado(remetente ?: JsonObject(), "cargo")?.texto("nome"),
                lido = id in vistos
            )
        }.sortedWith(compareByDescending<Feedback> { it.dataFeedback.orEmpty() }.thenByDescending { it.id })
    }

    /**
     * Respostas do titular a um feedback. O Back-end devolve as de todos os colaboradores; entram só as do [idFeedback]
     * gravadas pelo titular e ativas, na ordem de gravação.
     */
    fun respostasFeedback(json: JsonElement, idFeedback: Int, idColaborador: Int): List<RespostaFeedback>? {
        val itens = lista(json, "respostas") ?: return null
        return itens.mapNotNull { e ->
            val o = e.objeto() ?: return@mapNotNull null
            if (o.inteiro("id_feedback") != idFeedback || o.inteiro("id_colaborador") != idColaborador) return@mapNotNull null
            if (o.logico("ativo") == false) return@mapNotNull null
            RespostaFeedback(
                id = o.inteiro("id") ?: return@mapNotNull null,
                descricao = o.texto("descricao").orEmpty(),
                idFeedback = idFeedback,
                idColaborador = idColaborador,
                dataResposta = o.data("data_resposta")
            )
        }.sortedBy { it.id }
    }

    /**
     * Notificações do titular (o Back-end devolve as de todos). O banco não guarda data/hora nem "lida": a leitura vem
     * de [lidas] (ids abertos neste aparelho) e a ordem é por id decrescente.
     */
    fun notificacoes(json: JsonElement, idColaborador: Int, lidas: Set<Int>): List<Notificacao>? {
        val itens = lista(json, "notificacoes") ?: return null
        return itens.mapNotNull { e ->
            val o = e.objeto() ?: return@mapNotNull null
            val dono = o.inteiro("id_colaborador") ?: relacionado(o, "colaborador")?.inteiro("id")
            if (dono != idColaborador) return@mapNotNull null
            val id = o.inteiro("id") ?: return@mapNotNull null
            Notificacao(
                id = id,
                idColaborador = idColaborador,
                tipoOrigem = o.texto("tipo_origem"),
                titulo = o.texto("titulo") ?: return@mapNotNull null,
                mensagem = o.texto("mensagem"),
                lida = id in lidas,
                dataHora = null
            )
        }.sortedByDescending { it.id }
    }

    private fun idPesquisa(o: JsonObject): Int? = o.inteiro("id_pesquisa") ?: relacionado(o, "pesquisa")?.inteiro("id")

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
