package com.tobias.controleestoquevendas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vendas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Mapeia o campo 'id'

    // Relacionamento Many-to-One: Várias Vendas para um Cliente
    // Mapeia o campo 'cliente_id'
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnore
    private Cliente cliente;

    // Relacionamento Many-to-One: Várias Vendas para um Vendedor (User)
    // Mapeia o campo 'vendedor_id'
    // Você deve ter uma classe 'User' ou 'Usuario' para o vendedor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    @JsonIgnore
    private User vendedor;

    @Column(name = "data_venda", updatable = false)
    private LocalDateTime dataVenda = LocalDateTime.now(); // Mapeia o campo 'data_venda'

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO; // Mapeia o campo 'valor_total'

    // Relacionamento One-to-Many para a tabela de ligação (VendaProduto)
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<VendaProduto> itens;
}