package br.com.gestaoservicos.despesa.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ItemDespesaRequestDTO(@NotBlank @Size(max=500) String descricao,
    @NotNull @DecimalMin(value="0",inclusive=false) @Digits(integer=11,fraction=4) BigDecimal quantidade,
    @NotNull @DecimalMin("0") @Digits(integer=13,fraction=2) BigDecimal valorUnitario) {}
