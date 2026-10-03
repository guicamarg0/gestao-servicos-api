CREATE TABLE despesa_item (
    despesa_id UUID NOT NULL REFERENCES despesa(id),
    ordem INTEGER NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    quantidade NUMERIC(15,4) NOT NULL,
    valor_unitario NUMERIC(15,2) NOT NULL,
    PRIMARY KEY(despesa_id, ordem)
);
INSERT INTO despesa_item(despesa_id, ordem, descricao, quantidade, valor_unitario)
SELECT id, 0, descricao, 1, valor FROM despesa;
