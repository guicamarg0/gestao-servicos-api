package br.com.gestaoservicos.despesa.controller;

import br.com.gestaoservicos.compartilhado.paginacao.PaginaResponseDTO;
import br.com.gestaoservicos.despesa.dto.*;
import br.com.gestaoservicos.despesa.model.StatusDespesa;
import br.com.gestaoservicos.despesa.service.DespesaService;
import br.com.gestaoservicos.security.ContextoUnidade;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/despesas")
public class DespesaController {
    private final DespesaService service;
    public DespesaController(DespesaService service) { this.service = service; }
    @GetMapping @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR','OPERADOR','CONSULTA')")
    public PaginaResponseDTO<DespesaResponseDTO> listar(@RequestParam(required = false) String busca,
            @RequestParam(required = false) StatusDespesa status, @RequestParam(required = false) String categoria,
            @RequestParam(required = false) LocalDate de, @RequestParam(required = false) LocalDate ate,
            @RequestParam(required = false) UUID servicoId,
            @PageableDefault(sort = "dataDespesa") Pageable pagina) {
        return service.listar(unidade(), busca, status, categoria, de, ate, servicoId, pagina);
    }
    @GetMapping("/{id}") @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR','OPERADOR','CONSULTA')")
    public DespesaResponseDTO consultar(@PathVariable UUID id) { return service.consultar(unidade(), id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR','OPERADOR')")
    public DespesaResponseDTO criar(@Valid @RequestBody DespesaRequestDTO dto) { return service.criar(unidade(), dto); }
    @PutMapping("/{id}") @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR','OPERADOR')")
    public DespesaResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody DespesaRequestDTO dto) { return service.atualizar(unidade(), id, dto); }
    @PostMapping("/{id}/enviar") @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR','OPERADOR')")
    public DespesaResponseDTO enviar(@PathVariable UUID id) { return service.enviar(unidade(), id); }
    @PostMapping("/{id}/aprovar") @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR')")
    public DespesaResponseDTO aprovar(@PathVariable UUID id, @Valid @RequestBody DecisaoDespesaRequestDTO dto,
                                      Authentication autenticacao) {
        return service.decidir(unidade(), id, true, dto, UUID.fromString(autenticacao.getName()));
    }
    @PostMapping("/{id}/rejeitar") @PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN','GESTOR')")
    public DespesaResponseDTO rejeitar(@PathVariable UUID id, @Valid @RequestBody DecisaoDespesaRequestDTO dto,
                                       Authentication autenticacao) {
        return service.decidir(unidade(), id, false, dto, UUID.fromString(autenticacao.getName()));
    }
    private UUID unidade() {
        return ContextoUnidade.atual().map(ContextoUnidade.Selecao::unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma unidade"));
    }
}
