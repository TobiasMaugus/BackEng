package com.tobias.controleestoquevendas.repository;

import com.tobias.controleestoquevendas.model.VendaProduto;
import com.tobias.controleestoquevendas.model.VendaProdutoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendaProdutoRepository extends JpaRepository<VendaProduto, VendaProdutoId> {
}