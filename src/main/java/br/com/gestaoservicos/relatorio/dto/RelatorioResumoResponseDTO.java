package br.com.gestaoservicos.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RelatorioResumoResponseDTO(LocalDate de, LocalDate ate, long servicosConcluidos,
        long servicosEmAndamento, long servicosAgendados, long orcamentosEmitidos,
        long orcamentosAprovados, BigDecimal receitaAprovada, BigDecimal despesasAprovadas,
        long contratosAtivos) {}
