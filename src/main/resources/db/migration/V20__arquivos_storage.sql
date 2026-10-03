ALTER TABLE pdf_orcamento ALTER COLUMN conteudo DROP NOT NULL;
ALTER TABLE pdf_orcamento ADD COLUMN caminho_storage VARCHAR(500);
CREATE TABLE arquivo (
    id UUID PRIMARY KEY,
    unidade_id UUID NOT NULL REFERENCES unidade(id),
    nome VARCHAR(200) NOT NULL,
    tipo VARCHAR(100) NOT NULL,
    caminho VARCHAR(500) NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_arquivo_unidade ON arquivo(unidade_id);
