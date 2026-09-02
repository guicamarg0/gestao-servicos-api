package br.com.gestaoservicos.convite.dto;
import br.com.gestaoservicos.associacao.model.Perfil; import br.com.gestaoservicos.convite.model.ConviteUnidade; import java.time.Instant; import java.util.UUID;
public record ConviteUnidadeResponseDTO(UUID id,String email,Perfil perfil,Instant expiraEm,boolean disponivel,String codigoConvite){ public static ConviteUnidadeResponseDTO de(ConviteUnidade c,String codigo){return new ConviteUnidadeResponseDTO(c.getId(),c.getEmail(),c.getPerfil(),c.getExpiraEm(),c.estaDisponivel(),codigo);} }
