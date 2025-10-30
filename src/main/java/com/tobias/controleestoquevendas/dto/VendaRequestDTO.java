package com.tobias.controleestoquevendas.dto;

import jakarta.validation.Valid;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class VendaRequestDTO {

    @NotNull(message = "O ID do cliente é obrigatório.")
    private Long clienteId;

    @Valid
    @NotEmpty(message = "A venda deve conter pelo menos um item.")
    private List<ItemVendaRequestDTO> itens;
}