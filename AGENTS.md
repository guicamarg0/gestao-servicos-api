# Instruções de trabalho dos agentes

## Contexto

Este repositório contém a API do novo sistema Gestão de Serviços. O sistema será multiusuário e multiunidade. Um administrador cria uma unidade e concede acesso a outros usuários por perfil.

## Processo obrigatório

1. Ler este arquivo antes de executar qualquer ação.
2. Trabalhar somente no escopo da tarefa recebida.
3. Criar uma branch própria, com nome descritivo.
4. Fazer commits pequenos e claros.
5. Executar as verificações disponíveis antes de concluir.
6. Registrar decisões, riscos e pendências no resultado da tarefa.
7. Nunca expor segredos, senhas ou tokens no código, commits ou logs.

## Papéis

- Planejamento e arquitetura: `gpt-5.6-sol`, esforço médio.
- Implementação: `gpt-5.6-sol`, esforço médio.
- Comandos, formatação e revisão mecânica: `gpt-5.6-luna`, esforço leve.
- Revisão técnica e aprovação: `gpt-5.6-luna`, esforço leve.

## Regras técnicas

- Toda entidade operacional deverá possuir vínculo com uma unidade.
- A API deve validar a unidade com base no usuário autenticado.
- Não confiar somente em IDs enviados pelo front-end.
- Preferir PostgreSQL, migrações versionadas e testes de regras de negócio.
- Não implementar funcionalidades fora do planejamento aprovado.
- Não apagar ou sobrescrever trabalho existente sem confirmar o escopo.

## Padrões de código e projeto

- Usar nomes de domínio em português para classes, métodos e variáveis: `Usuario`, `Unidade`, `criarUnidade`, `senhaAtual`.
- Usar `camelCase` para atributos, métodos, variáveis, JSON e propriedades de configuração.
- Usar `PascalCase` para classes, interfaces, records e enums.
- Usar `UPPER_SNAKE_CASE` para constantes e valores de enum.
- Usar nomes de tabela e coluna em `snake_case`; o banco não precisa seguir o `camelCase` da API.
- Classes de persistência devem ficar em `model` e representar entidades JPA.
- Objetos de entrada devem terminar com `RequestDTO`; objetos de saída, com `ResponseDTO`.
- Não expor entidades JPA diretamente nos controllers; sempre converter para DTO.
- Enums devem representar estados e perfis fechados, evitando strings espalhadas pelo código.
- Controllers devem cuidar apenas de HTTP, validação de entrada e delegação ao service.
- Services devem concentrar regras de negócio e transações.
- Repositories devem cuidar somente do acesso a dados.
- Classes de configuração devem ficar em `config`; segurança em `security`.
- Preferir construtor injetado, imutabilidade, records para DTOs e métodos pequenos.
- Endpoints e mensagens visíveis à API devem estar em português quando fizerem parte do domínio.
- Usar `Id` no Java e `id` no JSON; manter JSON em `camelCase`.

## Integração

O front-end está no repositório `gestao-servicos-web`. Alterações que dependam do front-end devem documentar o contrato esperado da API.
