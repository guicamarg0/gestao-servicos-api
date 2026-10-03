package br.com.gestaoservicos.despesa.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DespesaRequestDTO(@NotBlank @Size(max = 100) String categoria,
        @NotBlank @Size(max = 500) String descricao,
        @DecimalMin(value = "0.01") @Digits(integer=13,fraction=2) BigDecimal valor,
        @NotNull LocalDate dataDespesa, UUID servicoId, UUID orcamentoId,
        @Size(max = 120) String responsavel, @Size(max = 1000) String comprovanteUrl,
        boolean enviarParaAprovacao, @Size(max=200) java.util.List<@NotNull @jakarta.validation.Valid ItemDespesaRequestDTO> itens) {}
