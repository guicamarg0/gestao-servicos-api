create table empresa (
  id uuid primary key,
  unidade_id uuid not null references unidade(id),
  matriz_id uuid references empresa(id),
  tipo varchar(10) not null,
  razao_social varchar(160) not null,
  nome_fantasia varchar(160),
  cnpj varchar(14) not null,
  inscricao_estadual varchar(40),
  telefone varchar(30),
  email varchar(254),
  cep varchar(12),
  logradouro varchar(180),
  numero varchar(30),
  complemento varchar(120),
  bairro varchar(120),
  cidade varchar(120),
  estado varchar(2),
  ativo boolean not null default true,
  unique(unidade_id, cnpj)
);
alter table contrato add column empresa_id uuid references empresa(id);
