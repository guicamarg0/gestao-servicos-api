package br.com.gestaoservicos.servico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record AgendamentoRequestDTO(@NotNull Instant inicioPrevisto, @NotNull Instant fimPrevisto,
                                    @NotBlank String responsavel) {}
