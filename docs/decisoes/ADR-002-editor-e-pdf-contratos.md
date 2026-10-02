# Editor e PDF de contratos — 02/10/2026

O foco do produto é gerar e salvar orçamentos e contratos. A interface de contratos deixa de enviar ou registrar assinaturas eletrônicas. Os endpoints legados continuam disponíveis para compatibilidade; documentos previamente assinados permanecem protegidos contra edição.

## Implementação adotada

Tiptap substitui contentEditable + document.execCommand. Permite fonte, tamanho em pontos de 6 a 96, cor livre por seletor ou hexadecimal, parágrafos/títulos, alinhamento, listas, tabelas e desfazer/refazer. Variáveis podem ser inseridas no cursor ou arrastadas. HTML existente é convertido para o esquema do editor, incluindo atributos font/color/face/size legados.

O PDF continua usando OpenHTMLtoPDF, com fontes Noto Sans, Noto Serif e Noto Sans Mono incluídas na aplicação e no frontend (licença OFL incluída). Isso evita depender de fontes instaladas no servidor. Fontes legadas Arial/Helvetica, Times New Roman e Courier New usam respectivamente essas três famílias; não são cópias exatas das fontes proprietárias. Noto Sans Mono não inclui variantes itálicas.

As assinaturas são desenhadas com PDFBox somente no rodapé da última página: razão social do cliente (contratante) e razão social da empresa selecionada (contratada). Há área reservada de 48 mm no rodapé de todas as páginas para impedir sobreposição. A prévia HTML exibe o bloco ao final, mas não simula paginação exata. Templates antigos com linhas de assinatura escritas manualmente precisam ter essas linhas removidas para evitar duplicação; não há remoção heurística de conteúdo do usuário.

## Alternativas avaliadas

| Alternativa | Quando usar | Custo / limitação |
| --- | --- | --- |
| Tiptap + OpenHTMLtoPDF (adotada) | Editor integrado e contratos com texto/tabelas | CSS de impressão limitado; exige fontes incorporadas |
| Tiptap + Chromium/Playwright | Layouts HTML mais complexos e maior semelhança com o navegador | Instalar/manter Chromium no servidor, controlar acesso a rede/arquivos e consumo de memória |
| Templates DOCX + docx4j | Modelos mantidos no Word por usuários | Conversão DOCX→PDF e variáveis precisam de fluxo próprio; fidelidade depende do conversor |

Não foi adotado Chromium nesta entrega: as falhas de cor/fontes do fluxo atual foram cobertas por testes que verificam fontes e pixels efetivamente renderizados no PDF. A migração para Chromium faz sentido quando houver necessidade de CSS moderno e layout de páginas mais complexo.

A importação DOCX existente extrai texto; não preserva layout completo do Word. Substituir o editor não muda essa limitação. Um fluxo centrado em DOCX seria uma evolução separada.

Fontes: [Tiptap FontFamily](https://tiptap.dev/docs/editor/extensions/functionality/fontfamily), [Playwright Page.pdf](https://playwright.dev/java/docs/api/class-page#page-pdf), [docx4j](https://www.docx4java.org/docx4j/Docx4j_GettingStarted.pdf).

## Contrato da API

- PUT /contratos/{id} valida e persiste empresaId antes de resolver o conteúdo.
- POST /contratos/previa retorna conteudo e assinaturasHtml separadamente; o bloco fixo nunca entra no editor.
- RevisaoOrcamentoResponseDTO inclui cliente (razão social) para listagens sem depender de uma segunda consulta ao cadastro.
- A busca de contratos inclui empresa, modelo e número de orçamento, além de cliente e número do contrato.
- Estados antigos são preservados; RASCUNHO é apresentado como Salvo na interface de contratos.

## Validação

Testes de integração verificam a empresa na criação e atualização, preservação de versões e rejeição de empresa de outra unidade. Testes de PDF verificam pixels vermelhos/azuis, famílias incorporadas e assinatura somente na última página. O frontend verifica a conversão das informações da lista e a data do evento mais recente.
