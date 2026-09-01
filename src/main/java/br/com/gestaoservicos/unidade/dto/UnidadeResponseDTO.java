package br.com.gestaoservicos.unidade.dto;

import br.com.gestaoservicos.associacao.model.Perfil;
import br.com.gestaoservicos.unidade.model.Unidade;

import java.util.UUID;

public record UnidadeResponseDTO(UUID id, String nome, Perfil perfil) {
    public static UnidadeResponseDTO de(Unidade unidade, Perfil perfil) {
        return new UnidadeResponseDTO(unidade.getId(), unidade.getNome(), perfil);
    }
}
