ALTER TABLE usuario ADD COLUMN versao_sessao INTEGER NOT NULL DEFAULT 0;
CREATE TABLE recuperacao_senha (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuario(id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    usado_em TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_recuperacao_usuario ON recuperacao_senha(usuario_id, criado_em);
