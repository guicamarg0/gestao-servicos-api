ALTER TABLE arquivo ALTER COLUMN caminho DROP NOT NULL;
ALTER TABLE arquivo ADD COLUMN conteudo BYTEA;
ALTER TABLE arquivo ADD CONSTRAINT ck_arquivo_conteudo_ou_storage
    CHECK (conteudo IS NOT NULL OR caminho IS NOT NULL);
