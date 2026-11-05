# AVALON — Mobile

Aplicativo mobile da plataforma **AVALON**, desenvolvido pela equipe **Nexus**, seguindo os requisitos funcionais, não funcionais e regras de negócio documentados no [repositório central](https://github.com/Daniele-SS/NEXUS).

## Status

Telas das Sprints 1 a 4 implementadas em Kotlin + Jetpack Compose, seguindo o protótipo do Figma e o contrato OpenAPI do Back-end (`openapi.yaml`, v2.1.0).

- **API e banco reais:** login e recuperação de senha, Perfil, Jornada e escala, Férias, Benefícios e Documentos (`MODULOS_API`).
- **Dados de demonstração** (até o Back-end publicar as rotas): Feedbacks, Pesquisas, Notificações, Dashboard e Mural de avisos, correção cadastral e download de documentos.

Telas por Sprint, fluxos, arquivos, usuários de teste e histórico de mudanças: [`docs/DOCUMENTACAO.md`](docs/DOCUMENTACAO.md).

### Executar

1. Abrir no Android Studio e rodar o módulo `app` (minSdk 27).
2. Subir o banco e a API (porta 8080). Login com o e-mail de qualquer usuário da carga inicial (scripts `BD01` a `BD04`), por exemplo `rafael.martins@empresa.com` / `123456`. Lista completa de usuários e senhas: [Usuários para teste](docs/DOCUMENTACAO.md#usuários-para-teste).
3. Emulador acessa a API em `10.0.2.2`; em celular físico, trocar `API_BASE_URL` pelo IP da máquina.
4. Sem API: `USE_MOCK_AUTH = true` e login de demonstração `ana.lucia@empresa.com.br` / `123456`.

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
