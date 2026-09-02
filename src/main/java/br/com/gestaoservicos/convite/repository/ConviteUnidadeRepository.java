package br.com.gestaoservicos.convite.repository;
import br.com.gestaoservicos.convite.model.ConviteUnidade;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface ConviteUnidadeRepository extends JpaRepository<ConviteUnidade, UUID> {
 @EntityGraph(attributePaths="unidade") List<ConviteUnidade> findAllByUnidadeIdOrderByExpiraEmDesc(UUID unidadeId);
 @EntityGraph(attributePaths="unidade") Optional<ConviteUnidade> findByTokenHash(String tokenHash);
 Optional<ConviteUnidade> findByIdAndUnidadeId(UUID id, UUID unidadeId);
}
