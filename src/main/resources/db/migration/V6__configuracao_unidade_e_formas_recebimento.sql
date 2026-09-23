create table configuracao_unidade (
    id uuid primary key,
    unidade_id uuid not null unique references unidade(id),
    nome_razao_social varchar(120) not null,
    documento varchar(14) not null,
    endereco_completo varchar(500) not null,
    nome_fantasia varchar(120),
    email varchar(254),
    logo_url varchar(500),
    responsavel varchar(120),
    condicoes_pagamento_padrao varchar(2000)
);

create table forma_recebimento (
    id uuid primary key,
    configuracao_unidade_id uuid not null references configuracao_unidade(id),
    tipo varchar(20) not null,
    nome_exibicao varchar(120) not null,
    instrucoes varchar(1000) not null,
    ativa boolean not null,
    padrao boolean not null,
    constraint ck_forma_recebimento_tipo check (tipo in ('PIX', 'CONTA_BANCARIA', 'OUTRO'))
);

create index ix_forma_recebimento_configuracao on forma_recebimento(configuracao_unidade_id);
