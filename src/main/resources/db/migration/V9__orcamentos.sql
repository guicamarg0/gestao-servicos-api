create table sequencia_orcamento (
    unidade_id uuid not null references unidade(id),
    ano integer not null,
    proximo_numero bigint not null,
    primary key (unidade_id, ano)
);

create table orcamento (
    id uuid primary key,
    unidade_id uuid not null references unidade(id),
    numero varchar(30) not null,
    revisao_atual integer not null,
    status varchar(20) not null,
    emitido_inicialmente boolean not null default false,
    versao bigint not null default 0,
    criado_em timestamp with time zone not null,
    atualizado_em timestamp with time zone not null,
    criado_por uuid,
    atualizado_por uuid,
    constraint uk_orcamento_unidade_numero unique (unidade_id, numero),
    constraint ck_orcamento_status check (status in ('RASCUNHO', 'EMITIDO', 'APROVADO', 'RECUSADO', 'CANCELADO'))
);
create index ix_orcamento_unidade_status on orcamento(unidade_id, status);

create table revisao_orcamento (
    id uuid primary key,
    orcamento_id uuid not null references orcamento(id),
    numero_revisao integer not null,
    status varchar(20) not null,
    contratante_id uuid references contratante(id),
    validade date,
    condicoes_pagamento varchar(2000),
    observacoes_comerciais varchar(4000),
    exibir_assinatura boolean not null default false,
    desconto_tipo varchar(12) not null,
    desconto_valor numeric(15,2) not null default 0,
    acrescimo_tipo varchar(12) not null,
    acrescimo_valor numeric(15,2) not null default 0,
    subtotal_servicos numeric(15,2) not null default 0,
    subtotal_materiais numeric(15,2) not null default 0,
    subtotal numeric(15,2) not null default 0,
    desconto_total numeric(15,2) not null default 0,
    acrescimo_total numeric(15,2) not null default 0,
    total_final numeric(15,2) not null default 0,
    contratante_snapshot varchar(8000),
    contratado_snapshot varchar(8000),
    pagamentos_snapshot varchar(8000),
    versao bigint not null default 0,
    criado_em timestamp with time zone not null,
    atualizado_em timestamp with time zone not null,
    criado_por uuid,
    atualizado_por uuid,
    constraint uk_revisao_orcamento unique (orcamento_id, numero_revisao),
    constraint ck_revisao_orcamento_status check (status in ('RASCUNHO', 'EMITIDO', 'APROVADO', 'RECUSADO', 'CANCELADO')),
    constraint ck_revisao_orcamento_desconto_tipo check (desconto_tipo in ('VALOR', 'PERCENTUAL')),
    constraint ck_revisao_orcamento_acrescimo_tipo check (acrescimo_tipo in ('VALOR', 'PERCENTUAL'))
);

create table item_revisao_orcamento (
    id uuid primary key,
    revisao_orcamento_id uuid not null references revisao_orcamento(id),
    ordem integer not null,
    item_catalogo_id uuid references item_catalogo(id),
    tipo varchar(20) not null,
    descricao varchar(2000) not null,
    unidade_medida varchar(30) not null,
    unidade_medida_personalizada varchar(60),
    quantidade numeric(15,2) not null,
    valor_unitario numeric(15,2) not null,
    desconto numeric(15,2) not null default 0,
    total numeric(15,2) not null,
    constraint uk_item_revisao_ordem unique (revisao_orcamento_id, ordem),
    constraint ck_item_revisao_tipo check (tipo in ('MAO_DE_OBRA', 'PECA', 'MATERIAL')),
    constraint ck_item_revisao_medida check (unidade_medida in ('UNIDADE', 'HORA', 'DIARIA', 'METRO', 'METRO_QUADRADO', 'QUILOGRAMA', 'LITRO', 'OUTRA'))
);

create table evento_orcamento (
    id uuid primary key,
    orcamento_id uuid not null references orcamento(id),
    revisao_orcamento_id uuid references revisao_orcamento(id),
    tipo varchar(40) not null,
    observacao varchar(2000),
    ocorrido_em timestamp with time zone not null,
    usuario_id uuid
);
create index ix_evento_orcamento_data on evento_orcamento(orcamento_id, ocorrido_em);
