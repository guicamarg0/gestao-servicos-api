package br.com.gestaoservicos.servico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record AgendamentoRequestDTO(@NotNull java.time.LocalDate dataProgramada,
                                    @NotBlank @jakarta.validation.constraints.Size(max = 120) String responsavel) {}
