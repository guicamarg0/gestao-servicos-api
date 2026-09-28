# Gestão de Serviços — Escopo funcional e técnico

## 1. Visão do produto

Sistema web para empresas que prestam serviços e precisam controlar clientes, serviços, agenda, peças, despesas, orçamentos, contratos e relatórios.

O sistema será multiusuário e multiunidade. Um administrador cria uma unidade e concede acesso a outros usuários por perfil. Cada dado operacional pertence a uma única unidade.

## 2. Atores e permissões

### Administrador (`ADMIN`)

- Cria e configura a unidade.
- Gerencia usuários, convites e perfis.
- Gerencia todos os cadastros e documentos.
- Acessa todos os relatórios da própria unidade.

### Gestor (`GESTOR`)

- Acessa clientes, serviços, agenda, orçamentos, contratos e relatórios.
- Pode aprovar ou rejeitar orçamentos conforme a regra da unidade.
- Não gerencia a conta principal nem remove o último administrador.

### Operador (`OPERADOR`)

- Cadastra e atualiza clientes.
- Cria e acompanha serviços.
- Gerencia agenda e despesas operacionais.
- Pode criar rascunhos de orçamento.

### Consulta (`CONSULTA`)

- Visualiza dados autorizados.
- Não cria, altera ou remove registros.

## 3. Regras globais de negócio

1. Todo usuário deve autenticar-se.
2. Todo acesso operacional deve estar associado a uma unidade ativa.
3. O perfil pertence à associação `usuario_unidade`, não ao usuário global.
4. Um usuário pode participar de várias unidades com perfis diferentes.
5. O criador da unidade recebe automaticamente o perfil `ADMIN` na mesma transação.
6. Deve existir no máximo uma associação ativa por par usuário/unidade.
7. Toda entidade operacional possui `unidade_id` obrigatório.
8. A API valida a unidade e o perfil; o front-end nunca é autoridade de segurança.
9. Exclusões operacionais são preferencialmente inativações, preservando histórico.
10. Documentos aprovados não podem ser alterados sem gerar nova versão ou registrar revisão.
11. Valores monetários usam precisão decimal, nunca `float` ou `double`.
12. Datas e horas devem considerar o fuso horário configurado na unidade.
13. Ações relevantes devem registrar usuário, data, unidade e alteração realizada.
14. Arquivos não devem ser armazenados permanentemente no disco local da API.
15. Listagens devem ser paginadas e filtradas no back-end.

## 4. Módulos funcionais

### 4.1 Autenticação e acesso

- Cadastro inicial do administrador.
- Login e logout.
- Recuperação e alteração de senha.
- Sessão com access token curto e refresh token seguro.
- Convite de usuário por e-mail ou link.
- Aceite, expiração, reenvio e cancelamento de convite.
- Ativação e inativação de usuário.
- Seleção da unidade atual.
- Controle de permissões por perfil.
- Registro de tentativas e eventos de segurança.

### 4.2 Unidade e configurações

- Dados da unidade: nome, documento, endereço, contatos e logotipo.
- Fuso horário, moeda e configurações de numeração.
- Configuração de informações exibidas em orçamento e contrato.
- Usuários vinculados, perfil e status.
- Dados bancários ou formas de pagamento.
- Preferências de prazo, validade, impostos e textos padrão.

### 4.3 Clientes

- Cadastro de pessoa física ou jurídica.
- Nome, documento, contatos e endereço.
- Contatos adicionais.
- Observações internas.
- Status ativo/inativo.
- Busca por nome, documento, telefone e e-mail.
- Histórico de serviços, orçamentos, contratos e despesas.
- Validação de duplicidade por documento dentro da unidade.

### 4.4 Catálogo de serviços

- Nome e descrição.
- Categoria.
- Unidade de cobrança: hora, diária, visita, projeto ou outro.
- Valor padrão de mão de obra.
- Duração estimada.
- Status ativo/inativo.
- Serviços podem ser usados como modelo em novos atendimentos.

### 4.5 Peças e materiais

