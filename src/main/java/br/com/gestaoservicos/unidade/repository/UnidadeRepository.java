package br.com.gestaoservicos.unidade.repository;

import br.com.gestaoservicos.unidade.model.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UnidadeRepository extends JpaRepository<Unidade, UUID> {}
