package br.com.gestaoservicos.unidade.dto;

import br.com.gestaoservicos.associacao.model.Associacao;
import br.com.gestaoservicos.associacao.model.Perfil;

import java.util.UUID;

public record AssociacaoUnidadeResponseDTO(UUID id, String nome, Perfil perfil) {
    public static AssociacaoUnidadeResponseDTO de(Associacao associacao) {
        return new AssociacaoUnidadeResponseDTO(
                associacao.getUnidade().getId(), associacao.getUnidade().getNome(), associacao.getPerfil());
    }
}
