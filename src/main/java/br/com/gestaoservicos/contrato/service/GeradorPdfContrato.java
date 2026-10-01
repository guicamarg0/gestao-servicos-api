package br.com.gestaoservicos.contrato.service;

import br.com.gestaoservicos.contrato.model.Contrato;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.stereotype.Service;
import java.io.*;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeradorPdfContrato {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    public byte[] gerar(Contrato contrato) {
        var linhas = new ArrayList<Linha>();
        linhas.add(new Linha("CONTRATO " + contrato.getNumero() + "  |  VERSÃO " + contrato.getNumeroVersao(), 11));
        bloco(linhas, null, contrato.getConteudoRascunho());
        if (contrato.getAssinadoPor() != null) {
            linhas.add(new Linha("Assinado por: " + contrato.getAssinadoPor(), 10));
            linhas.add(new Linha("Data: " + DATA.format(contrato.getAssinadoEm()) + "  Canal: " + contrato.getCanalAssinatura(), 10));
        }
        try (var documento = new PDDocument(); var saida = new ByteArrayOutputStream()) {
            var fonte = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDPageContentStream conteudo = null;
            float y = 770;
            try {
                for (var linha : linhas) {
                    if (conteudo == null || y - linha.tamanho() - 8 < 45) {
                        if (conteudo != null) conteudo.close();
                        var pagina = new PDPage();
                        documento.addPage(pagina);
                        conteudo = new PDPageContentStream(documento, pagina);
                        y = 770;
                    }
                    conteudo.beginText();
                    conteudo.setFont(fonte, linha.tamanho());
                    conteudo.newLineAtOffset(42, y);
                    conteudo.showText(ascii(linha.texto()));
                    conteudo.endText();
                    y -= linha.tamanho() + 8;
                }
            } finally {
                if (conteudo != null) conteudo.close();
            }
            documento.save(saida);
            return saida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível gerar o PDF do contrato", e);
        }
    }

    private void bloco(List<Linha> linhas, String titulo, String texto) {
        if (texto == null || texto.isBlank()) return;
        if (titulo != null) linhas.add(new Linha(titulo, 11));
        String normalizado = texto.replaceAll("(?i)<br\\s*/?>", "\\n").replaceAll("(?i)</(p|div|h[1-6]|li)>", "\\n").replaceAll("(?i)<li[^>]*>", "• ").replaceAll("<[^>]+>", "").replace("&nbsp;", " ").replace("&amp;", "&");
        for (String paragrafo : normalizado.split("\\R")) {
            String linha = ascii(paragrafo).trim();
            if (linha.isEmpty()) { linhas.add(new Linha(" ", 7)); continue; }
            while (linha.length() > 92) { int corte = linha.lastIndexOf(' ', 92); if (corte < 1) corte = 92; linhas.add(new Linha(linha.substring(0, corte), 10)); linha = linha.substring(corte).trim(); }
            linhas.add(new Linha(linha, 10));
        }
    }

    private String ascii(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replaceAll("[^\\x20-\\x7E]", "?");
    }

    private record Linha(String texto, int tamanho) {}
}
