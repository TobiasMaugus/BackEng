package com.tobias.controleestoquevendas.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class ItemVendaRequestDTO {

    @NotNull(message = "O ID do produto é obrigatório.")
    private Long produtoId;

    @Min(value = 1, message = "A quantidade deve ser de no mínimo 1.")
    private Integer quantidade;
}