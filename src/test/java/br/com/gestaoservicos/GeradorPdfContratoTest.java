package br.com.gestaoservicos;

import br.com.gestaoservicos.contrato.model.Contrato;
import br.com.gestaoservicos.contrato.model.StatusContrato;
import br.com.gestaoservicos.contrato.service.GeradorPdfContrato;
import br.com.gestaoservicos.orcamento.model.Orcamento;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class GeradorPdfContratoTest {
    @Test void preserva_html_formatado_no_pdf() {
        var contrato = mock(Contrato.class); var orcamento = mock(Orcamento.class);
        when(contrato.getNumero()).thenReturn("CT-001"); when(contrato.getNumeroVersao()).thenReturn(1);
        when(contrato.getOrcamento()).thenReturn(orcamento); when(orcamento.getNumero()).thenReturn("52");
        when(contrato.getConteudoRascunho()).thenReturn("<h1>Contrato</h1><p><strong>Cliente:</strong> PrismaPack</p><ul><li>Cláusula 1</li></ul>");
        when(contrato.getStatus()).thenReturn(StatusContrato.RASCUNHO);
        assertTrue(new GeradorPdfContrato().gerar(contrato).length > 500);
    }
}
