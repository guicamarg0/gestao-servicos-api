package br.com.gestaoservicos.usuario.service;

import br.com.gestaoservicos.associacao.repository.AssociacaoRepository;
import br.com.gestaoservicos.unidade.dto.AssociacaoUnidadeResponseDTO;
import br.com.gestaoservicos.usuario.dto.MeResponseDTO;
import br.com.gestaoservicos.usuario.model.Usuario;
import br.com.gestaoservicos.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final AssociacaoRepository associacoes;

    public UsuarioService(UsuarioRepository usuarios, AssociacaoRepository associacoes) {
        this.usuarios = usuarios;
        this.associacoes = associacoes;
    }

    @Transactional(readOnly = true)
    public MeResponseDTO consultarMe(UUID usuarioId) {
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));
        var unidades = associacoes.findAllByUsuarioIdOrderByUnidadeNome(usuarioId).stream()
                .map(AssociacaoUnidadeResponseDTO::de)
                .toList();
        return new MeResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), unidades);
    }

    @Transactional(readOnly = true)
    public AssociacaoUnidadeResponseDTO selecionarUnidade(UUID usuarioId, UUID unidadeId) {
        return associacoes.findByUsuarioIdAndUnidadeId(usuarioId, unidadeId)
                .map(AssociacaoUnidadeResponseDTO::de)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Usuário não possui acesso à unidade informada"));
    }
}
