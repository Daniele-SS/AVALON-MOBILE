# AVALON — Mobile

Aplicativo mobile da plataforma **AVALON**, desenvolvido pela equipe **Nexus**, seguindo os requisitos funcionais, não funcionais e regras de negócio documentados no [repositório central](https://github.com/Daniele-SS/NEXUS).

## Status

Telas das Sprints 1 a 4 implementadas em Kotlin + Jetpack Compose, seguindo o protótipo do Figma e o contrato OpenAPI do Back-end (`openapi.yaml`, v2.1.0). Enquanto o Back-end responde `501` nas rotas do contrato, o app roda com dados de demonstração (`USE_MOCK_API = true`).

| Sprint | Itens do planejamento | Telas / implementação |
|---|---|---|
| 1 | 6.1 ambiente; 6.2.1 login com validação de status da conta | Início, Login (`CREDENCIAIS_INVALIDAS` x `CONTA_INATIVA`) |
| 2 | 6.2.2 recuperação de senha; 6.2.3 token seguro e renovação; 6.5.1 perfil; 6.5.3 jornada | Código por e-mail, Redefinir senha, sessão cifrada (Android Keystore AES-GCM) com refresh e rotação, Perfil, Jornada e escala |
| 3 | 6.4 férias; 6.5.4 documentos/holerites; 6.5.2 correção cadastral; 6.7 feedback | Férias (saldo + solicitação), Documentos, Downloads, Benefícios, Solicitar correção, Feedbacks e resposta |
| 4 | 6.6 pesquisas/avaliações com alerta de obrigatórias; 6.3.1–6.3.4 dashboard, mural, atalhos e notificações | Pesquisas, Responder/Revisar (anônima), Riscos psicossociais (NR-1), Home, Mural de avisos, Notificações, Mais |

Documentação completa do código (pacotes, arquivos, classes, fluxos): [`docs/DOCUMENTACAO.md`](docs/DOCUMENTACAO.md).

### Executar

1. Abrir no Android Studio e rodar o módulo `app` (minSdk 27).
2. Modo demonstração (padrão): login `ana.lucia` / senha `123456`. O login `inativo` / `123456` demonstra a conta inativa.
3. API real: em `app/build.gradle.kts`, `USE_MOCK_API = false` e `API_BASE_URL` com o IP da máquina da API (`10.0.2.2` no emulador).

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
