package com.tobias.controleestoquevendas.service;

import com.tobias.controleestoquevendas.dto.ItemVendaRequestDTO;
import com.tobias.controleestoquevendas.dto.VendaRequestDTO;
import com.tobias.controleestoquevendas.exception.EstoqueInsuficienteException;
import com.tobias.controleestoquevendas.exception.ResourceNotFoundException;
import com.tobias.controleestoquevendas.model.*;
import com.tobias.controleestoquevendas.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private VendaProdutoRepository vendaProdutoRepository; // Necessário para exclusão de itens

    // ==============================================
    // 1. C - CREATE (Cria uma nova Venda)
    // Já estava implementado, mas revisado para clareza
    // ==============================================
    @Transactional
    public Venda criarVenda(VendaRequestDTO vendaDTO, Long vendedorId) {

        Cliente cliente = clienteRepository.findById(vendaDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + vendaDTO.getClienteId()));

        User vendedor = userRepository.findById(vendedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor não encontrado com ID: " + vendedorId));

        Venda venda = new Venda();
        venda.setCliente(cliente);
        venda.setVendedor(vendedor);

        // O método processarItens é delegado para reutilização (PUT)
        return processarItensDaVenda(venda, vendaDTO.getItens());
    }

    // ==============================================
    // 2. R - READ (Listar Todas)
    // ==============================================
    public List<Venda> listarTodasVendas() {
        return vendaRepository.findAll();
    }

    public Optional<Venda> buscarPorId(Long id) {
        return vendaRepository.findById(id);
    }

    // ==============================================
    // 3. R - READ (Listar por Vendedor)
    // ==============================================
    public List<Venda> listarVendasPorVendedor(Long vendedorId) {
        // Usa o Query Method definido no VendaRepository
        return vendaRepository.findByVendedorId(vendedorId);
    }

    // ==============================================
    // 4. R - READ (Listar por Cliente)
    // ==============================================
    public List<Venda> listarVendasPorCliente(Long clienteId) {
        // Usa o Query Method definido no VendaRepository
        return vendaRepository.findByClienteId(clienteId);
    }

    // ==============================================
    // 5. R - READ (Calcular Valor Total por Vendedor)
    // **NOTA:** Este método idealmente usaria uma query otimizada no Repository.
    // Para simplificar, faremos o cálculo em Java sobre a lista.
    // ==============================================
    public BigDecimal calcularValorTotalVendasPorVendedor(Long vendedorId) {
        List<Venda> vendas = vendaRepository.findByVendedorId(vendedorId);

        return vendas.stream()
                .map(Venda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==============================================
    // 6. U - UPDATE (Atualizar Venda)
    // Lógica complexa: exige ajustar o estoque. Só Gerente pode fazer.
    // ==============================================
    @Transactional
    public Venda atualizarVenda(Long vendaId, VendaRequestDTO vendaDTO, Long vendedorId) {

        Venda vendaExistente = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com ID: " + vendaId));

        Cliente novoCliente = clienteRepository.findById(vendaDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + vendaDTO.getClienteId()));

        User vendedor = userRepository.findById(vendedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor não encontrado com ID: " + vendedorId));

        // 1️⃣ Devolve o estoque dos itens antigos
        for (VendaProduto item : vendaExistente.getItens()) {
            Produto produto = item.getProduto();
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + item.getQuantidade());
            produtoRepository.save(produto);
        }

        // 2️⃣ Limpa a lista de itens **sem substituir a referência**
        vendaExistente.getItens().clear();

        // 3️⃣ Atualiza cliente e vendedor
        vendaExistente.setCliente(novoCliente);
        vendaExistente.setVendedor(vendedor);

        // 4️⃣ Adiciona os novos itens na lista existente
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVendaRequestDTO itemDTO : vendaDTO.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com ID: " + itemDTO.getProdutoId()));

            if (produto.getQuantidadeEstoque() < itemDTO.getQuantidade()) {
                throw new IllegalArgumentException("Estoque insuficiente para o produto: " + produto.getNome());
            }

            // Baixa no estoque
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - itemDTO.getQuantidade());
            produtoRepository.save(produto);

            // Cria item de venda e adiciona à lista existente
            VendaProduto vendaProduto = new VendaProduto();
            vendaProduto.setVenda(vendaExistente);
            vendaProduto.setProduto(produto);
            vendaProduto.setQuantidade(itemDTO.getQuantidade());
            vendaProduto.setPrecoUnitario(produto.getPreco());

            // Cria ID composto
            vendaProduto.setId(new VendaProdutoId(vendaExistente.getId(), produto.getId()));

            vendaExistente.getItens().add(vendaProduto);

            total = total.add(produto.getPreco().multiply(BigDecimal.valueOf(itemDTO.getQuantidade())));
        }

        vendaExistente.setValorTotal(total);

        return vendaRepository.save(vendaExistente);
    }

    // ==============================================
    // 7. D - DELETE (Excluir Venda)
    // ==============================================
    @Transactional
    public void deletarVenda(Long vendaId, boolean devolverEstoque) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com ID: " + vendaId));

        if (devolverEstoque) {
            // Devolve a quantidade ao estoque para cada item da venda
            for (VendaProduto item : venda.getItens()) {
                Produto produto = item.getProduto();
                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + item.getQuantidade());
                produtoRepository.save(produto);
            }
        }

        // Exclui a venda (os itens de VendaProduto serão excluídos em cascata)
        vendaRepository.delete(venda);
    }

    // ==============================================
    // MÉTODO AUXILIAR: Processa Itens e Atualiza Estoque
    // Usado em CREATE e UPDATE
    // ==============================================
    @Transactional
    protected Venda processarItensDaVenda(Venda venda, List<ItemVendaRequestDTO> itensDTO) {
        List<VendaProduto> itensVenda = new ArrayList<>();
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemVendaRequestDTO itemDTO : itensDTO) {

            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com ID: " + itemDTO.getProdutoId()));

            int quantidade = itemDTO.getQuantidade();
            BigDecimal precoUnitario = produto.getPreco();

            // Validação de Estoque
            if (produto.getQuantidadeEstoque() < quantidade) {
                throw new EstoqueInsuficienteException("Estoque insuficiente para o produto: " + produto.getNome());
            }

            // Cria VendaProduto
            VendaProdutoId vpId = new VendaProdutoId(venda.getId(), produto.getId());
            VendaProduto itemVenda = new VendaProduto();
            itemVenda.setId(vpId);
            itemVenda.setVenda(venda);
            itemVenda.setProduto(produto);
            itemVenda.setQuantidade(quantidade);
            itemVenda.setPrecoUnitario(precoUnitario);

            itensVenda.add(itemVenda);

            // Cálculo e Baixa no Estoque
            BigDecimal subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
            valorTotal = valorTotal.add(subtotal);

            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidade);
            produtoRepository.save(produto);
        }

        venda.setItens(itensVenda);
        venda.setValorTotal(valorTotal);

        return vendaRepository.save(venda);
    }
}