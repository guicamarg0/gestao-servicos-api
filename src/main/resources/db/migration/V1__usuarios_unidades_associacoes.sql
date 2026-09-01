create table usuario (
    id uuid primary key,
    nome varchar(120) not null,
    email varchar(254) not null,
    senha_hash varchar(200) not null,
    criado_em timestamp with time zone not null,
    constraint uk_usuario_email unique (email)
);

create table unidade (
    id uuid primary key,
    nome varchar(120) not null,
    criada_em timestamp with time zone not null
);

create table associacao (
    id uuid primary key,
    usuario_id uuid not null references usuario(id),
    unidade_id uuid not null references unidade(id),
    perfil varchar(20) not null,
    criada_em timestamp with time zone not null,
    constraint ck_associacao_perfil check (perfil in ('ADMIN', 'GESTOR', 'OPERADOR', 'CONSULTA')),
    constraint uk_associacao_usuario_unidade unique (usuario_id, unidade_id)
);

create index ix_associacao_unidade_id on associacao(unidade_id);
create index ix_associacao_usuario_id on associacao(usuario_id);