- Nome, código, descrição e unidade de medida.
- Custo e preço de venda.
- Estoque opcional.
- Estoque mínimo.
- Fornecedor opcional.
- Ativação e inativação sem apagar histórico.
- Histórico de utilização em orçamentos e serviços.

### 4.6 Serviços e ordens de serviço

- Cliente vinculado.
- Título, descrição e categoria.
- Status: rascunho, agendado, em andamento, aguardando, concluído, cancelado.
- Responsável ou equipe.
- Local de execução.
- Data e hora previstas.
- Data de início e conclusão.
- Anexos, observações e histórico de alterações.
- Relação com orçamento, contrato, despesas e agenda.

### 4.7 Agenda

- Visualização mensal, semanal e diária.
- Filtros por responsável, cliente, status e tipo de serviço.
- Criação de evento a partir de serviço.
- Alteração de data, horário e responsável.
- Detecção de conflito de agenda.
- Eventos de dia inteiro.
- Acesso rápido ao cliente e ao serviço.
- Histórico de reagendamentos.

### 4.8 Orçamentos

- Criação a partir de cliente ou serviço.
- Itens de serviço, mão de obra, peças e despesas.
- Quantidade, unidade, valor unitário, desconto e subtotal.
- Custos internos separados do preço apresentado ao cliente.
- Despesas previstas e margem.
- Validade, prazo de execução e condições de pagamento.
- Observações e textos padrão.
- Status: rascunho, enviado, aprovado, rejeitado, expirado, cancelado.
- Cálculos realizados no back-end.
- Histórico de versões.
- Geração de PDF real.
- Compartilhamento por link seguro ou download.
- Registro de aprovação, rejeição e data.

### 4.9 Contratos

- Criação a partir de orçamento aprovado.
- Modelos de contrato reutilizáveis.
- Campos variáveis da unidade, cliente, serviço e valores.
- Cláusulas editáveis e cláusulas obrigatórias.
- Vigência, renovação, multa e condições de pagamento.
- Status: rascunho, aguardando assinatura, ativo, encerrado, cancelado.
- Controle de versões.
- PDF final sem edição acidental.
- Anexos e evidência de assinatura.
- Histórico de alterações e eventos.

### 4.10 Despesas

- Despesa prevista ou realizada.
- Categoria, descrição, valor e data.
- Vínculo com serviço e orçamento.
- Comprovante anexado em storage externo.
- Status pendente, aprovado, rejeitado ou cancelado.
- Relacionamento com custo real do serviço.
- Controle de quem lançou e quem aprovou.

### 4.11 Relatórios

Todos os relatórios devem seguir o fluxo:

```text
Filtros → indicadores → tabela → detalhe → exportação
```

Relatórios iniciais:

- Serviços por período, cliente, responsável e status.
- Agenda e ocupação.
- Orçamentos por status, valor e taxa de aprovação.
- Receita prevista, aprovada e realizada.
- Custos e margem por serviço.
- Despesas por categoria, período e unidade.
- Peças mais utilizadas e custo de materiais.
- Contratos ativos, vencendo e encerrados.
- Clientes ativos e histórico de relacionamento.
- Auditoria de alterações.

Exportações:

- PDF formatado.
- CSV ou Excel para análise.
- Impressão com layout próprio.

Relatórios devem consultar dados agregados no back-end, com filtros, paginação e autorização por unidade.

### 4.12 Notificações e comunicação

- Convite de usuário.
- Orçamento enviado, aprovado ou rejeitado.
- Contrato próximo do vencimento.
- Serviço próximo do agendamento.
- Falha ou expiração de convite.
- Preferência de canais por unidade.

Este módulo pode começar com notificações internas e evoluir para e-mail ou WhatsApp.

### 4.13 Auditoria e suporte

- Login, logout e falhas de autenticação.
- Convites e alterações de permissões.
- Criação, edição, aprovação e cancelamento de documentos.
- Alteração de dados sensíveis.
- Usuário, unidade, IP quando disponível, data, ação e resumo da alteração.
- Consulta restrita ao ADMIN e, posteriormente, ao suporte autorizado.

