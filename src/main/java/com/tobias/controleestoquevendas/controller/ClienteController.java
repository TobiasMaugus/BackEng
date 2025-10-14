package com.tobias.controleestoquevendas.controller;

import com.tobias.controleestoquevendas.model.Cliente;
import com.tobias.controleestoquevendas.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService service;

    // Create
    @PostMapping
    public Cliente criarCliente(@RequestBody Cliente cliente) {
        return service.criarCliente(cliente);
    }

    // Read All
    @GetMapping
    public List<Cliente> listarClientes() {
        return service.listarClientes();
    }

    // Read One (por ID)
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Read One (por nome)
    @GetMapping("/search")
    public List<Cliente> buscarPorNome(@RequestParam String nome) {
        return service.buscarPorNome(nome);
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizarCliente(@PathVariable Long id, @RequestBody Cliente clienteAtualizado) {
        return service.buscarPorId(id).map(cliente -> {
            cliente.setNome(clienteAtualizado.getNome());
            cliente.setCpfCnpj(clienteAtualizado.getCpfCnpj());
            cliente.setTelefone(clienteAtualizado.getTelefone());
            Cliente atualizado = service.atualizarCliente(cliente);
            return ResponseEntity.ok(atualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCliente(@PathVariable Long id) {
        return service.buscarPorId(id).map(cliente -> {
            service.deletarCliente(id);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
