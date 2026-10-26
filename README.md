# Projeto Java — API REST de Estudo

API REST construída com **Spring Boot 3**, **JPA/Hibernate**, **PostgreSQL** e **Flyway**, totalmente containerizada com **Docker** e **Docker Compose**.

Projeto de estudo focado na stack da vaga: Java · Spring Boot · Hibernate · PostgreSQL · APIs REST · Docker · Clean Code · SOLID · DDD · Design Patterns.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.5 |
| ORM | Spring Data JPA / Hibernate |
| Banco de dados | PostgreSQL 14 |
| Migrations | Flyway |
| Documentação | SpringDoc OpenAPI 3 (Swagger UI) |
| Formatação | Spotless + Google Java Format |
| Build | Maven 3.9 |
| Container | Docker + Docker Compose |

---

## Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/) ≥ 24
- [Docker Compose](https://docs.docker.com/compose/) ≥ 2.20
- `make`

> **Tudo roda dentro do Docker.** Não é necessário ter Java, Maven ou PostgreSQL instalados na máquina.

---

## Início rápido

```bash
# 1. Sobe banco + aplicação (faz o build automaticamente)
make up

# 2. Acompanha os logs da aplicação
make logs-app
```

A API estará disponível em **http://localhost:8088**.

---

## Comandos disponíveis (`make help`)

```
  up                Sobe todos os serviços (build se necessário)
  up-db             Sobe apenas o banco de dados
  down              Para e remove os containers
  build             Reconstrói as imagens sem usar cache
  restart           Reinicia todos os serviços
  logs              Acompanha os logs de todos os serviços
  logs-app          Acompanha os logs apenas da aplicação
  logs-db           Acompanha os logs apenas do banco
  ps                Lista os containers em execução
  shell-app         Abre um shell no container da aplicação
  shell-db          Abre o psql no container do banco
  test              Executa os testes dentro do container Maven
  format            Formata o código com Spotless (Google Java Format)
  lint              Verifica a formatação sem aplicar (falha se houver diff)
  clean             Remove artefatos de build
```

---

## Documentação da API

Com a aplicação rodando, acesse:

| URL | Descrição |
|---|---|
| http://localhost:8088/swagger-ui.html | Swagger UI interativo |
| http://localhost:8088/api-docs | Spec OpenAPI 3 em JSON |
| http://localhost:8088/actuator/health | Health check |

---

## Endpoints

### Produtos — `GET /api/v1/produtos`

Lista todos os produtos ativos.

**Resposta `200`:**
```json
[
  {
    "id": 1,
    "nome": "Teclado Mecânico",
    "descricao": "Switch Cherry MX Red, RGB, ABNT2",
    "preco": 299.90,
    "estoque": 10,
    "ativo": true,
    "criadoEm": "2026-10-26T12:00:00",
    "atualizadoEm": "2026-10-26T12:00:00"
  }
]
```

---

### `GET /api/v1/produtos/{id}`

Busca um produto pelo ID.

| Código | Descrição |
|---|---|
| `200` | Produto encontrado |
| `404` | Produto não encontrado |

---

### `POST /api/v1/produtos`

Cria um novo produto.

**Body:**
```json
{
  "nome": "Teclado Mecânico",
  "descricao": "Switch Cherry MX Red, RGB, ABNT2",
  "preco": 299.90,
  "estoque": 10
}
```

| Código | Descrição |
|---|---|
| `201` | Produto criado — header `Location` aponta para o recurso |
| `422` | Dados inválidos |

---

### `PUT /api/v1/produtos/{id}`

Atualiza nome, descrição e preço de um produto.

**Body:**
```json
{
  "nome": "Teclado Mecânico Pro",
  "descricao": "Switch Cherry MX Blue, RGB, ABNT2",
  "preco": 349.90
}
```

| Código | Descrição |
|---|---|
| `200` | Produto atualizado |
| `404` | Produto não encontrado |
| `422` | Dados inválidos |

---

### `DELETE /api/v1/produtos/{id}`

Desativa um produto (**soft delete** — não remove do banco).

| Código | Descrição |
|---|---|
| `204` | Produto desativado |
| `404` | Produto não encontrado |

---

## Estrutura do projeto

```
src/
├── main/
│   ├── java/br/com/projetojava/
│   │   ├── ProjetoJavaApplication.java
│   │   ├── config/
│   │   │   └── OpenApiConfig.java          # Configuração Swagger/OpenAPI
│   │   ├── produto/
│   │   │   ├── controller/ProdutoController.java
│   │   │   ├── domain/Produto.java          # Entidade rica (regras de negócio)
│   │   │   ├── dto/
│   │   │   │   ├── CriarProdutoRequest.java
│   │   │   │   ├── AtualizarProdutoRequest.java
│   │   │   │   └── ProdutoResponse.java
│   │   │   ├── repository/ProdutoRepository.java
│   │   │   └── service/ProdutoService.java
│   │   └── shared/
│   │       ├── exception/RecursoNaoEncontradoException.java
│   │       └── handler/GlobalExceptionHandler.java
│   └── resources/
│       ├── application.yml
│       ├── application-docker.yml
│       └── db/migration/
│           └── V1__create_produtos.sql
└── test/
    └── java/br/com/projetojava/
        ├── produto/controller/ProdutoControllerTest.java
        └── produto/service/ProdutoServiceTest.java
```

---

## Portas utilizadas

| Serviço | Porta host |
|---|---|
| API (app) | `8088` |
| PostgreSQL | `5433` |

---

## Variáveis de ambiente

Configuradas no `docker-compose.yml`. Para sobrescrever localmente, crie um arquivo `.env` na raiz:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/java
SPRING_DATASOURCE_USERNAME=java
SPRING_DATASOURCE_PASSWORD=java
SPRING_PROFILES_ACTIVE=docker
```

---

## Formatação de código

O projeto usa **Spotless** com **Google Java Format**. Antes de commitar:

```bash
make format   # aplica a formatação
make lint     # verifica sem aplicar (útil no CI)
```

---

## Testes

```bash
make test
```

Os testes unitários rodam dentro do container Maven (sem precisar do banco):

- `ProdutoServiceTest` — 3 testes com Mockito
- `ProdutoControllerTest` — 3 testes com `@WebMvcTest`
