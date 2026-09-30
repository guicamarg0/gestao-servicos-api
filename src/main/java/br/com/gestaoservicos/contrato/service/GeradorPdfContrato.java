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
        linhas.add(new Linha("CONTRATO DE PRESTACAO DE SERVICOS", 15));
        linhas.add(new Linha("Numero: " + contrato.getNumero() + "  Versao: " + contrato.getNumeroVersao(), 10));
        linhas.add(new Linha("Cliente: " + contrato.getContratante().getNomeRazaoSocial(), 10));
        linhas.add(new Linha("Origem: " + contrato.getOrcamento().getNumero(), 10));
        linhas.add(new Linha("Vigencia: " + DATA.format(contrato.getInicioVigencia()) + " a " + DATA.format(contrato.getFimVigencia()), 10));
        linhas.add(new Linha("Valor: R$ " + contrato.getValorTotal().toPlainString(), 10));
        linhas.add(new Linha("Modelo: " + contrato.getModelo(), 10));
        bloco(linhas, "OBJETO", contrato.getObjeto());
        bloco(linhas, "CLAUSULAS ADICIONAIS", contrato.getClausulasAdicionais());
        bloco(linhas, "CONDICOES DE PAGAMENTO", contrato.getCondicoesPagamento());
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
        linhas.add(new Linha(" ", 8));
        linhas.add(new Linha(titulo, 11));
        String normalizado = ascii(texto).replaceAll("\\s+", " ");
        for (int i = 0; i < normalizado.length(); i += 92)
            linhas.add(new Linha(normalizado.substring(i, Math.min(i + 92, normalizado.length())), 10));
    }

    private String ascii(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replaceAll("[^\\x20-\\x7E]", "?");
    }

    private record Linha(String texto, int tamanho) {}
}
