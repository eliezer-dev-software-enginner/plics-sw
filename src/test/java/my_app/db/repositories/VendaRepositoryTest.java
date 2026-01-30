package my_app.db.repositories;

import my_app.db.DB;
import my_app.db.DBInitializer;
import my_app.db.dto.VendaDto;
import my_app.db.models.VendaModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class VendaRepositoryTest {
    private VendaRepository repo;
    private ProdutoRepository produtoRepo;
    private ClienteRepository clienteRepo;

    @BeforeEach
    void setup() throws Exception {
        DB.reset();
        DB.getInstance("jdbc:sqlite::memory:");
        DBInitializer.init();
        repo = new VendaRepository();
        produtoRepo = new ProdutoRepository();
        clienteRepo = new ClienteRepository();
    }

    @Test
    void salvar() throws SQLException {
        // Given
        var produto = criarProdutoFake();
        var produtoSalvo = produtoRepo.salvar(produto);
        
        var cliente = criarClienteFake();
        var clienteSalvo = clienteRepo.salvar(cliente);
        
        var dto = vendaDtoFake(produtoSalvo.id, clienteSalvo.id);
        
        // When
        var salvo = repo.salvar(dto);
        
        // Then
        var encontrado = repo.buscarById(salvo.id);
        
        assertNotNull(encontrado);
        assertEquals(produtoSalvo.id, encontrado.produtoId);
        assertEquals(clienteSalvo.id, encontrado.clienteId);
        assertEquals(0, dto.quantidade().compareTo(encontrado.quantidade));
        assertEquals(0, dto.precoUnitario().compareTo(encontrado.precoUnitario));
        assertEquals(0, dto.valorTotal().compareTo(encontrado.valorTotal));
        assertNotNull(salvo.id);
        assertNotNull(salvo.dataCriacao);
    }

    @Test
    void listar() throws SQLException {
        // Given
        var produto = criarProdutoFake();
        var produtoSalvo = produtoRepo.salvar(produto);
        
        var cliente = criarClienteFake();
        var clienteSalvo = clienteRepo.salvar(cliente);
        
        var dto1 = vendaDtoFake(produtoSalvo.id, clienteSalvo.id);
        var dto2 = vendaDtoFake(produtoSalvo.id, clienteSalvo.id);
        
        repo.salvar(dto1);
        repo.salvar(dto2);
        
        // When
        var lista = repo.listar();
        
        // Then
        assertEquals(2, lista.size());
    }

    @Test
    void atualizar() throws SQLException {
        // Given
        var produto = criarProdutoFake();
        var produtoSalvo = produtoRepo.salvar(produto);
        
        var cliente = criarClienteFake();
        var clienteSalvo = clienteRepo.salvar(cliente);
        
        var dto = vendaDtoFake(produtoSalvo.id, clienteSalvo.id);
        var salvo = repo.salvar(dto);
        
        // When
        salvo.quantidade = new BigDecimal("15");
        salvo.valorTotal = new BigDecimal("149.85");
        repo.atualizar(salvo);
        
        // Then
        var atualizado = repo.buscarById(salvo.id);
        assertEquals(0, new BigDecimal("15").compareTo(atualizado.quantidade));
        assertEquals(0, new BigDecimal("149.85").compareTo(atualizado.valorTotal));
    }

    @Test
    void excluir() throws SQLException {
        // Given
        var produto = criarProdutoFake();
        var produtoSalvo = produtoRepo.salvar(produto);
        
        var cliente = criarClienteFake();
        var clienteSalvo = clienteRepo.salvar(cliente);
        
        var dto = vendaDtoFake(produtoSalvo.id, clienteSalvo.id);
        var salvo = repo.salvar(dto);
        
        // When
        repo.excluirById(salvo.id);
        
        // Then
        assertNull(repo.buscarById(salvo.id));
    }

    @Test
    void listarPorCliente() throws SQLException {
        // Given
        var produto = criarProdutoFake();
        var produtoSalvo = produtoRepo.salvar(produto);
        
        var cliente1 = criarClienteFake();
        var cliente1Salvo = clienteRepo.salvar(cliente1);
        
        var cliente2 = new my_app.db.dto.ClienteDto("Cliente Teste 2", "12345678902", "11987654321", "cliente2@email.com");
        var cliente2Salvo = clienteRepo.salvar(cliente2);
        
        var dto1 = vendaDtoFake(produtoSalvo.id, cliente1Salvo.id);
        var dto2 = vendaDtoFake(produtoSalvo.id, cliente1Salvo.id);
        var dto3 = vendaDtoFake(produtoSalvo.id, cliente2Salvo.id);
        
        repo.salvar(dto1);
        repo.salvar(dto2);
        repo.salvar(dto3);
        
        // When
        var listaCliente1 = repo.listarPorCliente(cliente1Salvo.id);
        var listaCliente2 = repo.listarPorCliente(cliente2Salvo.id);
        
        // Then
        assertEquals(2, listaCliente1.size());
        assertEquals(1, listaCliente2.size());
    }

    @Test
    void listarPorProduto() throws SQLException {
        // Given
        var produto1 = criarProdutoFake();
        var produto1Salvo = produtoRepo.salvar(produto1);
        
        var produto2 = new my_app.db.dto.ProdutoDto();
        produto2.codigoBarras = "7891234567891";
        produto2.descricao = "Produto Teste 2";
        produto2.precoCompra = new BigDecimal("8.00");
        produto2.precoVenda = new BigDecimal("12.00");
        produto2.categoriaId = 1L;
        produto2.fornecedorId = 1L;
        produto2.estoque = new BigDecimal("50");
        
        var produto2Salvo = produtoRepo.salvar(produto2);
        
        var cliente = criarClienteFake();
        var clienteSalvo = clienteRepo.salvar(cliente);
        
        var dto1 = vendaDtoFake(produto1Salvo.id, clienteSalvo.id);
        var dto2 = vendaDtoFake(produto1Salvo.id, clienteSalvo.id);
        var dto3 = vendaDtoFake(produto2Salvo.id, clienteSalvo.id);
        
        repo.salvar(dto1);
        repo.salvar(dto2);
        repo.salvar(dto3);
        
        // When
        var listaProduto1 = repo.listarPorProduto(produto1Salvo.id);
        var listaProduto2 = repo.listarPorProduto(produto2Salvo.id);
        
        // Then
        assertEquals(2, listaProduto1.size());
        assertEquals(1, listaProduto2.size());
    }

    private VendaDto vendaDtoFake(Long produtoId, Long clienteId) {
        return new VendaDto(
            produtoId,
            clienteId,
            new BigDecimal("10"),
            new BigDecimal("9.99"),
            BigDecimal.ZERO,
            new BigDecimal("99.90"),
            "Dinheiro",
            "Observação teste"
        );
    }

    private my_app.db.dto.ProdutoDto criarProdutoFake() {
        var dto = new my_app.db.dto.ProdutoDto();
        dto.codigoBarras = "7891234567890";
        dto.descricao = "Produto Teste";
        dto.precoCompra = new BigDecimal("5.00");
        dto.precoVenda = new BigDecimal("9.99");
        dto.categoriaId = 1L;
        dto.fornecedorId = 1L;
        dto.estoque = new BigDecimal("100");
        return dto;
    }

    private my_app.db.dto.ClienteDto criarClienteFake() {
        return new my_app.db.dto.ClienteDto(
            "Cliente Teste",
            "12345678901",
            "11912345678",
            "cliente@email.com"
        );
    }
}