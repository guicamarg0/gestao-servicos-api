package br.com.gestaoservicos.despesa.dto;

import br.com.gestaoservicos.despesa.model.StatusDespesa;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DespesaResponseDTO(UUID id, String codigo, String categoria, String descricao,
        BigDecimal valor, LocalDate dataDespesa, UUID servicoId, String servicoCodigo, UUID orcamentoId,
        String responsavel, String comprovanteUrl, StatusDespesa status,
        String comentarioDecisao, Instant decididoEm, UUID decididoPor) {}
