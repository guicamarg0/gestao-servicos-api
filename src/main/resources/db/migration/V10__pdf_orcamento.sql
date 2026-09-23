create table pdf_orcamento (
    id uuid primary key,
    revisao_orcamento_id uuid not null unique references revisao_orcamento(id),
    nome_arquivo varchar(160) not null,
    conteudo bytea not null,
    gerado_em timestamp with time zone not null
);
