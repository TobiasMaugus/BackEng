package com.tobias.controleestoquevendas.repository;

import com.tobias.controleestoquevendas.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByNomeContainingIgnoreCase(String nome);
    List<Produto> findByCategoria(String categoria);
    boolean existsByNome(String nome);
}
