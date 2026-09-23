package br.com.gestaoservicos.orcamento.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pdf_orcamento")
public class PdfOrcamento {
    @Id private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "revisao_orcamento_id", unique = true)
    private RevisaoOrcamento revisao;
    @Column(name = "nome_arquivo", nullable = false) private String nomeArquivo;
    @Column(nullable = false) private byte[] conteudo;
    @Column(name = "gerado_em", nullable = false) private Instant geradoEm;
    protected PdfOrcamento() { }
    public PdfOrcamento(RevisaoOrcamento revisao, String nomeArquivo, byte[] conteudo) {
        this.id = UUID.randomUUID(); this.revisao = revisao; this.nomeArquivo = nomeArquivo;
        this.conteudo = conteudo.clone(); this.geradoEm = Instant.now();
    }
    public String getNomeArquivo() { return nomeArquivo; }
    public byte[] getConteudo() { return conteudo.clone(); }
}
