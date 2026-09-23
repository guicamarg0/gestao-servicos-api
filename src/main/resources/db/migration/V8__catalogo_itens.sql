create table item_catalogo (
    id uuid primary key,
    unidade_id uuid not null references unidade(id),
    tipo varchar(20) not null,
    nome varchar(120) not null,
    descricao varchar(2000),
    unidade_medida varchar(30) not null,
    unidade_medida_personalizada varchar(60),
    valor_padrao numeric(15, 2) not null,
    codigo_referencia varchar(80),
    ativo boolean not null,
    criado_em timestamp with time zone not null,
    atualizado_em timestamp with time zone not null,
    criado_por uuid,
    atualizado_por uuid,
    constraint ck_item_catalogo_tipo check (tipo in ('MAO_DE_OBRA', 'PECA', 'MATERIAL')),
    constraint ck_item_catalogo_unidade_medida check (unidade_medida in ('UNIDADE', 'HORA', 'DIARIA', 'METRO', 'METRO_QUADRADO', 'QUILOGRAMA', 'LITRO', 'OUTRA')),
    constraint ck_item_catalogo_unidade_personalizada check ((unidade_medida = 'OUTRA' and unidade_medida_personalizada is not null) or (unidade_medida <> 'OUTRA' and unidade_medida_personalizada is null))
);

create index ix_item_catalogo_unidade_nome on item_catalogo(unidade_id, nome);
