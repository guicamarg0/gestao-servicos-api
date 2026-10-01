package br.com.gestaoservicos.contrato.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record AssinaturaContratoRequestDTO(@NotBlank @Size(max = 120) String assinadoPor,
        @NotNull LocalDate assinadoEm, @NotBlank @Size(max = 120) String canalAssinatura,
        @Size(max = 1000) String evidenciaUrl) {}
