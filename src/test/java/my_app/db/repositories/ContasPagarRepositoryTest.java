package my_app.db.repositories;

import my_app.db.DB;
import my_app.db.DBInitializer;
import my_app.db.dto.ContasPagarDto;
import my_app.db.models.ContasPagarModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContasPagarRepositoryTest {
    private ContasPagarRepository repo;

    @BeforeEach
    void setup() throws Exception {
        DB.reset();
        DB.getInstance("jdbc:sqlite::memory:");
        DBInitializer.init();
        repo = new ContasPagarRepository();
    }

    @Test
    void salvar() throws SQLException {
        ContasPagarDto dto = contasPagarDtoFake();
        ContasPagarModel salvo = repo.salvar(dto);

        ContasPagarModel encontrado = repo.buscarById(salvo.id);

        assertNotNull(encontrado);
        assertEquals("Conta Teste", encontrado.descricao);
        assertEquals(BigDecimal.valueOf(1000.50), encontrado.valorOriginal);
        assertEquals("PENDENTE", encontrado.status);
        assertNotNull(salvo.id);
        assertNotNull(salvo.dataCriacao);
    }

    @Test
    void listar() throws SQLException {
        var dto1 = contasPagarDtoFake();
        var dto2 = contasPagarDtoFake();
        dto2 = new ContasPagarDto(
            "Outra Conta", 
            BigDecimal.valueOf(500.0), 
            BigDecimal.ZERO, 
            BigDecimal.valueOf(500.0),
            System.currentTimeMillis() + 86400000L, 
            null, 
            "PENDENTE", 
            2L, 
            null, 
            "DOC/002", 
            "BOLETO", 
            "Observação teste"
        );

        repo.salvar(dto1);
        repo.salvar(dto2);

        List<ContasPagarModel> lista = repo.listar();

        assertEquals(2, lista.size());
        assertTrue(lista.stream().anyMatch(p -> p.descricao.equals("Conta Teste")));
        assertTrue(lista.stream().anyMatch(p -> p.descricao.equals("Outra Conta")));
    }

    @Test
    void atualizar() throws SQLException {
        ContasPagarDto dto = contasPagarDtoFake();
        ContasPagarModel salvo = repo.salvar(dto);

        salvo.descricao = "Conta Atualizada";
        salvo.status = "PAGO";
        salvo.valorPago = BigDecimal.valueOf(1000.50);
        salvo.valorRestante = BigDecimal.ZERO;
        salvo.dataPagamento = System.currentTimeMillis();

        repo.atualizar(salvo);

        ContasPagarModel atualizado = repo.buscarById(salvo.id);
        assertEquals("Conta Atualizada", atualizado.descricao);
        assertEquals("PAGO", atualizado.status);
        assertEquals(BigDecimal.valueOf(1000.50), atualizado.valorPago);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), atualizado.valorRestante.stripTrailingZeros());
    }

    @Test
    void excluir() throws SQLException {
        ContasPagarDto dto = contasPagarDtoFake();
        ContasPagarModel salvo = repo.salvar(dto);

        repo.excluirById(salvo.id);

        assertNull(repo.buscarById(salvo.id));
    }

    @Test
    void buscarPorFornecedor() throws SQLException {
        ContasPagarDto dto1 = contasPagarDtoFake();
        ContasPagarDto dto2 = new ContasPagarDto(
            "Conta Fornecedor 2", 
            BigDecimal.valueOf(200.0), 
            BigDecimal.ZERO, 
            BigDecimal.valueOf(200.0),
            System.currentTimeMillis() + 86400000L, 
            null, 
            "PENDENTE", 
            5L, // fornecedor_id diferente
            null, 
            "DOC/003", 
            "CHEQUE", 
            null
        );

        repo.salvar(dto1); // fornecedorId = 1L
        repo.salvar(dto2); // fornecedorId = 5L

        List<ContasPagarModel> contasFornecedor1 = repo.buscarPorFornecedor(1L);
        List<ContasPagarModel> contasFornecedor5 = repo.buscarPorFornecedor(5L);

        assertEquals(1, contasFornecedor1.size());
        assertEquals(1, contasFornecedor5.size());
        assertEquals("Conta Teste", contasFornecedor1.get(0).descricao);
        assertEquals("Conta Fornecedor 2", contasFornecedor5.get(0).descricao);
    }

    @Test
    void buscarPorStatus() throws SQLException {
        ContasPagarDto dto1 = contasPagarDtoFake();
        ContasPagarDto dto2 = new ContasPagarDto(
            "Conta Paga", 
            BigDecimal.valueOf(300.0), 
            BigDecimal.valueOf(300.0), 
            BigDecimal.ZERO,
            System.currentTimeMillis() - 86400000L, // vencida
            System.currentTimeMillis(), // paga hoje
            "PAGO", 
            1L, 
            null, 
            "DOC/004", 
            "BOLETO", 
            null
        );

        repo.salvar(dto1); // PENDENTE
        repo.salvar(dto2); // PAGO

        List<ContasPagarModel> pendentes = repo.buscarPorStatus("PENDENTE");
        List<ContasPagarModel> pagas = repo.buscarPorStatus("PAGO");

        assertEquals(1, pendentes.size());
        assertEquals(1, pagas.size());
        assertEquals("Conta Teste", pendentes.get(0).descricao);
        assertEquals("Conta Paga", pagas.get(0).descricao);
    }

    @Test
    void buscarVencidas() throws SQLException {
        long agora = System.currentTimeMillis();
        long ontem = agora - 86400000L; // 1 dia atrás
        long amanha = agora + 86400000L; // 1 dia à frente

        ContasPagarDto dtoVencida = new ContasPagarDto(
            "Conta Vencida", 
            BigDecimal.valueOf(100.0), 
            BigDecimal.ZERO, 
            BigDecimal.valueOf(100.0),
            ontem, // vencida
            null, 
            "PENDENTE", 
            1L, 
            null, 
            "DOC/005", 
            "BOLETO", 
            null
        );

        ContasPagarDto dtoNaoVencida = new ContasPagarDto(
            "Conta Não Vencida", 
            BigDecimal.valueOf(200.0), 
            BigDecimal.ZERO, 
            BigDecimal.valueOf(200.0),
            amanha, // não vencida
            null, 
            "PENDENTE", 
            1L, 
            null, 
            "DOC/006", 
            "BOLETO", 
            null
        );

        repo.salvar(dtoVencida);
        repo.salvar(dtoNaoVencida);

        List<ContasPagarModel> vencidas = repo.buscarVencidas();

        assertEquals(1, vencidas.size());
        assertEquals("Conta Vencida", vencidas.get(0).descricao);
    }

    @Test
    void registrarPagamento() throws SQLException {
        ContasPagarDto dto = contasPagarDtoFake();
        ContasPagarModel salvo = repo.salvar(dto);

        BigDecimal valorPago = BigDecimal.valueOf(500.00);
        repo.registrarPagamento(salvo.id, valorPago);

        ContasPagarModel atualizada = repo.buscarById(salvo.id);
        assertEquals(valorPago, atualizada.valorPago);
        assertEquals(BigDecimal.valueOf(500.50).stripTrailingZeros(), atualizada.valorRestante.stripTrailingZeros()); // 1000.50 - 500.00
        assertEquals("PARCIAL", atualizada.status);
        assertNotNull(atualizada.dataPagamento);
    }

    @Test
    void registrarPagamentoCompleto() throws SQLException {
        ContasPagarDto dto = contasPagarDtoFake();
        ContasPagarModel salvo = repo.salvar(dto);

        // Pagar valor total
        repo.registrarPagamento(salvo.id, salvo.valorOriginal);

        ContasPagarModel atualizada = repo.buscarById(salvo.id);
        assertEquals(salvo.valorOriginal, atualizada.valorPago);
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), atualizada.valorRestante.stripTrailingZeros());
        assertEquals("PAGO", atualizada.status);
        assertNotNull(atualizada.dataPagamento);
    }

    private ContasPagarDto contasPagarDtoFake() {
        return new ContasPagarDto(
            "Conta Teste", 
            BigDecimal.valueOf(1000.50), 
            BigDecimal.ZERO, 
            BigDecimal.valueOf(1000.50),
            System.currentTimeMillis() + 86400000L, // vence amanhã
            null, 
            "PENDENTE", 
            1L, 
            null, 
            "DOC/001", 
            "DUPLICATA", 
            "Observação de teste"
        );
    }
}