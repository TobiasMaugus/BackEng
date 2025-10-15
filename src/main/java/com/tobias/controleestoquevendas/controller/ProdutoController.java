package com.tobias.controleestoquevendas.controller;

import com.tobias.controleestoquevendas.model.Produto;
import com.tobias.controleestoquevendas.repository.ProdutoRepository;
import com.tobias.controleestoquevendas.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService service;

    @Autowired
    private ProdutoRepository produtoRepository;

    // --- C - Create (POST) ---
    @PostMapping
    public ResponseEntity<?> criarProduto(@RequestBody Produto produto) {
        if (produtoRepository.existsByNome(produto.getNome())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Erro: já existe um produto com o nome '" + produto.getNome() + "'.");
        }

        Produto novo = produtoRepository.save(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    // --- R - Read All (GET) ---
    @GetMapping
    public List<Produto> listarProdutos() {
        return service.listarProdutos();
    }

    // --- R - Read One by ID (GET) ---
    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- R - Read by Category (GET) ---
    // Exemplo: GET /produtos/search?categoria=Eletronicos
    @GetMapping("/search")
    public List<Produto> buscarPorCategoria(@RequestParam String categoria) {
        // Supondo que o ProdutoService tenha um método para buscar por Categoria
        return service.buscarPorCategoria(categoria);
    }

    // --- U - Update (PUT) ---
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarProduto(@PathVariable Long id, @RequestBody Produto produtoAtualizado) {
        return service.buscarPorId(id).map(produtoExistente -> {

            // Verifica se o nome foi alterado para um nome que já existe,
            // exceto se for o nome do próprio produto que está sendo atualizado.
            if (!produtoExistente.getNome().equals(produtoAtualizado.getNome()) &&
                    produtoRepository.existsByNome(produtoAtualizado.getNome())) {

                return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
            }

            // Atualiza os campos
            produtoExistente.setNome(produtoAtualizado.getNome());
            produtoExistente.setCategoria(produtoAtualizado.getCategoria());
            produtoExistente.setPreco(produtoAtualizado.getPreco());
            produtoExistente.setQuantidadeEstoque(produtoAtualizado.getQuantidadeEstoque());

            Produto atualizado = service.atualizarProduto(produtoExistente);
            return ResponseEntity.ok(atualizado);

        }).orElse(ResponseEntity.notFound().build());
    }

    // --- D - Delete (DELETE) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        return service.buscarPorId(id).map(produto -> {
            service.deletarProduto(id);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}