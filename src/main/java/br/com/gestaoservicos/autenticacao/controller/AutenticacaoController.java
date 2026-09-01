package br.com.gestaoservicos.autenticacao.controller;

import br.com.gestaoservicos.autenticacao.dto.CadastroRequestDTO;
import br.com.gestaoservicos.autenticacao.dto.LoginRequestDTO;
import br.com.gestaoservicos.autenticacao.dto.TokenResponseDTO;
import br.com.gestaoservicos.autenticacao.service.AutenticacaoService;
import br.com.gestaoservicos.usuario.dto.UsuarioResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/autenticacao")
public class AutenticacaoController {
    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    UsuarioResponseDTO cadastrar(@Valid @RequestBody CadastroRequestDTO requisicao) {
        return UsuarioResponseDTO.de(autenticacaoService.cadastrar(requisicao.nome(), requisicao.email(), requisicao.senha()));
    }

    @PostMapping("/login")
    TokenResponseDTO login(@Valid @RequestBody LoginRequestDTO requisicao) {
        return autenticacaoService.autenticar(requisicao.email(), requisicao.senha());
    }
}
