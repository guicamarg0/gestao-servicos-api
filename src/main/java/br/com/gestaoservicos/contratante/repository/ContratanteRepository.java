package br.com.gestaoservicos.contratante.repository;
import br.com.gestaoservicos.contratante.model.Contratante;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface ContratanteRepository extends JpaRepository<Contratante, UUID> {
    boolean existsByUnidadeIdAndDocumento(UUID unidadeId, String documento);
    boolean existsByUnidadeIdAndDocumentoAndIdNot(UUID unidadeId, String documento, UUID id);
    @Query("select c from Contratante c where c.unidade.id = :unidadeId and (:busca is null or lower(c.nomeRazaoSocial) like lower(concat('%', :busca, '%')) or lower(coalesce(c.nomeFantasia, '')) like lower(concat('%', :busca, '%')) or c.documento like concat('%', :busca, '%'))")
    Page<Contratante> buscar(UUID unidadeId, String busca, Pageable pageable);
    Optional<Contratante> findByIdAndUnidadeId(UUID id, UUID unidadeId);
}
