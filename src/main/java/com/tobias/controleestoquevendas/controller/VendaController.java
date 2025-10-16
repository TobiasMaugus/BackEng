package com.tobias.controleestoquevendas.controller;

import com.tobias.controleestoquevendas.dto.VendaRequestDTO;
import com.tobias.controleestoquevendas.dto.VendaResponseDTO;
import com.tobias.controleestoquevendas.exception.EstoqueInsuficienteException;
import com.tobias.controleestoquevendas.exception.ResourceNotFoundException;
import com.tobias.controleestoquevendas.model.User;
import com.tobias.controleestoquevendas.model.Venda;
import com.tobias.controleestoquevendas.repository.UserRepository;
import com.tobias.controleestoquevendas.service.VendaService;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    UserRepository userRepository;
    // --- C - Create (POST) ---
    @PostMapping
    public ResponseEntity<?> criarVenda(@RequestBody VendaRequestDTO vendaDTO) {
        try {
            // Pega o usuário logado a partir do contexto
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Busca o ID no banco
            User user = userRepository.findByUsername(username).orElseThrow();
            Long vendedorId = user.getId();

            Venda novaVenda = vendaService.criarVenda(vendaDTO, vendedorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaVenda);
        } catch (EstoqueInsuficienteException e) {
            // 2. Captura a exceção específica de estoque
            // Retorna 400 Bad Request com uma mensagem de erro detalhada em JSON
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping // Rota base: /vendas?page=0&size=10
    public Page<VendaResponseDTO> listarTodasVendasPaginado(
            // Define o Pageable: page=0 (página inicial), size=10 (10 itens por página), sort=dataVenda,desc
            @PageableDefault(page = 0, size = 10, sort = "dataVenda", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return vendaService.listarTodasVendasPaginado((org.springframework.data.domain.Pageable) pageable);
    }

    @GetMapping("/periodo")
    public List<VendaResponseDTO> listarVendasPorPeriodo(
            @RequestParam("dataInicial")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // Ex: 2025-01-01T00:00:00
            LocalDateTime dataInicial,

            @RequestParam("dataFinal")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataFinal) {

        return vendaService.listarVendasPorPeriodo(dataInicial, dataFinal);
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

    @GetMapping("/{id}")
    public ResponseEntity<VendaResponseDTO> buscarVendaPorId(@PathVariable Long id) {
        try {
            VendaResponseDTO vendaDTO = vendaService.buscarVendaPorId(id);
            return ResponseEntity.ok(vendaDTO);

        } catch (ResourceNotFoundException e) {
            // Se a venda não for encontrada, retorna 404 Not Found
            return ResponseEntity.notFound().build();
        }
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
    public ResponseEntity<?> atualizarVenda(
            @PathVariable Long id,
            @RequestBody VendaRequestDTO vendaDTO
    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            // Assumindo que o UserRepository está injetado e User model tem o ID
            User user = userRepository.findByUsername(username).orElseThrow();

            Venda vendaAtualizada = vendaService.atualizarVenda(id, vendaDTO, user.getId());
            return ResponseEntity.ok(vendaAtualizada);

        } catch (ResourceNotFoundException e) {
            // Trata Venda, Cliente ou Produto não encontrado (404)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));

        } catch (EstoqueInsuficienteException e) {
            // Trata erro de estoque (400)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            // ✅ CORREÇÃO: Captura a exceção desconhecida e retorna o corpo JSON
            // Retorna a mensagem da exceção, que revelará o erro real
            e.printStackTrace(); // Mantenha isso para debug no console
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Falha na atualização da venda: " + e.getMessage()));
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