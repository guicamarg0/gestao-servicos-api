# Operação, cadastros e recuperação de senha

Implementação de 03/10/2026, branch `feat/operacao-cadastros-recuperacao`, coordenada com a mesma branch do frontend.

## Etapas implementadas

1. Cadastros: usuários podem ser criados, consultados, editados, inativados e reativados. Gestores gerenciam equipe, mas não administradores. Último administrador protegido. Clientes e catálogo permitem reativação; empresas, pagamento e modelos preservam os CRUDs existentes.
2. Filtros e datas: barra compartilhada com limpeza, componente de data sem horário, filtros remotos antes da paginação (incluindo status de cliente, serviço/categoria/período de despesas).
3. Agenda: calendário mensal, lista, serviços por dia, múltiplos serviços no mesmo dia, programação/reprogramação e download de convite ICS de dia inteiro. O convite é um arquivo para importar no calendário; não envia email nem sincroniza automaticamente.
4. Despesas: vários itens com descrição, quantidade e valor unitário. Total calculado no servidor, aprovação/rejeição e comprovantes mantidos. Vínculo com serviço e consulta de despesas dentro do serviço. Tela de relatórios removida da navegação; URLs antigas redirecionam.
5. Dashboard: dados reais da unidade, valores das revisões atuais dos orçamentos, aprovados, despesas aprovadas, serviços, agenda, contratos e gráficos. Valor aprovado não equivale a dinheiro recebido. Período considera data de criação do orçamento e data da despesa; serviços em andamento e contratos ativos refletem situação atual.
6. Recuperação de senha e armazenamento: fluxos do próprio usuário e do gestor; integração Resend e Supabase Storage no servidor, sem substituição da autenticação Java.

## Recuperação de senha

- POST `/api/v1/autenticacao/recuperar-senha`: `{ "email": "..." }`; resposta genérica, sem revelar existência da conta.
- POST `/api/v1/autenticacao/redefinir-senha`: `{ "token": "...", "senha": "..." }`; 204 quando válido.
- POST `/api/v1/unidades/atual/usuarios/{associacaoId}/recuperar-senha`: administrador ou gestor da unidade.
- Token aleatório de 32 bytes, somente hash SHA-256 persistido, validade 30 minutos, uso único; novo pedido invalida os anteriores.
- Limite por conta: um envio por minuto e cinco por hora, compartilhado entre os dois caminhos.
- Senhas continuam usando BCrypt (já existente). Redefinir revoga sessões anteriores via versão do usuário no JWT. Nenhuma senha é enviada por email.
- Nome/email de contas vinculadas a várias unidades não podem ser alterados por um gestor de apenas uma delas; perfil permanece editável por unidade.

## Agenda, despesas e arquivos

- PUT `/api/v1/servicos/{id}/agendamento` agora recebe `{ "dataProgramada": "2026-10-03", "responsavel": "..." }`, sem início/fim de horário.
- Despesa aceita `itens: [{descricao, quantidade, valorUnitario}]`. O servidor ignora o total fornecido para calcular pelos itens. Corpo antigo com apenas `valor` permanece compatível e vira um item.
- GET `/api/v1/despesas` aceita `servicoId`, além dos filtros existentes.
- GET `/api/v1/painel?de=2026-10-01&ate=2026-10-03`: período inclusivo, até 366 dias.
- POST `/api/v1/arquivos` multipart campo `arquivo`: PDF/PNG/JPG válido, máximo 5 MB. GET `/api/v1/arquivos/{id}` exige autenticação e unidade proprietária. O comprovante guarda a referência `/arquivos/{id}`.
- Novos comprovantes e PDFs emitidos de orçamento são gravados diretamente no PostgreSQL em bytes, sem depender de Storage. Logos continuam em Base64. Downloads de arquivos antigos com referência ao Storage continuam compatíveis; não há migração automática desses arquivos. Rascunhos de orçamento são transitórios e snapshots existentes são preservados. Migração V21 adiciona o conteúdo binário dos comprovantes.

## Configuração externa pendente

Não foram criadas contas, enviados emails reais ou publicados dados no Supabase. As integrações foram verificadas com substitutos locais.

Variáveis exclusivas do backend:

| Variável | Finalidade |
| --- | --- |
| `DB_URL` | JDBC PostgreSQL, com `sslmode=require` no Supabase |
| `DB_USER`, `DB_PASSWORD` | Credenciais do banco |
| `JWT_SECRET` | Segredo forte do JWT, exclusivo do servidor |
| `CORS_ALLOWED_ORIGINS` | URL exata do frontend publicado |
| `FRONTEND_URL` | URL pública usada no link de recuperação |
| `RESEND_API_KEY` | Chave do Resend |
| `EMAIL_REMETENTE` | Remetente em domínio verificado |
| `SUPABASE_URL` | URL HTTPS do projeto |
| `SUPABASE_SERVICE_ROLE_KEY` | Chave service role exclusiva do servidor |
| `SUPABASE_STORAGE_BUCKET` | Nome do bucket privado, padrão `documentos` |

Para usar Supabase somente como banco, desabilitar a exposição das tabelas do sistema pela Data API ou restringir privilégios/RLS de `anon` e `authenticated`, incluindo tabelas futuras. A API Java conecta pelo JDBC e valida unidade/perfil. Storage e sua chave de serviço são necessários apenas para ler arquivos antigos que já tenham sido armazenados lá; uploads novos funcionam diretamente pelo banco. A chave de serviço não deve entrar em variáveis `VITE_*`. Backend aceita multipart de arquivo até 5 MB e requisição até 6 MB, ajustável pela configuração Spring `spring.servlet.multipart.max-file-size` e `max-request-size`.

Para Java com conexões persistentes, usar conexão direta quando a hospedagem suportar o endereço, ou Session pooler para IPv4; obtenha a string no painel. [Guia oficial Spring Boot/Supabase](https://supabase.com/docs/guides/getting-started/quickstarts/spring-boot).

Destinatários podem usar qualquer provedor de email; para enviar em produção pelo Resend, verificar domínio do remetente. [Documentação de domínios Resend](https://resend.com/docs/dashboard/domains/introduction). A aplicação responde com indisponibilidade controlada quando a integração necessária não está configurada.

Frontend pode ser publicado na Vercel com `VITE_API_URL` apontando para a API. A API Spring Boot precisa de hospedagem Java separada; Supabase não hospeda esse servidor. Aplicar Flyway V17–V21 e reiniciar o backend com o código atualizado. Configurações locais preexistentes não foram sobrescritas.

Pendências operacionais após provisionamento: validar entrega real de email, privilégios e backup do banco, e rotina de limpeza de uploads abandonados (sem apagar arquivos referenciados).
