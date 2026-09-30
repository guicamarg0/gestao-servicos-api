package br.com.gestaoservicos.despesa.repository;

import br.com.gestaoservicos.despesa.model.*;
import java.math.BigDecimal;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDate;
import java.util.*;

public interface DespesaRepository extends JpaRepository<Despesa, UUID> {
    Optional<Despesa> findByIdAndUnidade_Id(UUID id, UUID unidadeId);
    @Query("select d from Despesa d where d.unidade.id = :unidadeId " +
           "and (:status is null or d.status = :status) and (:categoria is null or d.categoria = :categoria) " +
           "and (:de is null or d.dataDespesa >= :de) and (:ate is null or d.dataDespesa <= :ate) " +
           "and (lower(d.codigo) like lower(concat('%', :busca, '%')) or " +
           "lower(d.descricao) like lower(concat('%', :busca, '%')))")
    Page<Despesa> buscar(UUID unidadeId, String busca, StatusDespesa status, String categoria,
                         LocalDate de, LocalDate ate, Pageable pagina);
    @Query("select coalesce(sum(d.valor), 0) from Despesa d where d.unidade.id = :unidadeId " +
           "and d.status = :status and d.dataDespesa >= :de and d.dataDespesa <= :ate")
    BigDecimal somarPorStatusNoPeriodo(UUID unidadeId, StatusDespesa status, LocalDate de, LocalDate ate);
}
