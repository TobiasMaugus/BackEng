package com.tobias.controleestoquevendas.service;

import com.tobias.controleestoquevendas.model.Cliente;
import com.tobias.controleestoquevendas.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    // Create
    public Cliente criarCliente(Cliente cliente) {
        return repository.save(cliente);
    }

    // Read All
    public List<Cliente> listarClientes() {
        return repository.findAll();
    }

    // Read One (por ID)
    public Optional<Cliente> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Read One (por nome)
    public List<Cliente> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    // Update
    public Cliente atualizarCliente(Cliente cliente) {
        return repository.save(cliente);
    }

    // Delete
    public void deletarCliente(Long id) {
        repository.deleteById(id);
    }
}
