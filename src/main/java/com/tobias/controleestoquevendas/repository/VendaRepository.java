package com.tobias.controleestoquevendas.repository;

import com.tobias.controleestoquevendas.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    // Exemplo de método de consulta customizado
    // Encontra todas as vendas feitas por um vendedor específico (usando o ID do User)
    List<Venda> findByVendedorId(Long vendedorId);

    // Outro exemplo: Encontra todas as vendas de um cliente específico
    List<Venda> findByClienteId(Long clienteId);

    List<Venda> findByDataVendaBetween(LocalDateTime dataInicial, LocalDateTime dataFinal);
}