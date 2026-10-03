package br.com.gestaoservicos.servico.repository;

import br.com.gestaoservicos.servico.model.Servico;
import br.com.gestaoservicos.servico.model.StatusServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ServicoRepository extends JpaRepository<Servico, UUID> {
    Optional<Servico> findByIdAndUnidade_Id(UUID id, UUID unidadeId);

    @Query("select s from Servico s join s.contratante c where s.unidade.id = :unidadeId " +
           "and (:status is null or s.status = :status) and " +
           "(lower(s.codigo) like lower(concat('%', :busca, '%')) or " +
           "lower(s.titulo) like lower(concat('%', :busca, '%')) or " +
           "lower(c.nomeRazaoSocial) like lower(concat('%', :busca, '%')))")
    Page<Servico> buscar(UUID unidadeId, String busca, StatusServico status, Pageable pageable);

    @Query("select s from Servico s where s.unidade.id = :unidadeId and s.dataProgramada < :ate " +
           "and s.dataProgramada >= :de and (:responsavel is null or s.responsavel = :responsavel) " +
           "and s.status <> br.com.gestaoservicos.servico.model.StatusServico.CANCELADO")
    Page<Servico> agenda(UUID unidadeId, java.time.LocalDate de, java.time.LocalDate ate, String responsavel, Pageable pageable);

    @Query("select count(s) from Servico s where s.unidade.id = :unidadeId and s.id <> :ignorarId " +
           "and s.responsavel = :responsavel and s.inicioPrevisto < :fim and s.fimPrevisto > :inicio " +
           "and s.status not in (br.com.gestaoservicos.servico.model.StatusServico.CANCELADO, " +
           "br.com.gestaoservicos.servico.model.StatusServico.CONCLUIDO)")
    long contarConflitos(UUID unidadeId, UUID ignorarId, String responsavel, Instant inicio, Instant fim);

    @Query("select count(s) from Servico s where s.unidade.id = :unidadeId and s.status = :status " +
           "and s.atualizadoEm >= :de and s.atualizadoEm < :ate")
    long contarPorStatusNoPeriodo(UUID unidadeId, StatusServico status, Instant de, Instant ate);

    @Query("select s from Servico s where s.unidade.id = :unidadeId and s.concluidoEm >= :de " +
           "and s.concluidoEm < :ate and (:contratanteId is null or s.contratante.id = :contratanteId) " +
           "and (:responsavel is null or s.responsavel = :responsavel) order by s.concluidoEm desc")
    Page<Servico> concluidosNoPeriodo(UUID unidadeId, Instant de, Instant ate, UUID contratanteId,
                                       String responsavel, Pageable pagina);
}
