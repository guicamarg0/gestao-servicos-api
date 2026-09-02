package br.com.gestaoservicos.convite.dto;
import br.com.gestaoservicos.associacao.model.Perfil; import jakarta.validation.constraints.*;
public record CriarConviteUnidadeRequestDTO(@NotBlank @Email String email, @NotNull Perfil perfil) {}
