package br.com.gestaoservicos.relatorio.controller;

import br.com.gestaoservicos.relatorio.dto.RelatorioResumoResponseDTO;
import br.com.gestaoservicos.relatorio.service.RelatorioService;
import br.com.gestaoservicos.relatorio.service.GeradorPdfRelatorio;
import br.com.gestaoservicos.servico.service.ServicoService;
import br.com.gestaoservicos.servico.dto.ServicoResponseDTO;
import br.com.gestaoservicos.compartilhado.paginacao.PaginaResponseDTO;
import br.com.gestaoservicos.security.ContextoUnidade;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/relatorios")
@PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR','CONSULTA')")
public class RelatorioController {
    private final RelatorioService service;
    private final ServicoService servicos;
    private final GeradorPdfRelatorio pdf;
    public RelatorioController(RelatorioService service, ServicoService servicos, GeradorPdfRelatorio pdf) {
        this.service = service; this.servicos = servicos; this.pdf = pdf;
    }
    @GetMapping("/resumo")
    public RelatorioResumoResponseDTO resumo(@RequestParam LocalDate de, @RequestParam LocalDate ate,
            @RequestParam(defaultValue = "America/Sao_Paulo") String fuso) {
        return service.resumo(unidade(), de, ate, fuso);
    }
    @GetMapping(value = "/resumo.csv", produces = "text/csv; charset=utf-8")
    public ResponseEntity<byte[]> csv(@RequestParam LocalDate de, @RequestParam LocalDate ate,
            @RequestParam(defaultValue = "America/Sao_Paulo") String fuso) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-" + de + "-" + ate + ".csv")
                .body(service.csv(service.resumo(unidade(), de, ate, fuso)));
    }
    @GetMapping(value = "/resumo.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@RequestParam LocalDate de, @RequestParam LocalDate ate,
            @RequestParam(defaultValue = "America/Sao_Paulo") String fuso) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-" + de + "-" + ate + ".pdf")
                .body(pdf.gerar(service.resumo(unidade(), de, ate, fuso)));
    }
    @GetMapping("/servicos")
    public PaginaResponseDTO<ServicoResponseDTO> servicos(@RequestParam LocalDate de, @RequestParam LocalDate ate,
            @RequestParam(required = false) UUID contratanteId, @RequestParam(required = false) String responsavel,
            @RequestParam(defaultValue = "America/Sao_Paulo") String fuso,
            @PageableDefault(size = 20) Pageable pagina) {
        if (de.isAfter(ate)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Período inválido");
        var zona = service.zona(fuso);
        return servicos.concluidos(unidade(), de.atStartOfDay(zona).toInstant(),
                ate.plusDays(1).atStartOfDay(zona).toInstant(), contratanteId, responsavel, pagina);
    }
    private UUID unidade() {
        return ContextoUnidade.atual().map(ContextoUnidade.Selecao::unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma unidade"));
    }
}
