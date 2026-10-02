# Layout do orçamento

Referência visual: Orçamento 51 - Metafilme fornecido pelo usuário em 02/10/2026.

O PDF usa A4, logo pequena no canto superior esquerdo, título ORÇAMENTO, código operacional, data de emissão, emitente e cliente em duas colunas, tabela com item/descrição/quantidade/valor unitário/total e total do pedido destacado. Condições comerciais vêm da validade, pagamento e observações informadas. Assinaturas das duas partes são fixas e aparecem apenas no rodapé da última página; o antigo exibirAssinatura continua no contrato de entrada por compatibilidade, mas não oculta os campos do novo padrão.

Renderização com OpenHTMLtoPDF e fontes Noto incorporadas, preservando acentos. A tabela repete o cabeçalho em novas páginas. Ajustes financeiros são apresentados quando houver desconto/acréscimo. Observações preservam parágrafos.

OrcamentoRequestDTO e RevisaoOrcamentoResponseDTO acrescentam empresaId opcional. A empresa pertence à unidade e deve estar ativa ao selecionar. O vínculo é preservado na duplicação e nas novas revisões. Sem empresaId, mantém-se o emitente das configurações da unidade para orçamentos antigos e integrações existentes.

EmpresaRequestDTO/EmpresaResponseDTO acrescentam logoUrl: imagem incorporada em data URL PNG/JPEG (até 700.000 caracteres). O frontend recebe arquivo de até 5 MB e reduz a imagem a 800 pixels. A API valida o formato e as dimensões (até 4000 pixels), sem buscar URLs externas. Remoção envia null. A migração V14 adiciona logo e vínculo da empresa, ampliando o snapshot do emitente para text.

Dados das partes e logo são congelados no snapshot da emissão. PDFs já emitidos permanecem iguais; novos PDFs usam o novo layout. A logo deste fluxo é enviada no cadastro de empresas; URLs remotas antigas das configurações não são baixadas pelo renderizador.

Verificação: geração com logo, caracteres acentuados, valores em pt-BR, descontos/acréscimos e código; orçamento de 80 itens em cinco páginas com cabeçalho repetido e assinaturas apenas na última; integração de persistência da empresa, snapshot da logo, cópia entre revisões, rejeição de imagem inválida e isolamento entre unidades.
