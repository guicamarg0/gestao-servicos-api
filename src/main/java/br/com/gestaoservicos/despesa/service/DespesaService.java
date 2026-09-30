package br.com.gestaoservicos.despesa.service;

import br.com.gestaoservicos.compartilhado.paginacao.PaginaResponseDTO;
import br.com.gestaoservicos.despesa.dto.*;
import br.com.gestaoservicos.despesa.model.*;
import br.com.gestaoservicos.despesa.repository.DespesaRepository;
import br.com.gestaoservicos.orcamento.model.Orcamento;
import br.com.gestaoservicos.orcamento.repository.OrcamentoRepository;
import br.com.gestaoservicos.servico.model.Servico;
import br.com.gestaoservicos.servico.repository.ServicoRepository;
import br.com.gestaoservicos.unidade.repository.UnidadeRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class DespesaService {
    private final DespesaRepository despesas;
    private final ServicoRepository servicos;
    private final OrcamentoRepository orcamentos;
    private final UnidadeRepository unidades;
    public DespesaService(DespesaRepository despesas, ServicoRepository servicos,
                          OrcamentoRepository orcamentos, UnidadeRepository unidades) {
        this.despesas = despesas; this.servicos = servicos; this.orcamentos = orcamentos; this.unidades = unidades;
    }
    @Transactional(readOnly = true)
    public PaginaResponseDTO<DespesaResponseDTO> listar(UUID unidadeId, String busca, StatusDespesa status,
            String categoria, LocalDate de, LocalDate ate, Pageable pagina) {
        return PaginaResponseDTO.de(despesas.buscar(unidadeId, busca == null ? "" : busca.strip(), status,
                categoria == null || categoria.isBlank() ? null : categoria.strip(), de, ate, pagina).map(this::resposta));
    }
    @Transactional(readOnly = true)
    public DespesaResponseDTO consultar(UUID unidadeId, UUID id) { return resposta(obter(unidadeId, id)); }
    @Transactional
    public DespesaResponseDTO criar(UUID unidadeId, DespesaRequestDTO dto) {
        var unidade = unidades.findById(unidadeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var despesa = new Despesa(unidade, servico(unidadeId, dto.servicoId()), orcamento(unidadeId, dto.orcamentoId()),
                dto.categoria(), dto.descricao(), dto.valor(), dto.dataDespesa(), dto.responsavel(),
                dto.comprovanteUrl(), dto.enviarParaAprovacao());
        return resposta(despesas.save(despesa));
    }
    @Transactional
    public DespesaResponseDTO atualizar(UUID unidadeId, UUID id, DespesaRequestDTO dto) {
        var despesa = obter(unidadeId, id);
        if (despesa.getStatus() != StatusDespesa.RASCUNHO && despesa.getStatus() != StatusDespesa.REJEITADA)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente rascunho ou despesa rejeitada pode ser editada");
        despesa.atualizar(servico(unidadeId, dto.servicoId()), orcamento(unidadeId, dto.orcamentoId()),
                dto.categoria(), dto.descricao(), dto.valor(), dto.dataDespesa(), dto.responsavel(), dto.comprovanteUrl());
        if (dto.enviarParaAprovacao()) despesa.enviar();
        return resposta(despesa);
    }
    @Transactional
    public DespesaResponseDTO enviar(UUID unidadeId, UUID id) {
        var despesa = obter(unidadeId, id);
        if (despesa.getStatus() != StatusDespesa.RASCUNHO && despesa.getStatus() != StatusDespesa.REJEITADA)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Despesa não pode ser enviada para aprovação");
        despesa.enviar(); return resposta(despesa);
    }
    @Transactional
    public DespesaResponseDTO decidir(UUID unidadeId, UUID id, boolean aprovar,
                                      DecisaoDespesaRequestDTO dto, UUID usuarioId) {
        var despesa = obter(unidadeId, id);
        if (despesa.getStatus() != StatusDespesa.PENDENTE)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A despesa não está pendente");
        despesa.decidir(aprovar ? StatusDespesa.APROVADA : StatusDespesa.REJEITADA,
                dto.comentario() == null ? null : dto.comentario().strip(), usuarioId);
        return resposta(despesa);
    }
    private Despesa obter(UUID unidadeId, UUID id) {
        return despesas.findByIdAndUnidade_Id(id, unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não encontrada"));
    }
    private Servico servico(UUID unidadeId, UUID id) {
        if (id == null) return null;
        return servicos.findByIdAndUnidade_Id(id, unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço não pertence à unidade"));
    }
    private Orcamento orcamento(UUID unidadeId, UUID id) {
        if (id == null) return null;
        return orcamentos.findByIdAndUnidadeId(id, unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento não pertence à unidade"));
    }
    private DespesaResponseDTO resposta(Despesa d) {
        return new DespesaResponseDTO(d.getId(), d.getCodigo(), d.getCategoria(), d.getDescricao(), d.getValor(),
                d.getDataDespesa(), d.getServico() == null ? null : d.getServico().getId(),
                d.getServico() == null ? null : d.getServico().getCodigo(),
                d.getOrcamento() == null ? null : d.getOrcamento().getId(), d.getResponsavel(),
                d.getComprovanteUrl(), d.getStatus(), d.getComentarioDecisao(), d.getDecididoEm(), d.getDecididoPor());
    }
}
