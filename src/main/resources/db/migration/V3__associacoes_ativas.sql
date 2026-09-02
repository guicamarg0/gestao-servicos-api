alter table associacao add column ativa boolean not null default true;

create index ix_associacao_unidade_ativa on associacao(unidade_id, ativa);
