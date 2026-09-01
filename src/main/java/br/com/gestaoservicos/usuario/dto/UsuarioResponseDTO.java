package br.com.gestaoservicos.usuario.dto;

import br.com.gestaoservicos.usuario.model.Usuario;

import java.util.UUID;

public record UsuarioResponseDTO(UUID id, String nome, String email) {
    public static UsuarioResponseDTO de(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