## 5. Fluxos principais

### Onboarding

```text
Administrador cria conta
→ confirma e-mail, quando aplicável
→ cria unidade
→ recebe perfil ADMIN
→ configura dados da unidade
→ convida usuários
```

### Login e contexto

```text
Usuário informa credenciais
→ API autentica
→ Angular carrega /me
→ usuário seleciona unidade
→ Angular envia o contexto nas requisições
→ API valida associação e perfil
```

### Cliente e serviço

```text
Cadastrar cliente
→ criar serviço
→ informar execução e responsável
→ agendar
→ acompanhar status
→ concluir serviço
```

### Orçamento

```text
Selecionar cliente/serviço
→ adicionar mão de obra, peças e despesas
→ revisar totais
→ salvar rascunho
→ gerar PDF
→ enviar
→ aprovar ou rejeitar
```

### Contrato

```text
Orçamento aprovado
→ escolher modelo
→ preencher variáveis
→ revisar documento
→ gerar versão final
→ enviar para assinatura
→ ativar após aceite
```

### Encerramento do serviço

```text
Serviço concluído
→ lançar despesas reais
→ anexar comprovantes
→ comparar previsto x realizado
→ calcular margem
→ disponibilizar relatório
```

## 6. Componentes do Angular

```text
core/
  autenticacao/
  sessao/
  unidade-atual/
  guards/
  interceptors/
  tratamento-erros/
  configuracao/

layout/
  app-shell/
  sidebar/
  topbar/
  seletor-unidade/
  breadcrumbs/

shared/
  componentes/
    page-header/
    data-table/
    filter-bar/
    status-badge/
    money-field/
    date-field/
    confirm-dialog/
    empty-state/
    loading-state/
  pipes/
  validadores/
  tipos/

features/
  login/
  onboarding/
  usuarios/
  unidades/
  clientes/
  servicos/
  agenda/
  pecas/
  orcamentos/
  contratos/
  despesas/
  relatorios/
  configuracoes/
```

Páginas devem terminar com `Page`, serviços com `Service`, guards com `Guard` e interceptors com `Interceptor`. Componentes de apresentação não devem fazer chamadas HTTP diretamente.

## 7. Componentes da API

```text
br.com.gestaoservicos/
  autenticacao/
    controller/
    service/
    dto/
    security/
  usuarios/
  unidades/
  clientes/
  servicos/
  agenda/
  pecas/
  orcamentos/
  contratos/
  despesas/
  relatorios/
  compartilhado/
    config/
    exception/
    auditoria/
    persistence/
```

Padrões obrigatórios:

- `Model`: entidade JPA e persistência.
- `RequestDTO`: entrada validada.
- `ResponseDTO`: saída pública.
- `Enum`: estados e valores fechados.
- `Controller`: HTTP, validação e delegação.
- `Service`: regras de negócio e transações.
- `Repository`: consulta e persistência.
- `Mapper`: conversão entre model e DTO.
- `Config`: infraestrutura e integrações.
- `Security`: autenticação, autorização e contexto de unidade.

## 8. Etapas de entrega

1. Fundação técnica, padrões, banco, autenticação e multiunidade.
2. Usuários, convites, perfis e configurações da unidade.
3. Clientes.
4. Serviços e catálogo.
5. Agenda.
6. Peças e despesas.
7. Orçamentos.
8. Contratos.
9. Relatórios e exportações.
10. Auditoria, notificações, segurança e produção.

Cada etapa deve ter uma branch própria, commits pequenos, testes, revisão técnica e aprovação antes do merge.

## 9. Critérios para considerar uma etapa concluída

- Escopo da etapa documentado.
- Contrato API–Web atualizado.
- Branch criada com nome da etapa.
- Código seguindo os padrões de nomenclatura.
- Testes unitários e de integração aplicáveis executados.
- Teste negativo de isolamento entre unidades quando houver dados operacionais.
- README e variáveis de ambiente atualizados.
- Revisão técnica realizada.
- Pendências e riscos registrados.
- Commit(s) identificáveis e PR aprovado.
