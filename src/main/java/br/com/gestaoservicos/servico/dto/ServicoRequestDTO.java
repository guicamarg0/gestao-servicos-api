package br.com.gestaoservicos.servico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ServicoRequestDTO(
        @NotNull UUID contratanteId,
        UUID orcamentoId,
        @NotBlank @Size(max = 200) String titulo,
        @Size(max = 100) String categoria,
        @Size(max = 4000) String descricao,
        @Size(max = 120) String responsavel,
        @Size(max = 120) String equipe,
        @Size(max = 500) String localExecucao) {}
