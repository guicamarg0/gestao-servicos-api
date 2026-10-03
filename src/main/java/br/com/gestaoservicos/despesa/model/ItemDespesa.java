package br.com.gestaoservicos.despesa.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Embeddable
public class ItemDespesa {
    @Column(nullable=false,length=500) private String descricao;
    @Column(nullable=false,precision=15,scale=4) private BigDecimal quantidade;
    @Column(name="valor_unitario",nullable=false,precision=15,scale=2) private BigDecimal valorUnitario;
    protected ItemDespesa() {}
    public ItemDespesa(String descricao,BigDecimal quantidade,BigDecimal valorUnitario) {
        this.descricao=descricao.strip();this.quantidade=quantidade;this.valorUnitario=valorUnitario;
    }
    public String getDescricao() { return descricao; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public BigDecimal getTotal() { return quantidade.multiply(valorUnitario).setScale(2,java.math.RoundingMode.HALF_UP); }
}
