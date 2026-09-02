create table convite_unidade (
    id uuid primary key,
    unidade_id uuid not null references unidade(id),
    email varchar(254) not null,
    perfil varchar(20) not null,
    token_hash varchar(64) not null unique,
    expira_em timestamp with time zone not null,
    aceita_em timestamp with time zone,
    revogada_em timestamp with time zone,
    criada_em timestamp with time zone not null,
    constraint ck_convite_unidade_perfil check (perfil in ('ADMIN', 'GESTOR', 'OPERADOR', 'CONSULTA'))
);
create index ix_convite_unidade_ativo on convite_unidade(unidade_id, revogada_em, aceita_em);
