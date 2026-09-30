package br.com.gestaoservicos.relatorio.service;

import br.com.gestaoservicos.relatorio.dto.RelatorioResumoResponseDTO;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.stereotype.Service;
import java.io.*;
import java.text.Normalizer;

@Service
public class GeradorPdfRelatorio {
    public byte[] gerar(RelatorioResumoResponseDTO r) {
        try (var documento = new PDDocument(); var saida = new ByteArrayOutputStream()) {
            documento.addPage(new PDPage());
            try (var conteudo = new PDPageContentStream(documento, documento.getPage(0))) {
                var fonte = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                float y = 770;
                y = linha(conteudo, fonte, "RELATORIO DE GESTAO", y, 16);
                y = linha(conteudo, fonte, "Periodo: " + r.de() + " a " + r.ate(), y, 11);
                y -= 20;
                y = linha(conteudo, fonte, "Servicos concluidos: " + r.servicosConcluidos(), y, 11);
                y = linha(conteudo, fonte, "Servicos em andamento: " + r.servicosEmAndamento(), y, 11);
                y = linha(conteudo, fonte, "Servicos agendados: " + r.servicosAgendados(), y, 11);
                y = linha(conteudo, fonte, "Orcamentos emitidos: " + r.orcamentosEmitidos(), y, 11);
                y = linha(conteudo, fonte, "Orcamentos aprovados: " + r.orcamentosAprovados(), y, 11);
                y = linha(conteudo, fonte, "Receita aprovada: R$ " + r.receitaAprovada(), y, 11);
                y = linha(conteudo, fonte, "Despesas aprovadas: R$ " + r.despesasAprovadas(), y, 11);
                linha(conteudo, fonte, "Contratos ativos: " + r.contratosAtivos(), y, 11);
            }
            documento.save(saida);
            return saida.toByteArray();
        } catch (IOException e) { throw new IllegalStateException("Não foi possível gerar o relatório em PDF", e); }
    }
    private float linha(PDPageContentStream c, PDFont fonte, String valor, float y, int tamanho) throws IOException {
        c.beginText(); c.setFont(fonte, tamanho); c.newLineAtOffset(42, y);
        c.showText(Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replaceAll("[^\\x20-\\x7E]", "?"));
        c.endText(); return y - tamanho - 11;
    }
}
