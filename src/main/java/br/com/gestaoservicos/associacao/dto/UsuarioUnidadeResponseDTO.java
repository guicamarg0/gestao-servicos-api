package br.com.gestaoservicos.associacao.dto;

import br.com.gestaoservicos.associacao.model.Associacao;
import br.com.gestaoservicos.associacao.model.Perfil;

import java.util.UUID;

public record UsuarioUnidadeResponseDTO(UUID id, UUID usuarioId, String nome, String email, Perfil perfil) {
    public static UsuarioUnidadeResponseDTO de(Associacao associacao) {
        return new UsuarioUnidadeResponseDTO(associacao.getId(), associacao.getUsuario().getId(),
                associacao.getUsuario().getNome(), associacao.getUsuario().getEmail(), associacao.getPerfil());
    }
}
