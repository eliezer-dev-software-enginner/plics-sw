package my_app.db.services;

import net.sf.persism.Session;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class BaseServiceTest {

    protected static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual), "Expected " + expected + " (scale " + expected.scale() + ") but got " + actual + " (scale " + (actual == null ? "N/A" : actual.scale()) + ")");
    }

    protected Session session;

    protected abstract void initService();

    protected Connection rawConnection;

    @BeforeEach
    void setUp() throws Exception {
        String testUrl = "jdbc:sqlite:file:testdb?mode=memory&cache=shared";
        rawConnection = DriverManager.getConnection(testUrl);
        rawConnection.createStatement().execute("PRAGMA foreign_keys = OFF");
        Flyway.configure()
                .dataSource(testUrl, "", "")
                .locations("classpath:flyway_migrations")
                .load()
                .migrate();
        limparDadosPadrao();
        session = new Session(rawConnection);
        initService();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (session != null) session.close();
        if (rawConnection != null && !rawConnection.isClosed()) rawConnection.close();
    }

    private void limparDadosPadrao() throws Exception {
        var stmt = rawConnection.createStatement();
        stmt.execute("DELETE FROM contas_pagar");
        stmt.execute("DELETE FROM contas_a_receber");
        stmt.execute("DELETE FROM vendas");
        stmt.execute("DELETE FROM compras");
        stmt.execute("DELETE FROM pedido_itens");
        stmt.execute("DELETE FROM pedidos");
        stmt.execute("DELETE FROM ordens_de_servico");
        stmt.execute("DELETE FROM preferencias");
        stmt.execute("DELETE FROM empresas");
        stmt.execute("DELETE FROM fornecedores");
        stmt.execute("DELETE FROM categorias");
        stmt.execute("DELETE FROM clientes");
        stmt.execute("DELETE FROM tecnicos");
        stmt.execute("DELETE FROM usuarios");
    }

}
