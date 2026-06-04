package my_app.db.repositories;

import my_app.db.models.CompraModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComprasRepositoryTest extends BaseRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(ComprasRepositoryTest.class);
    ComprasRepository repository;

    @Override
    protected void initRepository() {
        repository = new ComprasRepository(session);
    }

    @BeforeEach
    void cleanCompras() throws Exception {
        try (var conn = DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM compras");
        }
    }

    private CompraModel novaCompra(String produtoCod, BigDecimal totalLiquido, long dataCriacaoMillis) {
        var model = new CompraModel();
        model.setProdutoCod(produtoCod);
        model.setFornecedorId(1);
        model.setQuantidade(BigDecimal.ONE);
        model.setPrecoDeCompra(BigDecimal.TEN);
        model.setDescontoEmReais(BigDecimal.ZERO);
        model.setTipoPagamento("A VISTA");
        model.setObservacao("Teste");
        model.setDataCompra(dataCriacaoMillis);
        model.setNumeroNota("123456");
        model.setTotalLiquido(totalLiquido);
        model.setDataCriacaoMillis(dataCriacaoMillis);
        return model;
    }

    @Test
    void salvar() throws SQLException {
        CompraModel salvo = repository.salvar(novaCompra("PROD001", BigDecimal.valueOf(100.50), System.currentTimeMillis()));

        log.info("Compra salva com id={}", salvo.getId());

        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals("PROD001", salvo.getProdutoCod());
        assertEquals(0, BigDecimal.valueOf(100.50).compareTo(salvo.getTotalLiquido()));
    }

    @Test
    void listar() throws SQLException {
        repository.salvar(novaCompra("PROD001", BigDecimal.valueOf(50), System.currentTimeMillis()));

        List<CompraModel> lista = repository.listar();

        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(it -> "PROD001".equals(it.getProdutoCod())));
    }

    @Test
    void atualizar() throws SQLException {
        CompraModel salvo = repository.salvar(novaCompra("PROD001", BigDecimal.valueOf(50), System.currentTimeMillis()));

        salvo.setObservacao("Observacao atualizada");
        salvo.setTotalLiquido(BigDecimal.valueOf(75));
        repository.atualizar(salvo);

        CompraModel atualizado = repository.buscarById(salvo.getId());

        assertNotNull(atualizado);
        assertEquals("Observacao atualizada", atualizado.getObservacao());
        assertEquals(0, BigDecimal.valueOf(75).compareTo(atualizado.getTotalLiquido()));
    }

    @Test
    void excluirById() throws SQLException {
        CompraModel salvo = repository.salvar(novaCompra("PROD001", BigDecimal.valueOf(50), System.currentTimeMillis()));

        repository.excluirById(salvo.getId());

        CompraModel deletado = repository.buscarById(salvo.getId());
        assertNull(deletado);
    }

    @Test
    void buscarById() throws SQLException {
        CompraModel salvo = repository.salvar(novaCompra("PROD001", BigDecimal.valueOf(50), System.currentTimeMillis()));

        CompraModel encontrado = repository.buscarById(salvo.getId());

        assertNotNull(encontrado);
        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals("PROD001", encontrado.getProdutoCod());
    }

    @Test
    void somarComprasPorPeriodo() throws SQLException {
        long base = System.currentTimeMillis();
        // compra 1: 1 dia atras (dentro do periodo)
        long t1 = base - 86_400_000L;
        // compra 2: 2 dias atras (dentro do periodo)
        long t2 = base - 172_800_000L;
        // compra 3: 30 dias atras (fora do periodo)
        long t3 = base - 2_592_000_000L;

        repository.salvar(novaCompra("P1", BigDecimal.valueOf(100), t1));
        repository.salvar(novaCompra("P2", BigDecimal.valueOf(200), t2));
        repository.salvar(novaCompra("P3", BigDecimal.valueOf(300), t3));

        long dataInicio = base - 259_200_000L; // 3 dias atras
        long dataFim = base + 86_400_000L;     // 1 dia a frente

        BigDecimal total = repository.somarComprasPorPeriodo(dataInicio, dataFim);

        assertEquals(0, BigDecimal.valueOf(300).compareTo(total),
                "Deveria somar apenas as 2 compras dentro do periodo (100 + 200 = 300)");
    }
}
