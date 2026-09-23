package br.com.gestaoservicos.configuracaounidade.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "forma_recebimento")
public class FormaRecebimento {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "configuracao_unidade_id", nullable = false) private ConfiguracaoUnidade configuracao;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TipoFormaRecebimento tipo;
    @Column(name = "nome_exibicao", nullable = false, length = 120) private String nomeExibicao;
    @Column(nullable = false, length = 1000) private String instrucoes;
    @Column(nullable = false) private boolean ativa;
    @Column(nullable = false) private boolean padrao;
    protected FormaRecebimento() {}
    public FormaRecebimento(ConfiguracaoUnidade configuracao, TipoFormaRecebimento tipo, String nomeExibicao, String instrucoes, boolean ativa, boolean padrao) {
        this.id = UUID.randomUUID(); this.configuracao = configuracao; atualizar(tipo, nomeExibicao, instrucoes, ativa, padrao);
    }
    public void atualizar(TipoFormaRecebimento tipo, String nomeExibicao, String instrucoes, boolean ativa, boolean padrao) { this.tipo = tipo; this.nomeExibicao = nomeExibicao; this.instrucoes = instrucoes; this.ativa = ativa; this.padrao = padrao; }
    public void removerPadrao() { this.padrao = false; }
    public UUID getId() { return id; } public TipoFormaRecebimento getTipo() { return tipo; } public String getNomeExibicao() { return nomeExibicao; } public String getInstrucoes() { return instrucoes; } public boolean isAtiva() { return ativa; } public boolean isPadrao() { return padrao; }
}
