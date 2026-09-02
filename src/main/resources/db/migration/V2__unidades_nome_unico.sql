alter table unidade add column nome_normalizado varchar(120);

update unidade
set nome_normalizado = translate(
    lower(regexp_replace(trim(nome), '[[:space:]]+', ' ', 'g')),
    'áàâãäéèêëíìîïóòôõöúùûüçñ',
    'aaaaaeeeeiiiiooooouuuucn'
);

alter table unidade alter column nome_normalizado set not null;

alter table unidade
    add constraint uk_unidade_nome_normalizado unique (nome_normalizado);
