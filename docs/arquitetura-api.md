# Convenções técnicas da API

## Organização de módulos

Cada módulo mantém `controller`, `service`, `repository`, `model`, `dto` e `mapper`. Controllers recebem `RequestDTO`s e retornam somente `ResponseDTO`s; entidades JPA não fazem parte dos contratos HTTP.

## Erros

Erros usam `ProblemDetail` e expõem `code` estável, além de `path`. Regras de negócio podem definir códigos específicos; para erros HTTP genéricos são usados `REQUISICAO_INVALIDA`, `NAO_AUTENTICADO`, `ACESSO_NEGADO`, `RECURSO_NAO_ENCONTRADO` e `CONFLITO_DE_REGRA`. Falhas de Bean Validation usam `VALIDACAO_INVALIDA`.

## Recursos reutilizáveis

`PaginaResponseDTO` adapta `Page<T>` para listas paginadas. Entidades novas que precisem de metadados podem estender `Auditavel`; o auditor é resolvido do usuário autenticado, sem substituir a auditoria de eventos de domínio.
