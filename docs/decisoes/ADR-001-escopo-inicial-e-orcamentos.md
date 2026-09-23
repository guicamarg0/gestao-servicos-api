# ADR-001 — Escopo inicial e orçamento como núcleo do MVP

**Data:** 2026-09-22
**Status:** Aceita

## Contexto

O sistema legado possui várias áreas, mas o novo produto será reconstruído sem migração de dados e focado inicialmente em microempresas prestadoras de serviço.

## Decisão

- O MVP priorizará orçamento; contratos serão planejados antes de serem implementados.
- Unidade e empresa serão o mesmo conceito no MVP.
- Financeiro fica fora do escopo inicial.
- O orçamento terá múltiplos itens, revisão, PDF preservado por emissão e aprovação manual interna.
- Rascunhos sem contratante serão permitidos e poderão servir como modelos reutilizáveis.

## Consequências

- A configuração da unidade, contratantes e catálogo devem preceder a implementação completa de orçamentos.
- Não serão criadas rotinas de importação ou adaptação ao banco legado.
- Contratos não devem ser implementados com base em suposições; seu desenho será discutido em etapa própria.
