# personal-finance-ledger

API backend para gerenciamento de **finanças pessoais**, incluindo autenticação, categorias e transações.

---

## Status atual (verificado em 26/02/2026)

- Stack principal: **Java 21 + Spring Boot 4.0.2**
- Arquitetura em camadas: `domain`, `application`, `adapters` (Ports & Use Cases)
- Banco de dados: PostgreSQL + Flyway
  - `V1__users`
  - `V2__categories`
  - `V3__transactions`
- Segurança: JWT + Spring Security (stateless)
- OpenAPI/Swagger habilitado
- Actuator expõe:
  - `/actuator/health`
  - `/actuator/info`
  (Atualmente requer autenticação)

### Testes

Resultado de `.\mvnw.cmd test` em 26/02/2026:

- **101 testes executados**
- **0 falhas**
- **0 erros**
- **0 ignorados**

---

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

## Endpoints implementados

### Públicos

- `POST /auth/register`
- `POST /auth/login`
- `GET /v3/api-docs/**`
- `GET /swagger-ui/**`

### Protegidos (Bearer Token)

#### Categorias

- `POST /categories`
- `GET /categories`
- `GET /categories/{id}`
- `DELETE /categories/{id}`

#### Transações

- `POST /transactions`
- `GET /transactions`
- `PATCH /transactions/{id}`
- `POST /transactions/{id}/settle`
- `DELETE /transactions/{id}`

---

## Autenticação (fluxo rápido)

### 1) Registrar usuário

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Bruno","email":"bruno@email.com","password":"123456"}'
```
### 2) Login (retorna Access Token)

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"bruno@email.com","password":"123456"}'
```

## Exemplos de resposta:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
### 3) Usar token nos endpoints protegidos

```bash
curl http://localhost:8080/categories \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

---

## Como rodar localmente

### Pré-requisitos

- Java 21
- Docker

### Subir PostgreSQL

```bash
docker compose up -d
```

Configuração do compose:

- Host: `localhost:5432`
- Database: `financeiro`
- User: `financeiro`
- Password: `financeiro`

### Rodar a API

Windows:

```bash
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

### Acessos

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Actuator:
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/info`

## Configuração

As propriedades podem ser definidas via `application.properties` ou variáveis de ambiente.

Banco:

- `DB_URL` (default: `jdbc:postgresql://localhost:5432/financeiro`)
- `DB_USER` (default: `financeiro`)
- `DB_PASSWORD` (default: `financeiro`)

JWT:

- `jwt.secret`
- `jwt.ttl-seconds` (default: `3600`)

## Testes e cobertura

Executar testes:

```bash
.\mvnw.cmd test
```

Gerar relatório de cobertura (JaCoCo):

```bash
.\mvnw.cmd -Pcoverage verify
```
