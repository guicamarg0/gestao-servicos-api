package br.com.gestaoservicos.contrato.controller;

import br.com.gestaoservicos.compartilhado.paginacao.PaginaResponseDTO;
import br.com.gestaoservicos.contrato.dto.*;
import br.com.gestaoservicos.contrato.model.StatusContrato;
import br.com.gestaoservicos.contrato.service.ContratoService;
import br.com.gestaoservicos.security.ContextoUnidade;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/contratos")
public class ContratoController {
    private final ContratoService service;
    public ContratoController(ContratoService service) { this.service = service; }
    @GetMapping @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR','OPERADOR','CONSULTA')")
    public PaginaResponseDTO<ContratoResponseDTO> listar(@RequestParam(required = false) String busca,
            @RequestParam(required = false) StatusContrato status, @PageableDefault(sort = "inicioVigencia") Pageable pagina) {
        return service.listar(unidade(), busca, status, pagina);
    }
    @GetMapping("/{id}") @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR','OPERADOR','CONSULTA')")
    public ContratoResponseDTO consultar(@PathVariable UUID id) { return service.consultar(unidade(), id); }
    @GetMapping("/{id}/snapshots") @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR','OPERADOR','CONSULTA')")
    public java.util.List<ContratoSnapshotResponseDTO> snapshots(@PathVariable UUID id) { return service.snapshots(unidade(), id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR')")
    public ContratoResponseDTO criar(@Valid @RequestBody ContratoRequestDTO dto) { return service.criar(unidade(), dto); }
    @PutMapping("/{id}") @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR')")
    public ContratoResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody ContratoRequestDTO dto) {
        return service.atualizar(unidade(), id, dto);
    }
    @PostMapping("/{id}/enviar-assinatura") @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR')")
    public ContratoResponseDTO enviar(@PathVariable UUID id) { return service.enviar(unidade(), id); }
    @PostMapping("/{id}/registrar-assinatura") @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR')")
    public ContratoResponseDTO assinar(@PathVariable UUID id, @Valid @RequestBody AssinaturaContratoRequestDTO dto) {
        return service.assinar(unidade(), id, dto);
    }
    @PostMapping("/{id}/encerrar") @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR')")
    public ContratoResponseDTO encerrar(@PathVariable UUID id) { return service.encerrar(unidade(), id); }
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR','OPERADOR','CONSULTA')")
    public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contrato-" + id + ".pdf")
                .body(service.pdf(unidade(), id));
    }
    private UUID unidade() {
        return ContextoUnidade.atual().map(ContextoUnidade.Selecao::unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma unidade"));
    }
}
