# Requisitos do MVP

## Visão do produto

O Gestão de Serviços atenderá microempresas prestadoras de serviço. O foco inicial é criar, emitir, revisar e administrar orçamentos; contratos serão planejados em etapa posterior.

Não haverá migração de dados do sistema legado. O novo banco iniciará vazio e toda funcionalidade operacional será isolada por unidade.

## Decisões de escopo

- Neste MVP, unidade e empresa representam o mesmo conceito para o usuário.
- Financeiro não faz parte do escopo inicial.
- Usuários podem participar de mais de uma unidade e possuem perfil por unidade.
- O acesso é controlado pela API, usando o contexto de unidade validado.
- Convites usam links de acesso de uso único, associados a um perfil e válidos por sete dias.

## Orçamentos

### Estrutura

- Um orçamento pertence a uma unidade e pode, em rascunho, não possuir contratante.
- O mesmo orçamento pode reunir serviços, materiais e itens manuais.
- Itens devem guardar uma cópia de descrição, unidade de medida, quantidade, valor unitário, desconto e total aplicado na revisão.
- Os totais são: subtotal de serviços, subtotal de materiais, desconto geral, acréscimo geral e total final.
- O orçamento sem contratante em rascunho também pode ser usado como modelo, sem um tipo de registro separado.
- Duplicar orçamento cria uma nova negociação, com novo número e revisão inicial.

### Relação com serviços executados

- O orçamento é uma proposta comercial e pode conter mão de obra, peças e materiais.
- Mão de obra, peça e material são tipos de item da proposta; não representam, isoladamente, o serviço executado para o cliente.
- O **serviço** representa o trabalho efetivamente contratado/executado para um contratante e será um módulo posterior, com agenda, responsáveis, execução e anexos.
- Um orçamento aprovado poderá originar um serviço baseado na revisão aprovada. Orçamentos não aprovados não criam serviços.

### Numeração e revisão

- O número é sugerido automaticamente, de modo sequencial por unidade, no formato inicial `ORC-AAAA-000000`.
- O usuário pode alterar esse número antes da primeira emissão; o valor deve ser único na unidade.
- Após a emissão inicial, o número não pode ser alterado.
- Revisões mantêm o número principal e recebem sequência própria, por exemplo `ORC-2026-000123 / Rev. 2`.
- Cada emissão gera um PDF permanente e imutável da revisão correspondente.

### Status e edição

```text
RASCUNHO -> EMITIDO -> APROVADO
                      -> RECUSADO
RASCUNHO / EMITIDO -> CANCELADO
EMITIDO -> RASCUNHO (nova revisão para edição)
```

- Apenas rascunhos podem ser editados livremente.
- Alterar um orçamento emitido cria uma nova revisão em rascunho; a revisão emitida anterior continua preservada.
- A aprovação e a recusa são registradas manualmente por usuário interno, com data, responsável e observação opcional.
- Revisões aprovadas, recusadas ou canceladas ficam congeladas.

### Catálogo e atualização de preços

- Itens originados de serviço ou material guardam vínculo com o catálogo e uma cópia dos dados usados.
- Em `RASCUNHO` ou `EMITIDO`, a ação **Atualizar valores pelo catálogo** compara os itens vinculados com preços atuais e apresenta as mudanças antes da confirmação.
- Itens manuais não são alterados por essa ação.
- Após aprovação, recusa ou cancelamento, os valores ficam congelados definitivamente.

### Totais, descontos e acréscimos

- O orçamento calcula subtotal de itens, desconto geral, acréscimo geral e total final.
- Neste MVP, desconto é apenas geral; não haverá desconto individual por item.
- Desconto geral e acréscimo geral podem ser informados em valor ou percentual sobre o subtotal.
- O total final não pode ser negativo.
- A memória de cálculo deve aparecer no formulário e no PDF da revisão emitida.

### Contratante e dados de pagamento

- Contratante será obrigatório para emitir um PDF comercial, mas não para salvar rascunho.
- Dados de pagamento padrão pertencem à configuração da unidade.
- Cada orçamento pode ajustar suas condições de pagamento e guarda uma cópia delas na revisão emitida.
- A validade é opcional. Quando preenchida, aparece no PDF e pode gerar aviso de vencimento, sem bloquear aprovação manual.

## Cadastro rápido

O sistema terá um padrão compartilhado de cadastro rápido para relacionamentos, começando por contratantes e depois serviços e materiais.

- A pesquisa de relacionamento deve oferecer a ação “Cadastrar novo”.
- O formulário abre em modal ou painel lateral e preserva o estado da tela de origem.
- O formulário rápido contém somente campos mínimos.
- Depois de salvar, o registro novo é selecionado automaticamente.
- As regras de validação permanecem centralizadas na API.

## Catálogo único de itens

O sistema manterá um único cadastro de itens para compor orçamentos, com os tipos abaixo:

- `MAO_DE_OBRA`: diagnóstico, instalação, manutenção, visita técnica e diária.
- `PECA`: componentes específicos usados na execução.
- `MATERIAL`: insumos e materiais genéricos.

Todo item pertence à unidade e possui nome, descrição opcional, unidade de medida, valor padrão, código ou referência opcional e situação ativo/inativo. Itens inativos não podem ser adicionados a novos orçamentos, mas permanecem em documentos históricos.

Unidades de medida iniciais: unidade, hora, diária, metro, metro quadrado, quilograma, litro e outra unidade personalizada. O orçamento pode ajustar os valores copiados do catálogo e também aceitar itens manuais sem cadastro prévio.

## Próximos módulos planejados

1. Organização técnica da API: MVC, DTOs, mappers, exceções, auditoria, paginação e testes multiunidade.
2. Configuração da unidade: identificação, logo e dados de pagamento padrão.
3. Contratantes e cadastro rápido.
4. Catálogo de serviços e materiais.
5. Orçamentos, revisões e PDF.
6. Contratos, após planejamento específico.
