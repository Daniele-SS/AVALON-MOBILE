# AVALON — Mobile

Aplicativo mobile da plataforma **AVALON**, desenvolvido pela equipe **Nexus**, seguindo os requisitos funcionais, não funcionais e regras de negócio documentados no [repositório central](https://github.com/Daniele-SS/NEXUS).

## Status

Telas das Sprints 1 a 4 implementadas em Kotlin + Jetpack Compose, seguindo o protótipo do Figma e o contrato OpenAPI do Back-end (`openapi.yaml`, v2.1.0). O login usa a API e o banco reais (`USE_MOCK_AUTH = false`); os módulos das Sprints 2–4 seguem com dados de demonstração (`USE_MOCK_API = true`) até o Back-end publicar as rotas.

| Sprint | Itens do planejamento | Telas / implementação |
|---|---|---|
| 1 | 6.1 ambiente; 6.2.1 login com validação de status da conta | Início, Login (`CREDENCIAIS_INVALIDAS` x `CONTA_INATIVA`) |
| 2 | 6.2.2 recuperação de senha; 6.2.3 token seguro e renovação; 6.5.1 perfil; 6.5.3 jornada | Código por e-mail, Redefinir senha, sessão cifrada (Android Keystore AES-GCM) com refresh e rotação, Perfil, Jornada e escala |
| 3 | 6.4 férias; 6.5.4 documentos/holerites; 6.5.2 correção cadastral; 6.7 feedback | Férias (saldo + solicitação), Documentos, Downloads, Benefícios, Solicitar correção, Feedbacks e resposta |
| 4 | 6.6 pesquisas/avaliações com alerta de obrigatórias; 6.3.1–6.3.4 dashboard, mural, atalhos e notificações | Pesquisas, Responder/Revisar (anônima), Riscos psicossociais (NR-1), Home, Mural de avisos, Notificações, Mais |

Documentação completa do código (pacotes, arquivos, classes, fluxos): [`docs/DOCUMENTACAO.md`](docs/DOCUMENTACAO.md).

### Executar

1. Abrir no Android Studio e rodar o módulo `app` (minSdk 27).
2. Subir o banco e a API (porta 8080). Login com usuários da carga inicial, por exemplo `rafael.martins` / `123456`. Lista completa de usuários e senhas: [Usuários para teste](docs/DOCUMENTACAO.md#usuários-para-teste).
3. Emulador acessa a API em `10.0.2.2`; em celular físico, trocar `API_BASE_URL` pelo IP da máquina.
4. Sem API: `USE_MOCK_AUTH = true` e login de demonstração `ana.lucia` / `123456`.

### Estrutura

```
model/        modelos do contrato OpenAPI
data/         Retrofit, sessão segura, renovação de token, repositórios (remoto e demonstração)
viewmodel/    estado de cada tela
ui/           tema, componentes e telas por módulo
navigation/   rotas e barra inferior
```

## Protótipo (Figma)

| Plataforma | Link |
|---|---|
| Mobile | [PROTÓTIPO MOBILE / Usuário](https://www.figma.com/design/FeIClfB0f8s3A4SUXyKFll/Prot%C3%B3tipo---Nexus-RH?node-id=0-1&t=d4wus0k8oqgZov16-1) |

## Equipe

| Nome | Função |
|---|---|
| Daniele Silva | Desenvolvedora Mobile |
