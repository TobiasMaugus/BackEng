package com.tobias.controleestoquevendas.service;

import com.tobias.controleestoquevendas.model.Produto;
import com.tobias.controleestoquevendas.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository repository;

    // Create
    public Produto criarProduto(Produto produto) {
        return repository.save(produto);
    }

    // Read All
    public List<Produto> listarProdutos() {
        return repository.findAll();
    }

    // Read One (por ID)
    public Optional<Produto> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Read One (por nome)
    public List<Produto> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Produto> buscarPorCategoria(String categoria) {
        return repository.findByCategoria(categoria);
    }

    // Update
    public Produto atualizarProduto(Produto produto) {
        return repository.save(produto);
    }

    // Delete
    public void deletarProduto(Long id) {
        repository.deleteById(id);
    }
}
