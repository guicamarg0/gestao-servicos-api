package br.com.gestaoservicos.associacao.repository;

import br.com.gestaoservicos.associacao.model.Associacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssociacaoRepository extends JpaRepository<Associacao, UUID> {
    @EntityGraph(attributePaths = "unidade")
    List<Associacao> findAllByUsuarioIdOrderByUnidadeNome(UUID usuarioId);

    @EntityGraph(attributePaths = {"unidade", "usuario"})
    Optional<Associacao> findByUsuarioIdAndUnidadeId(UUID usuarioId, UUID unidadeId);
}
