package com.tobias.controleestoquevendas.dto;

import com.tobias.controleestoquevendas.model.Venda;
import com.tobias.controleestoquevendas.model.VendaProduto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
public class VendaResponseDTO {

    private Long id;
    private LocalDateTime dataVenda;
    private BigDecimal valorTotal;

    // CAMPOS ADICIONADOS
    private Long vendedorId;
    private String vendedorNome;
    private Long clienteId;
    private String clienteNome;
    // FIM CAMPOS ADICIONADOS

    // Você pode criar um DTO separado para os itens de venda se quiser simplificar,
    // mas por enquanto, manteremos o VendaProduto simplificado se não tiver loops.
    private List<VendaProduto> itens;

    // Construtor, Getters e Setters

    public VendaResponseDTO(Venda venda) {
        this.id = venda.getId();
        this.dataVenda = venda.getDataVenda();
        this.valorTotal = venda.getValorTotal();
        this.itens = venda.getItens();

        // Carrega dados do Vendedor (User)
        if (venda.getVendedor() != null) {
            this.vendedorId = venda.getVendedor().getId();
            this.vendedorNome = venda.getVendedor().getUsername(); // Supondo que User tem um método getNome()
        }

        // Carrega dados do Cliente
        if (venda.getCliente() != null) {
            this.clienteId = venda.getCliente().getId();
            this.clienteNome = venda.getCliente().getNome();
        }
    }

    // ... Getters and Setters (Gerados pelo IntelliJ ou Lombok)
}