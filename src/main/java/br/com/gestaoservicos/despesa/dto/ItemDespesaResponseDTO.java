package br.com.gestaoservicos.despesa.dto;
import java.math.BigDecimal;
public record ItemDespesaResponseDTO(String descricao,BigDecimal quantidade,BigDecimal valorUnitario,BigDecimal total) {}
