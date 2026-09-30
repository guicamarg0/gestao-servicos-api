# Gestão de Serviços — API

Fundação da API multiusuário e multiunidade em Java 21, Spring Boot 4.1, PostgreSQL e Flyway.

## Escopo atual

- cadastro e login com senha protegida e JWT stateless;
- usuário, unidade e associação com perfis `ADMIN`, `GESTOR`, `OPERADOR` e `CONSULTA`;
- criação transacional de unidade com associação `ADMIN` para o criador;
- nome globalmente único de unidade, ignorando caixa, acentos e espaços excedentes;
- consulta de `/me` e seleção validada de unidade;
- contexto por `X-Unidade-Id`, sempre conferido pela API contra o usuário autenticado;
- autorização contextual por perfil e teste de bloqueio de acesso cruzado.
- clientes, catálogo de peças e mão de obra, configuração da unidade e formas de recebimento;
- orçamentos com revisões, decisões e PDF; contratos gerados de orçamento aprovado, assinatura registrada e PDF;
- serviços e agenda com detecção de conflito de horário;
- despesas com aprovação e relatórios por período em JSON, CSV e PDF.

## Execução local

Requisitos: Java 21, Maven 3.9+ e Docker.

```powershell
docker compose up -d
mvn spring-boot:run
```

Os valores locais funcionam sem arquivo adicional. Para personalizá-los, exporte as variáveis de `.env.example` antes de iniciar. O segredo JWT padrão existe somente para desenvolvimento; o perfil `prod` exige `JWT_SECRET`, `DB_URL`, `DB_USER` e `DB_PASSWORD`.

Use `CORS_ALLOWED_ORIGINS` para configurar as origens permitidas do front-end.

## Verificações

```powershell
mvn test
mvn verify
```

Os testes sempre exercitam concorrência e o contrato HTTP no banco em memória. Com um engine Docker disponível, o Testcontainers também inicia PostgreSQL 17 e valida a migração e a restrição única no banco-alvo; sem Docker, esse teste específico é marcado como ignorado.

Swagger UI: `http://localhost:8080/swagger-ui.html`. Contrato OpenAPI JSON: `http://localhost:8080/v3/api-docs`.

## Rotas principais

| Método | Rota | Regra |
| --- | --- | --- |
| `POST` | `/api/v1/autenticacao/cadastro` | público; cria usuário sem unidade |
| `POST` | `/api/v1/autenticacao/login` | público; devolve Bearer JWT |
| `GET` | `/api/v1/me` | autenticado; lista associações e perfis |
| `POST` | `/api/v1/me/unidades/{unidadeId}/selecionar` | valida que a associação pertence ao usuário |
| `POST` | `/api/v1/unidades` | autenticado; cria unidade + associação `ADMIN` atomicamente; nome duplicado retorna `409` |
| `GET` | `/api/v1/unidades/atual/protegida` | exige `X-Unidade-Id` válido e perfil `ADMIN`, `GESTOR` ou `OPERADOR` |
| `GET/POST` | `/api/v1/contratantes`, `/api/v1/catalogo-itens`, `/api/v1/orcamentos` | dados da unidade atual |
| `GET/PUT` | `/api/v1/unidades/atual/configuracao` | administrador; dados e formas de recebimento |
| `GET` | `/api/v1/unidades/atual/formas-recebimento` | formas ativas para orçamentos |
| `GET/POST` | `/api/v1/servicos`, `/api/v1/despesas`, `/api/v1/contratos` | operações da unidade atual |
| `GET` | `/api/v1/agenda`, `/api/v1/relatorios/resumo` | agenda e indicadores por período |

O JWT identifica somente o usuário. A unidade é selecionada pelo cabeçalho `X-Unidade-Id`; o filtro de segurança consulta a associação a cada requisição e não confia no ID enviado pelo cliente. As entidades operacionais possuem `unidade_id` e as consultas são filtradas pelo contexto validado.

Quando o nome normalizado da unidade já existe, a API responde `409 Conflict` em `application/problem+json`, com `code` igual a `UNIDADE_NOME_JA_EXISTENTE`. A restrição `uk_unidade_nome_normalizado` no PostgreSQL é a garantia final contra criações concorrentes.

## Padrão de projeto

O monólito é modular por domínio (`autenticacao`, `usuario`, `unidade`, `associacao`). Entidades JPA ficam em `model`, entradas e saídas usam records `RequestDTO`/`ResponseDTO`, controllers tratam apenas HTTP, services concentram regras e transações e repositories isolam persistência. O Java e o JSON usam nomes de domínio em português e `camelCase`; somente tabelas e colunas usam `snake_case`. Configuração transversal fica em `config` e segurança em `security`.
