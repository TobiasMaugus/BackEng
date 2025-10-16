package com.tobias.controleestoquevendas.controller;

import com.tobias.controleestoquevendas.dto.VendaRequestDTO;
import com.tobias.controleestoquevendas.exception.ResourceNotFoundException;
import com.tobias.controleestoquevendas.model.User;
import com.tobias.controleestoquevendas.model.Venda;
import com.tobias.controleestoquevendas.repository.UserRepository;
import com.tobias.controleestoquevendas.security.CustomUserDetails;
import com.tobias.controleestoquevendas.service.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    UserRepository userRepository;
    // --- C - Create (POST) ---
    @PostMapping
    public ResponseEntity<Venda> criarVenda(@RequestBody VendaRequestDTO vendaDTO) {
        try {
            // Pega o usuário logado a partir do contexto
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Busca o ID no banco
            User user = userRepository.findByUsername(username).orElseThrow();
            Long vendedorId = user.getId();

            Venda novaVenda = vendaService.criarVenda(vendaDTO, vendedorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaVenda);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ---------------------------------------------------------------------
    // 2. LER SOMENTE VENDAS DO VENDEDOR LOGADO
    // ---------------------------------------------------------------------
    @GetMapping("/meus")
    public List<Venda> listarMinhasVendas() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        return vendaService.listarVendasPorVendedor(user.getId());
    }


    // ---------------------------------------------------------------------
    // 3. LER VENDAS DE UM CLIENTE ESPECÍFICO
    // ---------------------------------------------------------------------
    @GetMapping("/cliente/{clienteId}")
    public List<Venda> listarVendasPorCliente(@PathVariable Long clienteId) {
        return vendaService.listarVendasPorCliente(clienteId);
    }

    // ---------------------------------------------------------------------
    // 4. VALOR TOTAL DAS VENDAS DO VENDEDOR LOGADO
    // ---------------------------------------------------------------------
    @GetMapping("/total/meu")
    public ResponseEntity<BigDecimal> valorTotalMinhasVendas() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        BigDecimal total = vendaService.calcularValorTotalVendasPorVendedor(user.getId());
        return ResponseEntity.ok(total);
    }


    // ---------------------------------------------------------------------
    // 5. ATUALIZAR UMA VENDA
    // ---------------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<Venda> atualizarVenda(
            @PathVariable Long id,
            @RequestBody VendaRequestDTO vendaDTO
    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userRepository.findByUsername(username).orElseThrow();

            Venda vendaAtualizada = vendaService.atualizarVenda(id, vendaDTO, user.getId());
            return ResponseEntity.ok(vendaAtualizada);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    // ---------------------------------------------------------------------
    // 6. EXCLUIR UMA VENDA (com opção de devolver estoque)
    // ---------------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarVenda(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean devolverEstoque
    ) {
        try {
            vendaService.deletarVenda(id, devolverEstoque);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}