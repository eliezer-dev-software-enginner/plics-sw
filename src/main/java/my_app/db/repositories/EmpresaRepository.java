package my_app.db.repositories;

import my_app.db.dto.CategoriaDto;
import my_app.db.dto.EmpresaDto;
import my_app.db.models.CategoriaModel;
import my_app.db.models.EmpresaModel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EmpresaRepository extends BaseRepository<EmpresaDto, EmpresaModel> {
    @Override
    protected EmpresaModel salvar(EmpresaDto dto) throws SQLException {
       return null;
    }

    @Override
    public List<EmpresaModel> listar() throws SQLException {
        List<EmpresaModel> lista = new ArrayList<>();
        try (Statement st = conn().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM empresas");
            while (rs.next()) lista.add(EmpresaModel.fromResultSet(rs));
        }
        return lista;
    }


    @Override
    public void atualizar(EmpresaModel empresaModel) throws SQLException {
        String sql = """
        UPDATE empresas SET
            nome = ?,
            cpfCnpj = ?,
            celular = ?,
            endereco_cep = ?,
            endereco_cidade = ?,
            endereco_rua = ?,
            endereco_bairro = ?,
            local_pagamento = ?,
            texto_responsabilidade = ?,
            texto_termo_de_servico = ?,
            data_criacao = ?
        WHERE id = ?
    """;

        long dataCriacao = System.currentTimeMillis();

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, empresaModel.nome);
            ps.setString(2, empresaModel.cpfCnpj);
            ps.setString(3, empresaModel.telefone);
            ps.setString(4, empresaModel.cep);
            ps.setString(5, empresaModel.cidade);
            ps.setString(6, empresaModel.rua);
            ps.setString(7, empresaModel.bairro);
            ps.setString(8, empresaModel.localPagamento);
            ps.setString(9, empresaModel.textoResponsabilidade);
            ps.setString(10, empresaModel.termoServico);
            ps.setLong(11, dataCriacao);
            ps.setLong(12, empresaModel.id);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Nenhuma empresa foi atualizada");
            }
        }
    }


    @Override
    protected void excluirById(Long id) throws SQLException {

    }

    @Override
    protected EmpresaModel buscarById(Long id) throws SQLException {
        return null;
    }
}

