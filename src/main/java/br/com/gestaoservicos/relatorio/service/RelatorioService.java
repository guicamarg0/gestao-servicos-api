package br.com.gestaoservicos.relatorio.service;

import br.com.gestaoservicos.contrato.model.StatusContrato;
import br.com.gestaoservicos.contrato.repository.ContratoRepository;
import br.com.gestaoservicos.despesa.model.StatusDespesa;
import br.com.gestaoservicos.despesa.repository.DespesaRepository;
import br.com.gestaoservicos.orcamento.model.StatusOrcamento;
import br.com.gestaoservicos.orcamento.repository.RevisaoOrcamentoRepository;
import br.com.gestaoservicos.relatorio.dto.RelatorioResumoResponseDTO;
import br.com.gestaoservicos.servico.model.StatusServico;
import br.com.gestaoservicos.servico.repository.ServicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.UUID;

@Service
public class RelatorioService {
    private final ServicoRepository servicos;
    private final DespesaRepository despesas;
    private final RevisaoOrcamentoRepository revisoes;
    private final ContratoRepository contratos;
    public RelatorioService(ServicoRepository servicos, DespesaRepository despesas,
            RevisaoOrcamentoRepository revisoes, ContratoRepository contratos) {
        this.servicos = servicos; this.despesas = despesas; this.revisoes = revisoes; this.contratos = contratos;
    }
    @Transactional(readOnly = true)
    public RelatorioResumoResponseDTO resumo(UUID unidadeId, LocalDate de, LocalDate ate, String fuso) {
        if (de.isAfter(ate)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Período inválido");
        if (de.plusYears(2).isBefore(ate)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Período máximo de dois anos");
        ZoneId zona = zona(fuso);
        Instant inicio = de.atStartOfDay(zona).toInstant();
        Instant fim = ate.plusDays(1).atStartOfDay(zona).toInstant();
        return new RelatorioResumoResponseDTO(de, ate,
                servicos.contarPorStatusNoPeriodo(unidadeId, StatusServico.CONCLUIDO, inicio, fim),
                servicos.contarPorStatusNoPeriodo(unidadeId, StatusServico.EM_ANDAMENTO, inicio, fim),
                servicos.contarPorStatusNoPeriodo(unidadeId, StatusServico.AGENDADO, inicio, fim),
                revisoes.contarAtuaisPorStatusNoPeriodo(unidadeId, StatusOrcamento.EMITIDO, inicio, fim),
                revisoes.contarAtuaisPorStatusNoPeriodo(unidadeId, StatusOrcamento.APROVADO, inicio, fim),
                revisoes.somarAtuaisPorStatusNoPeriodo(unidadeId, StatusOrcamento.APROVADO, inicio, fim),
                despesas.somarPorStatusNoPeriodo(unidadeId, StatusDespesa.APROVADA, de, ate),
                contratos.countByUnidade_IdAndStatus(unidadeId, StatusContrato.ATIVO));
    }
    public ZoneId zona(String fuso) {
        try { return ZoneId.of(fuso); }
        catch (DateTimeException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fuso horário inválido"); }
    }
    public byte[] csv(RelatorioResumoResponseDTO r) {
        String conteudo = "Indicador;Valor\r\n" +
                "Serviços concluídos;" + r.servicosConcluidos() + "\r\n" +
                "Serviços em andamento;" + r.servicosEmAndamento() + "\r\n" +
                "Serviços agendados;" + r.servicosAgendados() + "\r\n" +
                "Orçamentos emitidos;" + r.orcamentosEmitidos() + "\r\n" +
                "Orçamentos aprovados;" + r.orcamentosAprovados() + "\r\n" +
                "Receita aprovada;" + r.receitaAprovada() + "\r\n" +
                "Despesas aprovadas;" + r.despesasAprovadas() + "\r\n" +
                "Contratos ativos;" + r.contratosAtivos() + "\r\n";
        return ("\uFEFF" + conteudo).getBytes(StandardCharsets.UTF_8);
    }
}
