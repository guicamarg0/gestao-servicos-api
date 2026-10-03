package br.com.gestaoservicos.despesa.model;

import br.com.gestaoservicos.compartilhado.auditoria.Auditavel;
import br.com.gestaoservicos.orcamento.model.Orcamento;
import br.com.gestaoservicos.servico.model.Servico;
import br.com.gestaoservicos.unidade.model.Unidade;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "despesa")
public class Despesa extends Auditavel {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "unidade_id") private Unidade unidade;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "servico_id") private Servico servico;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "orcamento_id") private Orcamento orcamento;
    @Column(nullable = false, length = 30) private String codigo;
    @Column(nullable = false, length = 100) private String categoria;
    @Column(nullable = false, length = 500) private String descricao;
    @Column(nullable = false, precision = 15, scale = 2) private BigDecimal valor;
    @Column(name = "data_despesa", nullable = false) private LocalDate dataDespesa;
    @Column(length = 120) private String responsavel;
    @Column(name = "comprovante_url", length = 1000) private String comprovanteUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private StatusDespesa status;
    @Column(name = "comentario_decisao", length = 2000) private String comentarioDecisao;
    @Column(name = "decidido_em") private Instant decididoEm;
    @Column(name = "decidido_por") private UUID decididoPor;
    @Version @Column(name = "versao") private long versao;
    @ElementCollection
    @CollectionTable(name="despesa_item",joinColumns=@JoinColumn(name="despesa_id"))
    @OrderColumn(name="ordem")
    private java.util.List<ItemDespesa> itens = new java.util.ArrayList<>();

    protected Despesa() {}
    public Despesa(Unidade unidade, Servico servico, Orcamento orcamento, String categoria, String descricao,
                   BigDecimal valor, LocalDate dataDespesa, String responsavel, String comprovanteUrl, boolean enviar) {
        id = UUID.randomUUID();
        codigo = "DS-" + id.toString().substring(0, 8).toUpperCase();
        this.unidade = unidade;
        status = enviar ? StatusDespesa.PENDENTE : StatusDespesa.RASCUNHO;
        atualizar(servico, orcamento, categoria, descricao, valor, dataDespesa, responsavel, comprovanteUrl);
    }
    public void atualizar(Servico servico, Orcamento orcamento, String categoria, String descricao,
                          BigDecimal valor, LocalDate dataDespesa, String responsavel, String comprovanteUrl) {
        this.servico = servico;
        this.orcamento = orcamento;
        this.categoria = categoria.strip();
        this.descricao = descricao.strip();
        this.valor = valor;
        this.dataDespesa = dataDespesa;
        this.responsavel = responsavel;
        this.comprovanteUrl = comprovanteUrl;
    }
    public void enviar() { status = StatusDespesa.PENDENTE; comentarioDecisao = null; decididoEm = null; decididoPor = null; }
    public void atualizarItens(java.util.List<ItemDespesa> novos) {
        itens.clear();itens.addAll(novos);
        valor = itens.stream().map(ItemDespesa::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
    }
    public java.util.List<ItemDespesa> getItens() { return itens; }
    public void decidir(StatusDespesa decisao, String comentario, UUID usuarioId) {
        status = decisao;
        comentarioDecisao = comentario;
        decididoEm = Instant.now();
        decididoPor = usuarioId;
    }
    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public Servico getServico() { return servico; }
    public Orcamento getOrcamento() { return orcamento; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getDataDespesa() { return dataDespesa; }
    public String getResponsavel() { return responsavel; }
    public String getComprovanteUrl() { return comprovanteUrl; }
    public StatusDespesa getStatus() { return status; }
    public String getComentarioDecisao() { return comentarioDecisao; }
    public Instant getDecididoEm() { return decididoEm; }
    public UUID getDecididoPor() { return decididoPor; }
}
