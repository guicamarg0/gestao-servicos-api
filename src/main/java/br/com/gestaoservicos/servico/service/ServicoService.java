package br.com.gestaoservicos.servico.service;

import br.com.gestaoservicos.compartilhado.paginacao.PaginaResponseDTO;
import br.com.gestaoservicos.contratante.model.Contratante;
import br.com.gestaoservicos.contratante.repository.ContratanteRepository;
import br.com.gestaoservicos.orcamento.model.Orcamento;
import br.com.gestaoservicos.orcamento.repository.OrcamentoRepository;
import br.com.gestaoservicos.servico.dto.*;
import br.com.gestaoservicos.servico.model.*;
import br.com.gestaoservicos.servico.repository.ServicoRepository;
import br.com.gestaoservicos.unidade.repository.UnidadeRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.UUID;

@Service
public class ServicoService {
    private final ServicoRepository servicos;
    private final ContratanteRepository contratantes;
    private final OrcamentoRepository orcamentos;
    private final UnidadeRepository unidades;

    public ServicoService(ServicoRepository servicos, ContratanteRepository contratantes,
                          OrcamentoRepository orcamentos, UnidadeRepository unidades) {
        this.servicos = servicos;
        this.contratantes = contratantes;
        this.orcamentos = orcamentos;
        this.unidades = unidades;
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<ServicoResponseDTO> listar(UUID unidadeId, String busca, StatusServico status, Pageable pagina) {
        return PaginaResponseDTO.de(servicos.buscar(unidadeId, busca == null ? "" : busca.strip(), status, pagina).map(this::resposta));
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<ServicoResponseDTO> agenda(UUID unidadeId, Instant de, Instant ate,
                                                        String responsavel, Pageable pagina) {
        if (!de.isBefore(ate)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Período inválido");
        return PaginaResponseDTO.de(servicos.agenda(unidadeId, de, ate,
                responsavel == null || responsavel.isBlank() ? null : responsavel.strip(), pagina).map(this::resposta));
    }

    @Transactional(readOnly = true)
    public PaginaResponseDTO<ServicoResponseDTO> concluidos(UUID unidadeId, Instant de, Instant ate,
            UUID contratanteId, String responsavel, Pageable pagina) {
        return PaginaResponseDTO.de(servicos.concluidosNoPeriodo(unidadeId, de, ate, contratanteId,
                responsavel == null || responsavel.isBlank() ? null : responsavel.strip(), pagina).map(this::resposta));
    }

    @Transactional(readOnly = true)
    public ServicoResponseDTO consultar(UUID unidadeId, UUID id) { return resposta(obter(unidadeId, id)); }

    @Transactional
    public ServicoResponseDTO criar(UUID unidadeId, ServicoRequestDTO dto) {
        var unidade = unidades.findById(unidadeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var servico = new Servico(unidade, cliente(unidadeId, dto.contratanteId()), orcamento(unidadeId, dto.orcamentoId()),
                dto.titulo(), dto.categoria(), dto.descricao(), dto.responsavel(), dto.equipe(), dto.localExecucao());
        return resposta(servicos.save(servico));
    }

    @Transactional
    public ServicoResponseDTO atualizar(UUID unidadeId, UUID id, ServicoRequestDTO dto) {
        var servico = obter(unidadeId, id);
        if (servico.getStatus() == StatusServico.CONCLUIDO || servico.getStatus() == StatusServico.CANCELADO)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serviço encerrado não pode ser alterado");
        servico.atualizar(cliente(unidadeId, dto.contratanteId()), orcamento(unidadeId, dto.orcamentoId()),
                dto.titulo(), dto.categoria(), dto.descricao(), dto.responsavel(), dto.equipe(), dto.localExecucao());
        return resposta(servico);
    }

    @Transactional
    public ServicoResponseDTO agendar(UUID unidadeId, UUID id, AgendamentoRequestDTO dto) {
        var servico = obter(unidadeId, id);
        if (servico.getStatus() == StatusServico.CONCLUIDO || servico.getStatus() == StatusServico.CANCELADO)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serviço encerrado não pode ser agendado");
        if (!dto.inicioPrevisto().isBefore(dto.fimPrevisto()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O término deve ser posterior ao início");
        if (servicos.contarConflitos(unidadeId, id, dto.responsavel().strip(), dto.inicioPrevisto(), dto.fimPrevisto()) > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Responsável já possui serviço nesse horário");
        servico.agendar(dto.inicioPrevisto(), dto.fimPrevisto(), dto.responsavel().strip());
        return resposta(servico);
    }

    @Transactional
    public ServicoResponseDTO iniciar(UUID unidadeId, UUID id) {
        var servico = obter(unidadeId, id);
        if (servico.getStatus() != StatusServico.AGENDADO && servico.getStatus() != StatusServico.AGUARDANDO)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente serviço agendado ou aguardando pode iniciar");
        servico.alterarStatus(StatusServico.EM_ANDAMENTO, null);
        return resposta(servico);
    }

    @Transactional
    public ServicoResponseDTO concluir(UUID unidadeId, UUID id, ConclusaoServicoRequestDTO dto) {
        var servico = obter(unidadeId, id);
        if (servico.getStatus() != StatusServico.EM_ANDAMENTO && servico.getStatus() != StatusServico.AGUARDANDO)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente serviço em execução pode ser concluído");
        servico.alterarStatus(StatusServico.CONCLUIDO, dto.resumoConclusao().strip());
        return resposta(servico);
    }

    @Transactional
    public ServicoResponseDTO cancelar(UUID unidadeId, UUID id) {
        var servico = obter(unidadeId, id);
        if (servico.getStatus() == StatusServico.CONCLUIDO)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serviço concluído não pode ser cancelado");
        servico.alterarStatus(StatusServico.CANCELADO, null);
        return resposta(servico);
    }

    public Servico obter(UUID unidadeId, UUID id) {
        return servicos.findByIdAndUnidade_Id(id, unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
    }

    private Contratante cliente(UUID unidadeId, UUID id) {
        return contratantes.findByIdAndUnidadeId(id, unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente não pertence à unidade"));
    }

    private Orcamento orcamento(UUID unidadeId, UUID id) {
        if (id == null) return null;
        return orcamentos.findByIdAndUnidadeId(id, unidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento não pertence à unidade"));
    }

    private ServicoResponseDTO resposta(Servico s) {
        return new ServicoResponseDTO(s.getId(), s.getCodigo(), s.getContratante().getId(),
                s.getContratante().getNomeFantasia() == null ? s.getContratante().getNomeRazaoSocial() : s.getContratante().getNomeFantasia(),
                s.getOrcamento() == null ? null : s.getOrcamento().getId(), s.getTitulo(), s.getCategoria(),
                s.getDescricao(), s.getResponsavel(), s.getEquipe(), s.getLocalExecucao(), s.getInicioPrevisto(),
                s.getFimPrevisto(), s.getConcluidoEm(), s.getResumoConclusao(), s.getStatus());
    }
}
