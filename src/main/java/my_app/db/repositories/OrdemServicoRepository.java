package my_app.db.repositories;

import my_app.db.dto.OrdemServicoDto;
import my_app.db.models.OrdemServicoModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrdemServicoRepository extends BaseRepository<OrdemServicoDto, OrdemServicoModel> {

    // CREATE
    public OrdemServicoModel salvar(OrdemServicoDto dto) throws SQLException {
        long numeroOS = gerarProximoNumeroOS();
        
        String sql = """
        INSERT INTO ordens_de_servico 
        (cliente_id, tecnico_id, numero_os, equipamento, mao_de_obra_valor, pecas_valor, 
         tipo_pagamento, status, checklist_relatorio, data_escolhida, total_liquido, data_criacao) 
         VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, dto.clienteId());
            ps.setLong(2, dto.tecnicoId());
            ps.setLong(3, numeroOS);
            ps.setString(4, dto.equipamento());
            ps.setBigDecimal(5, dto.mao_de_obra_valor());
            ps.setBigDecimal(6, dto.pecas_valor());
            ps.setString(7, dto.tipoPagamento());
            ps.setString(8, dto.status());
            ps.setString(9, dto.checklist_relatorio());
            ps.setLong(10, dto.data_escolhida());
            ps.setBigDecimal(11, dto.totalLiquido());
            ps.setLong(12, System.currentTimeMillis());
            ps.executeUpdate();
            
            // Recupera o ID gerado e cria nova instância
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long idGerado = generatedKeys.getLong(1);
                    // Criar novo DTO com o número OS gerado
                    OrdemServicoDto dtoComNumero = new OrdemServicoDto(
                        dto.clienteId(), dto.tecnicoId(), dto.equipamento(),
                        dto.mao_de_obra_valor(), dto.pecas_valor(), dto.checklist_relatorio(),
                        dto.data_escolhida(), dto.tipoPagamento(), dto.status(),
                        dto.totalLiquido(), numeroOS
                    );
                    return new OrdemServicoModel().fromIdAndDtoAndMillis(idGerado, dtoComNumero, System.currentTimeMillis());
                }
            }
        }
        throw new SQLException("Falha ao recuperar ID gerado");
    }

    private long gerarProximoNumeroOS() throws SQLException {
        String sql = "SELECT COALESCE(MAX(numero_os), 0) as max_numero FROM ordens_de_servico";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                long maxNumero = rs.getLong("max_numero");
                return Math.max(1001, maxNumero + 1); // Começa com 1001 e é incremental
            }
        }
        return 1001; // Se não houver registros, começa com 1001
    }

    public List<OrdemServicoModel> listar() throws SQLException {
        List<OrdemServicoModel> lista = new ArrayList<>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM ordens_de_servico");
            while (rs.next()) lista.add((OrdemServicoModel) new OrdemServicoModel().fromResultSet(rs));
        }
        return lista;
    }

    // UPDATE
    public void atualizar(OrdemServicoModel model) throws SQLException {
        String sql = """
        UPDATE ordens_de_servico SET cliente_id = ?, tecnico_id = ?, equipamento = ?,
        mao_de_obra_valor = ?, pecas_valor = ?, tipo_pagamento = ?, status = ?,
        checklist_relatorio = ?, data_escolhida = ?, total_liquido = ?, numero_os = ?
        WHERE id = ?
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, model.clienteId);
            ps.setLong(2, model.tecnicoId);
            ps.setString(3, model.equipamento);
            ps.setBigDecimal(4, model.maoDeObraValor);
            ps.setBigDecimal(5, model.pecas_valor);
            ps.setString(6, model.tipoPagamento);
            ps.setString(7, model.status);
            ps.setString(8, model.checklistRelatorio);
            ps.setLong(9, model.dataEscolhida);
            ps.setBigDecimal(10, model.totalLiquido);
            ps.setLong(11, model.numeroOs);
            ps.setLong(12, model.id);

            ps.executeUpdate();
        }
    }

    public void excluirById(Long id) throws SQLException {
        try (PreparedStatement ps =
                     conn().prepareStatement("DELETE FROM ordens_de_servico WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    protected OrdemServicoModel buscarById(Long id) throws SQLException {
        String sql = "SELECT * FROM ordens_de_servico WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? (OrdemServicoModel) new OrdemServicoModel().fromResultSet(rs) : null;
        }
    }
}