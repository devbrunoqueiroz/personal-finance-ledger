# personal-finance-ledger

API backend para gerenciamento de finanças pessoais.

O objetivo do projeto é fornecer uma base sólida para registrar, consultar e evoluir o controle financeiro pessoal (receitas, despesas, categorias, autenticação e regras de domínio), com foco em arquitetura limpa e testabilidade.

## O que já existe no projeto

- API REST em Java 21 com Spring Boot 4.
- Arquitetura em camadas com separação entre:
  - `domain`
  - `application` (ports/use cases)
  - `adapters` (inbound/outbound)
- Autenticação com JWT.
- Segurança stateless com Spring Security.
- Cadastro e login de usuário implementados.
- Persistência com Spring Data JPA + PostgreSQL.
- Migrações versionadas com Flyway.
- Documentação de API com OpenAPI/Swagger habilitada.
- Actuator básico habilitado (`health` e `info`).
- Cobertura de testes com JUnit/Mockito e geração de relatório Jacoco.

## Diagrama simples da arquitetura

```text
[Cliente HTTP]
      |
      v
[Controllers REST (adapters/inbound/web)]
      |
      v
[Use Cases (application/usecases)]
      |
      v
[Ports (application/ports)] <--------------------+
      |                                           |
      v                                           |
[Adapters Outbound (JWT, BCrypt, JPA)]            |
      |                                           |
      +--> [Banco PostgreSQL + Flyway]            |
                                                  |
[Domain (entidades e regras de negocio)] ---------+
```

Fluxo principal: entrada HTTP -> controller -> use case -> port -> adapter -> infraestrutura.

## Endpoints disponíveis hoje

### Públicos

- `POST /auth/register` - registra usuário e retorna token.
- `POST /auth/login` - autentica usuário e retorna token.
- `GET /v3/api-docs/**` e `GET /swagger-ui/**` - documentação.

### Protegidos

- Rotas fora de `/auth/**` e docs exigem token Bearer.

## Banco de dados e migrações

Atualmente existem migrações para:

- `users`
- `categories`
- `transactions`

Arquivos em `src/main/resources/db/migration`.

## Estado atual de funcionalidades

- Autenticação e usuários: funcional.
- Módulo de transações: estrutura de domínio/persistência existente, controller/use case ainda em evolução.
- Há testes cobrindo domínio, adaptadores e fluxo de autenticação.

## Como rodar localmente

## Pré-requisitos

- Java 21
- Docker (opcional, para subir PostgreSQL)
- Maven Wrapper (`mvnw` já incluído no projeto)

## 1) Subir banco PostgreSQL

```bash
docker compose up -d
```

O `compose.yaml` sobe um PostgreSQL 16 em `localhost:5432` com:

- database: `financeiro`
- user: `financeiro`
- password: `financeiro`

## 2) Executar a aplicação

No Windows:

```bash
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

## 3) Acessar

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Health: `http://localhost:8080/actuator/health`

## Configuração por variáveis de ambiente

Valores padrão em `application.properties`:

- `DB_URL=jdbc:postgresql://localhost:5432/financeiro`
- `DB_USER=financeiro`
- `DB_PASSWORD=financeiro`
- `jwt.secret` configurado localmente
- `jwt.ttl-seconds=3600`

## Build e testes

```bash
.\mvnw.cmd test
```

Para gerar relatório de cobertura:

```bash
.\mvnw.cmd -Pcoverage verify
```

## Próximos passos naturais do projeto

- Finalizar casos de uso e endpoints de transações.
- Adicionar CRUD completo de categorias.
- Melhorar tratamento de erros no filtro de segurança (retorno 401 consistente).
- Evoluir observabilidade (métricas e tracing).
- Endurecer configuração para produção (segredos e perfis).

