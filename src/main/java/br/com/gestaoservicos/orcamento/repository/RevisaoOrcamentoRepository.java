package br.com.gestaoservicos.orcamento.repository;

import br.com.gestaoservicos.orcamento.model.RevisaoOrcamento;
import br.com.gestaoservicos.orcamento.model.StatusOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public interface RevisaoOrcamentoRepository extends JpaRepository<RevisaoOrcamento,UUID> {
    Optional<RevisaoOrcamento> findByOrcamentoIdAndNumeroRevisao(UUID orcamentoId,int numeroRevisao);
    List<RevisaoOrcamento> findAllByOrcamentoIdOrderByNumeroRevisaoDesc(UUID orcamentoId);
    @Query("select count(r) from RevisaoOrcamento r where r.orcamento.unidade.id = :unidadeId " +
           "and r.numeroRevisao = r.orcamento.revisaoAtual and r.status = :status " +
           "and r.atualizadoEm >= :de and r.atualizadoEm < :ate")
    long contarAtuaisPorStatusNoPeriodo(UUID unidadeId, StatusOrcamento status, Instant de, Instant ate);
    @Query("select coalesce(sum(r.totalFinal), 0) from RevisaoOrcamento r where r.orcamento.unidade.id = :unidadeId " +
           "and r.numeroRevisao = r.orcamento.revisaoAtual and r.status = :status " +
           "and r.atualizadoEm >= :de and r.atualizadoEm < :ate")
    BigDecimal somarAtuaisPorStatusNoPeriodo(UUID unidadeId, StatusOrcamento status, Instant de, Instant ate);
}
