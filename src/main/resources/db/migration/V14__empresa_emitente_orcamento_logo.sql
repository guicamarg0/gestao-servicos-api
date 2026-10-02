alter table empresa add column logo_url text;
alter table revisao_orcamento add column empresa_id uuid references empresa(id);
alter table revisao_orcamento alter column contratado_snapshot type text;
