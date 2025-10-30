package com.tobias.controleestoquevendas.controller;

import com.tobias.controleestoquevendas.model.Cliente;
import com.tobias.controleestoquevendas.repository.ClienteRepository;
import com.tobias.controleestoquevendas.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService service;
    @Autowired
    private ClienteRepository clienteRepository;

    private Map<String, String> formatarErros(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage(),
                        (existing, replacement) -> existing // Mantém a primeira mensagem se houver duplicidade
                ));
    }

    // Create
    @PostMapping
    public ResponseEntity<?> criarCliente(@Valid @RequestBody Cliente cliente, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(formatarErros(bindingResult));
        }

        if (clienteRepository.existsByCpf(cliente.getCpf())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Erro: já existe um cliente com esse CPF");
        }

        Cliente novo = clienteRepository.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
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
    public ResponseEntity<?> atualizarCliente(@PathVariable Long id, @RequestBody @Valid Cliente clienteAtualizado, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(formatarErros(bindingResult));
        }
        return service.buscarPorId(id).map(cliente -> {
            cliente.setNome(clienteAtualizado.getNome());
            cliente.setCpf(clienteAtualizado.getCpf());
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
