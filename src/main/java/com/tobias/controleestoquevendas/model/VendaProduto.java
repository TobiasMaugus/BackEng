package com.tobias.controleestoquevendas.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "venda_produto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaProduto {

    // Chave Composta: Usa a classe VendaProdutoId definida acima
    @EmbeddedId
    private VendaProdutoId id;

    // Relacionamento Many-to-One: Mapeia o campo 'venda_id'
    // A anotação @MapsId indica que a FK é parte da PK composta
    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("vendaId")
    @JoinColumn(name = "venda_id")
    @EqualsAndHashCode.Exclude
    @JsonBackReference
    private Venda venda;

    // Relacionamento Many-to-One: Mapeia o campo 'produto_id'
    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("produtoId")
    @JoinColumn(name = "produto_id")
    @EqualsAndHashCode.Exclude
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade; // Mapeia o campo 'quantidade'

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario; // Mapeia o campo 'preco_unitario'
}