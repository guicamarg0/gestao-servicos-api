create table modelo_contrato (
    id uuid primary key,
    unidade_id uuid not null references unidade(id),
    nome varchar(120) not null,
    descricao varchar(500),
    conteudo text not null,
    status varchar(20) not null,
    numero_versao integer not null,
    versao bigint not null default 0,
    criado_em timestamp with time zone not null,
    atualizado_em timestamp with time zone not null,
    criado_por uuid,
    atualizado_por uuid,
    constraint uk_modelo_contrato_nome unique (unidade_id, nome),
    constraint ck_modelo_contrato_status check (status in ('RASCUNHO','PUBLICADO','ARQUIVADO'))
);
create index ix_modelo_contrato_unidade_status on modelo_contrato(unidade_id, status);

create table modelo_contrato_versao (
    id uuid primary key,
    modelo_contrato_id uuid not null references modelo_contrato(id),
    numero integer not null,
    conteudo text not null,
    criado_em timestamp with time zone not null,
    atualizado_em timestamp with time zone not null,
    criado_por uuid,
    atualizado_por uuid,
    constraint uk_modelo_contrato_versao unique (modelo_contrato_id, numero)
);

alter table contrato add column modelo_contrato_id uuid references modelo_contrato(id);
alter table contrato add column conteudo_rascunho text;

create table contrato_snapshot (
    id uuid primary key,
    contrato_id uuid not null references contrato(id),
    numero_versao integer not null,
    conteudo text not null,
    criado_em timestamp with time zone not null,
    atualizado_em timestamp with time zone not null,
    criado_por uuid,
    atualizado_por uuid,
    constraint uk_contrato_snapshot unique (contrato_id, numero_versao)
);
