package br.com.gestaoservicos.contrato.service;

import br.com.gestaoservicos.contrato.model.Contrato;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class GeradorPdfContrato {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    public byte[] gerar(Contrato contrato) {
        try (var saida = new ByteArrayOutputStream()) {
            var conteudo = normalizarHtml(contrato.getConteudoRascunho());
            String assinatura = contrato.getAssinadoPor() == null ? "" : """
                <section class=\"assinatura\"><strong>Assinado por:</strong> %s<br/>
                <strong>Data:</strong> %s &nbsp; <strong>Canal:</strong> %s</section>""".formatted(
                    escapar(contrato.getAssinadoPor()), DATA.format(contrato.getAssinadoEm()), escapar(contrato.getCanalAssinatura()));
            String html = """
                <!DOCTYPE html><html><head><meta charset=\"UTF-8\" />
                <style>
                  @page { size: A4; margin: 22mm 18mm; }
                  body { font-family: sans-serif; color: #102a56; font-size: 11pt; line-height: 1.5; }
                  .cabecalho { border-bottom: 2px solid #1768e8; padding-bottom: 9px; margin-bottom: 22px; }
                  .cabecalho h1 { font-size: 16pt; margin: 0; color: #0a2350; }
                  .cabecalho p { color: #506990; font-size: 9pt; margin: 3px 0 0; }
                  h1 { font-size: 18pt; } h2 { font-size: 15pt; } h3 { font-size: 12pt; }
                  p { margin: 0 0 10px; } ul, ol { margin: 0 0 12px 20px; padding: 0; }
                  li { margin: 0 0 4px; } strong, b { font-weight: bold; } em, i { font-style: italic; }
                  .assinatura { margin-top: 32px; border-top: 1px solid #cad5e5; padding-top: 12px; font-size: 10pt; }
                </style></head><body>
                  <header class=\"cabecalho\"><h1>Contrato %s</h1><p>Versão %s · Orçamento #%s</p></header>
                  <main>%s</main>%s
                </body></html>""".formatted(escapar(contrato.getNumero()), contrato.getNumeroVersao(), escapar(contrato.getOrcamento().getNumero()), conteudo, assinatura);
            new PdfRendererBuilder().withHtmlContent(html, null).toStream(saida).run();
            return saida.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível gerar o PDF do contrato", e);
        }
    }

    private String normalizarHtml(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) return "<p>Contrato sem conteúdo.</p>";
        var documento = Jsoup.parseBodyFragment(conteudo);
        documento.outputSettings(new OutputSettings().syntax(OutputSettings.Syntax.xml));
        return documento.body().html();
    }

    private String escapar(String texto) {
        return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
