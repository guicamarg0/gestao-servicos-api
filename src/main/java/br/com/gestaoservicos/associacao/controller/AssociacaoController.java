package br.com.gestaoservicos.associacao.controller;

import br.com.gestaoservicos.associacao.dto.AdicionarUsuarioUnidadeRequestDTO;
import br.com.gestaoservicos.associacao.dto.AtualizarPerfilAssociacaoRequestDTO;
import br.com.gestaoservicos.associacao.dto.UsuarioUnidadeResponseDTO;
import br.com.gestaoservicos.associacao.service.AssociacaoService;
import br.com.gestaoservicos.security.ContextoUnidade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/unidades/atual/usuarios")
@PreAuthorize("@autorizacaoUnidade.possuiAlgumPerfil('ADMIN')")
public class AssociacaoController {
    private final AssociacaoService associacaoService;

    public AssociacaoController(AssociacaoService associacaoService) { this.associacaoService = associacaoService; }

    @GetMapping
    List<UsuarioUnidadeResponseDTO> listar() { return associacaoService.listarAtivos(unidadeIdAtual()); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UsuarioUnidadeResponseDTO adicionar(@Valid @RequestBody AdicionarUsuarioUnidadeRequestDTO requisicao) {
        return associacaoService.adicionar(unidadeIdAtual(), requisicao.email(), requisicao.perfil());
    }

    @PatchMapping("/{associacaoId}/perfil")
    UsuarioUnidadeResponseDTO alterarPerfil(@PathVariable UUID associacaoId,
                                             @Valid @RequestBody AtualizarPerfilAssociacaoRequestDTO requisicao) {
        return associacaoService.alterarPerfil(unidadeIdAtual(), associacaoId, requisicao.perfil());
    }

    @DeleteMapping("/{associacaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void desativar(@PathVariable UUID associacaoId) { associacaoService.desativar(unidadeIdAtual(), associacaoId); }

    private UUID unidadeIdAtual() {
        return ContextoUnidade.atual().map(ContextoUnidade.Selecao::unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma unidade pelo cabeçalho X-Unidade-Id"));
    }
}
