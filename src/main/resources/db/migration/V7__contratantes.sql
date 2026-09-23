create table contratante (
    id uuid primary key,
    unidade_id uuid not null references unidade(id),
    tipo varchar(2) not null,
    nome_razao_social varchar(120) not null,
    nome_fantasia varchar(120),
    documento varchar(14) not null,
    endereco varchar(500),
    observacao_interna varchar(2000),
    ativo boolean not null,
    criado_em timestamp with time zone not null,
    atualizado_em timestamp with time zone not null,
    criado_por uuid,
    atualizado_por uuid,
    constraint ck_contratante_tipo check (tipo in ('PF', 'PJ')),
    constraint uk_contratante_unidade_documento unique (unidade_id, documento)
);

create index ix_contratante_unidade_nome on contratante(unidade_id, nome_razao_social);

create table contato_contratante (
    id uuid primary key,
    contratante_id uuid not null references contratante(id),
    nome varchar(120) not null,
    telefone varchar(30),
    email varchar(254),
    constraint ck_contato_contratante_meio check (telefone is not null or email is not null)
);

create index ix_contato_contratante_contratante on contato_contratante(contratante_id);
