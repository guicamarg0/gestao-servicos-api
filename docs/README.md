# Documentação do produto

Esta pasta é a fonte oficial das decisões de produto, requisitos e arquitetura do novo Gestão de Serviços.

## Organização

- `requisitos-mvp.md`: escopo e regras confirmadas do MVP.
- `decisoes/`: decisões de arquitetura e produto que afetam mais de uma funcionalidade.

## Como registrar uma decisão

1. Crie o próximo arquivo numerado em `decisoes`, com contexto, decisão, consequências e pendências.
2. Atualize `requisitos-mvp.md` quando a decisão mudar ou detalhar um requisito.
3. Não substitua decisões antigas: registre uma nova decisão que a substitua e crie um link entre ambas.

O front-end e a API devem consultar esta documentação antes de alterar contratos ou fluxos de negócio.
