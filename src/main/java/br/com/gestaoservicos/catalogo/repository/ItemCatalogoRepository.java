package br.com.gestaoservicos.catalogo.repository;

import br.com.gestaoservicos.catalogo.model.ItemCatalogo;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface ItemCatalogoRepository extends JpaRepository<ItemCatalogo, UUID> {
    @Query("select i from ItemCatalogo i where i.unidade.id = :unidadeId and (lower(i.nome) like lower(concat('%', :busca, '%')) or lower(coalesce(i.codigoReferencia, '')) like lower(concat('%', :busca, '%')))")
    Page<ItemCatalogo> buscar(UUID unidadeId, String busca, Pageable pageable);
    Optional<ItemCatalogo> findByIdAndUnidadeId(UUID id, UUID unidadeId);
}
