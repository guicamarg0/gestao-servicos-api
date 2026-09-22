package br.com.gestaoservicos.convite.dto;

import br.com.gestaoservicos.associacao.model.Perfil;
import br.com.gestaoservicos.convite.model.ConviteUnidade;
import java.time.Instant;
import java.util.UUID;

public record ConviteUnidadeResponseDTO(UUID id, Perfil perfil, Instant expiraEm,
                                         boolean disponivel, String codigoConvite) {
    public static ConviteUnidadeResponseDTO de(ConviteUnidade convite, String codigo) {
        return new ConviteUnidadeResponseDTO(convite.getId(), convite.getPerfil(), convite.getExpiraEm(),
                convite.estaDisponivel(), codigo);
    }
}
