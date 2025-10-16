package com.tobias.controleestoquevendas.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable // Indica que esta classe é incorporável
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaProdutoId implements Serializable {

    // Deve corresponder ao tipo do ID em Venda (Long)
    private Long vendaId;

    // Deve corresponder ao tipo do ID em Produto (Long)
    private Long produtoId;
}