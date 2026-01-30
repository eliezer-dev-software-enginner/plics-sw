package my_app.db.repositories;

import my_app.db.dto.VendaDto;
import my_app.db.models.VendaModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VendaRepository extends BaseRepository<VendaDto, VendaModel> {

    public VendaModel salvar(VendaDto dto) throws SQLException {
        String sql = """
                INSERT INTO vendas 
                (produto_id, cliente_id, quantidade, preco_unitario, desconto, 
                 valor_total, forma_pagamento, observacao, data_criacao)
                VALUES (?,?,?,?,?,?,?,?,?)
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, dto.produtoId());
            ps.setLong(2, dto.clienteId());
            ps.setBigDecimal(3, dto.quantidade());
            ps.setBigDecimal(4, dto.precoUnitario());
            ps.setBigDecimal(5, dto.desconto());
            ps.setBigDecimal(6, dto.valorTotal());
            ps.setString(7, dto.formaPagamento());
            ps.setString(8, dto.observacao());
            ps.setLong(9, System.currentTimeMillis());
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
                if (venda.produtoId != null) {
                    ProdutoRepository produtoRepo = new ProdutoRepository();
                    venda.produto = produtoRepo.buscarById(venda.produtoId);
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
                      valor_total = ?, forma_pagamento = ?, observacao = ?
                    WHERE id = ?
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBigDecimal(1, model.quantidade);
            ps.setBigDecimal(2, model.precoUnitario);
            ps.setBigDecimal(3, model.desconto);
            ps.setBigDecimal(4, model.valorTotal);
            ps.setString(5, model.formaPagamento);
            ps.setString(6, model.observacao);
            ps.setLong(7, model.id);
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
        if (venda.produtoId != null) {
            ProdutoRepository produtoRepo = new ProdutoRepository();
            venda.produto = produtoRepo.buscarById(venda.produtoId);
        }

        // Carregar cliente
        if (venda.clienteId != null) {
            ClienteRepository clienteRepo = new ClienteRepository();
            venda.cliente = clienteRepo.buscarById(venda.clienteId);
        }

        return venda;
    }
}