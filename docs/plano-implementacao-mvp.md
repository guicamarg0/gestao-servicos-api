# Plano de implementação do MVP

Este roteiro organiza as issues do GitHub por dependência. Os detalhes de requisitos ficam em [requisitos-mvp.md](requisitos-mvp.md); a issue é a unidade de execução e revisão.

## Regras de execução

1. Uma issue por branch, com nome descritivo.
2. Implementação, testes e revisão não devem ser combinados na mesma tarefa de agente quando houver risco de regra de negócio.
3. Toda alteração de banco usa Flyway e possui teste de isolamento multiunidade.
4. Toda tela nova executa `npm test` e `npm run build`; toda alteração da API executa `mvn test`.
5. Pull requests referenciam a issue, documentam contrato API e não misturam escopo.

## Fase 1 — Fundação

| Ordem | Repositório | Issue | Resultado |
| --- | --- | --- | --- |
| 1 | API | [#1](https://github.com/guicamarg0/gestao-servicos-api/issues/1) | MVC, DTOs, mappers, erros, paginação e auditoria reutilizável. |
| 2 | Web | [#1](https://github.com/guicamarg0/gestao-servicos-web/issues/1) | Componentes compartilhados de CRUD, busca e cadastro rápido. |

As duas issues podem ser implementadas em paralelo. A revisão deve confirmar que não alteraram contratos existentes indevidamente.

## Fase 2 — Dados-base da unidade

| Ordem | Repositório | Issue | Dependência |
| --- | --- | --- | --- |
| 3 | API | [#2](https://github.com/guicamarg0/gestao-servicos-api/issues/2) | API #1 |
| 4 | Web | [#2](https://github.com/guicamarg0/gestao-servicos-web/issues/2) | Web #1 e API #2 |

Entrega: identificação do contratado, logo, condições padrão e múltiplas formas de recebimento.

## Fase 3 — Cadastros comerciais

| Ordem | Repositório | Issue | Dependência |
| --- | --- | --- | --- |
| 5 | API | [#3](https://github.com/guicamarg0/gestao-servicos-api/issues/3) | API #1 |
| 6 | API | [#4](https://github.com/guicamarg0/gestao-servicos-api/issues/4) | API #1 |
| 7 | Web | [#3](https://github.com/guicamarg0/gestao-servicos-web/issues/3) | Web #1 e API #3 |
| 8 | Web | [#4](https://github.com/guicamarg0/gestao-servicos-web/issues/4) | Web #1 e API #4 |

As APIs de contratantes e catálogo podem seguir em paralelo. Cada interface só começa quando seu contrato de API estiver revisado.

## Fase 4 — Orçamentos e PDFs

| Ordem | Repositório | Issue | Dependência |
| --- | --- | --- | --- |
| 9 | API | [#5](https://github.com/guicamarg0/gestao-servicos-api/issues/5) | API #1, #2, #3 e #4 |
| 10 | Web | [#5](https://github.com/guicamarg0/gestao-servicos-web/issues/5) | Web #1, #2, #3, #4 e API #5 |
| 11 | API | [#6](https://github.com/guicamarg0/gestao-servicos-api/issues/6) | API #5 |

Antes da interface de PDF, a issue de API #6 deve definir o endpoint de download e o contrato de visualização. PDFs devem ser preservados por revisão emitida.

## Papéis de agentes por issue

| Papel | Responsabilidade |
| --- | --- |
| Planejamento/revisão de contrato | Confirmar design, dependências, DTOs, regras e critérios de aceite antes do código. |
| Implementação | Trabalhar somente na issue, em branch própria, com commits pequenos. |
| Verificação leve | Executar testes, build, formatação, revisão de diff e checagens de dependência. |
| Revisão técnica | Conferir regra de negócio, segurança multiunidade, concorrência, migrações e regressões. |

## Próximas issues deliberadamente adiadas

- Serviço criado a partir de orçamento aprovado.
- Agenda e execução de serviço.
- Contratos.
- Financeiro, estoque, impostos e Excel.

Essas issues serão abertas somente depois de planejamento de domínio específico.
