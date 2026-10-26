-- V1__create_produtos.sql
-- Migration inicial: cria a tabela de produtos.

CREATE TABLE produtos (
    id         BIGSERIAL    PRIMARY KEY,
    nome       VARCHAR(255) NOT NULL,
    descricao  TEXT,
    preco      NUMERIC(10, 2) NOT NULL,
    estoque    INTEGER      NOT NULL DEFAULT 0,
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em  TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_produtos_ativo ON produtos (ativo);
