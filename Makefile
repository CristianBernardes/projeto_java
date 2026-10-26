.PHONY: help up up-db down build restart logs logs-app logs-db ps \
        shell-app shell-db test format lint clean migration

# ─── Variáveis ────────────────────────────────────────────────────────────────
COMPOSE     := docker compose
MVN_RUN     := $(COMPOSE) --profile dev run --rm maven mvn

# ─── Ajuda ────────────────────────────────────────────────────────────────────
help: ## Exibe esta ajuda
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

# ─── Docker ───────────────────────────────────────────────────────────────────
up: ## Sobe todos os serviços (build se necessário)
	$(COMPOSE) up -d --build

up-db: ## Sobe apenas o banco de dados
	$(COMPOSE) up -d postgres

down: ## Para e remove os containers
	$(COMPOSE) down

build: ## Reconstrói as imagens sem usar cache
	$(COMPOSE) build --no-cache

restart: ## Reinicia todos os serviços
	$(COMPOSE) restart

logs: ## Acompanha os logs de todos os serviços
	$(COMPOSE) logs -f

logs-app: ## Acompanha os logs apenas da aplicação
	$(COMPOSE) logs -f app

logs-db: ## Acompanha os logs apenas do banco
	$(COMPOSE) logs -f postgres

ps: ## Lista os containers em execução
	$(COMPOSE) ps

# ─── Shells ───────────────────────────────────────────────────────────────────
shell-app: ## Abre um shell no container da aplicação
	$(COMPOSE) exec app sh

shell-db: ## Abre o psql no container do banco
	$(COMPOSE) exec postgres psql -U java -d java

# ─── Maven (container dev com cache ~/.m2) ────────────────────────────────────
test: ## Executa os testes dentro do container Maven
	$(MVN_RUN) test

format: ## Formata o código com Spotless (Google Java Format)
	$(MVN_RUN) spotless:apply -q

lint: ## Verifica a formatação sem aplicar (falha se houver diff)
	$(MVN_RUN) spotless:check

clean: ## Remove artefatos de build
	$(MVN_RUN) clean -q

migration: ## Cria uma nova migration. Uso: make migration name=add_column_x
ifndef name
	$(error Informe o nome: make migration name=add_column_x)
endif
	@NEXT=$$(ls src/main/resources/db/migration/V*.sql 2>/dev/null | \
		grep -oP 'V\K[0-9]+' | sort -n | tail -1); \
	NEXT=$$((NEXT + 1)); \
	FILE="src/main/resources/db/migration/V$${NEXT}__$(name).sql"; \
	echo "-- Migration: $(name)" > $$FILE; \
	echo "-- Created at: $$(date -u +%Y-%m-%dT%H:%M:%SZ)" >> $$FILE; \
	echo "" >> $$FILE; \
	echo "Created: $$FILE"

