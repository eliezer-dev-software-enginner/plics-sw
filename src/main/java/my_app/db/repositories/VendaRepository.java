package my_app.db.repositories;

import my_app.db.dto.VendaDto;
import my_app.db.models.VendaModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class VendaRepository extends BaseRepository<VendaDto, VendaModel> {

    public VendaModel salvar(VendaDto dto) throws SQLException {
        String sql = """
                INSERT INTO vendas 
                (produto_cod, cliente_id, quantidade, preco_unitario, desconto, 
                tipo_pagamento, observacao, data_criacao, total_liquido, data_validade)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dto.produtoCod());
            ps.setLong(2, dto.clienteId());
            ps.setBigDecimal(3, dto.quantidade());
            ps.setBigDecimal(4, dto.precoUnitario());
            ps.setBigDecimal(5, dto.desconto());
            ps.setString(6, dto.formaPagamento());
            ps.setString(7, dto.observacao());
            ps.setLong(8, System.currentTimeMillis());
            ps.setBigDecimal(9, dto.totalLiquido());

            if (dto.dataValidade() != null) {
                ps.setLong(10, dto.dataValidade());
            } else {
                ps.setNull(10, java.sql.Types.BIGINT);
            }

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    return new VendaModel().fromIdAndDto(id, dto);
                }
            }
        }
        throw new SQLException("Falha ao recuperar ID gerado");
    }

    public List<VendaModel> listar() throws SQLException {
        var lista = new ArrayList<VendaModel>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM vendas ORDER BY data_criacao DESC");
            while (rs.next()) lista.add(new VendaModel().fromResultSet(rs));
        }
        return lista;
    }

    public List<VendaModel> listarComProdutoECliente() throws SQLException {
        var lista = new ArrayList<VendaModel>();
        String sql = """
                SELECT v.*, p.descricao as produto_descricao, c.nome as cliente_nome
                FROM vendas v
                LEFT JOIN produtos p ON v.produto_id = p.id
                LEFT JOIN clientes c ON v.cliente_id = c.id
                ORDER BY v.data_criacao DESC
                """;

        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                VendaModel venda = new VendaModel().fromResultSet(rs);
                
                // Carregar produto
                if (venda.produtoCod != null) {
                    ProdutoRepository produtoRepo = new ProdutoRepository();
                    venda.produto = produtoRepo.buscarPorCodigoBarras(venda.produtoCod);
                }
                
                // Carregar cliente
                if (venda.clienteId != null) {
                    ClienteRepository clienteRepo = new ClienteRepository();
                    venda.cliente = clienteRepo.buscarById(venda.clienteId);
                }
                
                lista.add(venda);
            }
        }
        return lista;
    }

    public List<VendaModel> listarPorCliente(Long clienteId) throws SQLException {
        var lista = new ArrayList<VendaModel>();
        String sql = "SELECT * FROM vendas WHERE cliente_id = ? ORDER BY data_criacao DESC";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, clienteId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(new VendaModel().fromResultSet(rs));
        }
        return lista;
    }

    public List<VendaModel> listarPorProduto(Long produtoId) throws SQLException {
        var lista = new ArrayList<VendaModel>();
        String sql = "SELECT * FROM vendas WHERE produto_id = ? ORDER BY data_criacao DESC";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, produtoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(new VendaModel().fromResultSet(rs));
        }
        return lista;
    }

    public void atualizar(VendaModel model) throws SQLException {
        String sql = """
                    UPDATE vendas SET
                      quantidade = ?, preco_unitario = ?, desconto = ?,
                      tipo_pagamento = ?, observacao = ?, total_liquido = ?
                    WHERE id = ?
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBigDecimal(1, model.quantidade);
            ps.setBigDecimal(2, model.precoUnitario);
            ps.setBigDecimal(3, model.desconto);
            ps.setString(4, model.tipoPagamento);
            ps.setString(5, model.observacao);
            ps.setLong(6, model.id);
            ps.setBigDecimal(7, model.totalLiquido);
            ps.executeUpdate();
        }
    }

    public void excluirById(Long id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM vendas WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    protected VendaModel buscarById(Long id) throws SQLException {
        String sql = "SELECT * FROM vendas WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? new VendaModel().fromResultSet(rs) : null;
        }
    }

    public VendaModel buscarPorIdComProdutoECliente(Long id) throws SQLException {
        VendaModel venda = buscarById(id);
        if (venda == null) return null;

        // Carregar produto
        if (venda.produtoCod != null) {
            ProdutoRepository produtoRepo = new ProdutoRepository();
            venda.produto = produtoRepo.buscarPorCodigoBarras(venda.produtoCod);
        }

        // Carregar cliente
        if (venda.clienteId != null) {
            ClienteRepository clienteRepo = new ClienteRepository();
            venda.cliente = clienteRepo.buscarById(venda.clienteId);
        }

        return venda;
    }
    
    public BigDecimal somarVendasPorPeriodo(Long dataInicio, Long dataFim) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(total_liquido), 0) as total 
            FROM vendas 
            WHERE data_criacao BETWEEN ? AND ?
            AND tipo_pagamento != 'A PRAZO'
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, dataInicio);
            ps.setLong(2, dataFim);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }
        }
        return BigDecimal.ZERO;
    }
}