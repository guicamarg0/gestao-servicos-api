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
- O MVP não terá cálculo ou tratamento tributário separado; os valores informados representam o valor final da proposta.

### PDF da revisão emitida

Todo PDF de orçamento deve conter identificação e logo da unidade, número e revisão, emissão, validade quando preenchida, dados do contratante, itens, subtotais, desconto, acréscimo, total, condições de pagamento e observações comerciais.

A seção visual de assinatura ou aceite do contratante será opcional por orçamento, controlada por um checkbox antes da emissão. A escolha fica congelada junto com a revisão e o respectivo PDF.

PDF é a única exportação do MVP. Não haverá exportação para Excel nesta etapa.

### Permissões do orçamento

| Perfil | Permissões |
| --- | --- |
| `ADMIN` | Acesso total ao módulo. |
| `GESTOR` | Criar, editar, emitir, cancelar, aprovar e recusar. |
| `OPERADOR` | Criar e editar rascunhos; não aprova nem cancela. |
| `CONSULTA` | Visualizar e baixar PDFs. |

### Histórico e auditoria

O orçamento manterá uma linha do tempo com criação, alterações de rascunho, criação de revisão, atualização de valores pelo catálogo, emissão de PDF, aprovação, recusa, cancelamento e alterações relevantes de contratante ou condições de pagamento.

Cada evento deve registrar usuário responsável e data/hora. Observações de usuário são opcionais em todos os eventos, inclusive recusa e cancelamento.

### Contratante, contratado e dados de pagamento

- Contratante é a pessoa ou empresa que receberá a proposta e é opcional, inclusive na emissão. Isso permite PDFs genéricos e modelos sem destinatário.
- Contratado é a unidade prestadora do serviço. Seus dados de identificação, contato e logo vêm da configuração da unidade.
- Cada revisão emitida guarda uma cópia imutável dos dados do contratado usados no PDF, para preservar o histórico mesmo se a unidade for alterada depois.
- Dados de pagamento padrão pertencem à configuração da unidade.
- Cada orçamento terá condições de pagamento em texto livre, iniciadas por um texto padrão configurado na unidade.
- O usuário pode ajustar as condições apenas para aquele orçamento; a revisão emitida guarda uma cópia imutável do texto e dos dados de pagamento usados.
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

## Contratantes

O contratante é cadastrado por unidade e pode ser pessoa física ou jurídica. O cadastro possui nome completo ou razão social, nome fantasia opcional, CPF ou CNPJ, endereço, observações internas e situação ativo/inativo.

Um CPF ou CNPJ informado deve ser único dentro da unidade. Contratantes que possuírem orçamento ou serviço no histórico não são excluídos; podem apenas ser inativados.

O contratante pode ter vários contatos simples. Cada contato possui nome e pelo menos um meio de contato: telefone ou e-mail. Cargos, departamentos e automações de comunicação ficam fora do MVP.

O cadastro rápido de contratante solicitará nome ou razão social e telefone; os demais dados poderão ser completados posteriormente. Os dados usados são copiados para a revisão emitida do orçamento, sem alterar PDFs históricos.

## Próximos módulos planejados

1. Organização técnica da API: MVC, DTOs, mappers, exceções, auditoria, paginação e testes multiunidade.
2. Configuração da unidade: identificação, logo e dados de pagamento padrão.
3. Contratantes e cadastro rápido.
4. Catálogo de serviços e materiais.
5. Orçamentos, revisões e PDF.
6. Contratos, após planejamento específico.

## Configuração da unidade

### Dados obrigatórios

- Nome ou razão social.
- Documento, CPF ou CNPJ.
- Endereço completo.
- Dados de pagamento.

Campos inicialmente opcionais: nome fantasia, e-mail, logo, responsável e texto padrão de condições de pagamento.

### Pagamento

Dados de pagamento são informações de recebimento estáveis da unidade, como chave PIX, favorecido, banco, agência e conta. A unidade pode manter mais de uma forma de recebimento e escolher quais delas exibir em cada orçamento. Os dados são configurados uma vez na unidade e podem ser exibidos nos PDFs.

Condições de pagamento são regras específicas de cada orçamento, como pagamento à vista, sinal e saldo ou parcelamento. O orçamento inicia com o texto padrão da unidade, permite edição para a proposta e congela o texto usado na revisão emitida.
