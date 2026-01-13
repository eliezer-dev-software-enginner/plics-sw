package my_app.db.repositories;

import my_app.db.DB;
import my_app.db.DBInitializer;
import my_app.db.models.EmpresaModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaRepositoryTest {
    private EmpresaRepository repo;

    @BeforeEach
    void setup() throws Exception {
        DB.reset();
        DB.getInstance("jdbc:sqlite::memory:");
        DBInitializer.init();
        repo = new EmpresaRepository();
        
        // Limpa empresas antes de cada teste
        try (var ps = DB.getInstance().connection().prepareStatement("DELETE FROM empresas")) {
            ps.executeUpdate();
        }
    }

    @Test
    void atualizar_whenUpdateLogoMarca_shouldUpdateSuccessfully() throws SQLException {
        // Given - criar uma empresa de teste
        EmpresaModel empresa = createTestEmpresa();
        
        // When - atualizar apenas a logomarca
        String newLogoPath = "file:///C:/test/new_logo.png";
        empresa.logoMarca = newLogoPath;
        repo.atualizar(empresa);
        
        // Then - verificar se a logomarca foi atualizada
        EmpresaModel updated = repo.listar().getFirst();
        assertEquals(newLogoPath, updated.logoMarca);
    }

    @Test
    void atualizar_whenUpdateMultipleFields_shouldUpdateSuccessfully() throws SQLException {
        // Given
        EmpresaModel empresa = createTestEmpresa();
        
        // When - atualizar múltiplos campos
        empresa.nome = "Updated Company";
        empresa.telefone = "11999999999";
        empresa.logoMarca = "file:///C:/test/logo_updated.png";
        empresa.cep = "12345-678";
        repo.atualizar(empresa);
        
        // Then
        EmpresaModel updated = repo.listar().getFirst();
        assertEquals("Updated Company", updated.nome);
        assertEquals("11999999999", updated.telefone);
        assertEquals("file:///C:/test/logo_updated.png", updated.logoMarca);
        assertEquals("12345-678", updated.cep);
    }

    private EmpresaModel createTestEmpresa() throws SQLException {
        EmpresaModel empresa = new EmpresaModel();
        empresa.id = 1L;
        empresa.nome = "Test Company";
        empresa.cpfCnpj = "12345678901";
        empresa.telefone = "11987654321";
        empresa.cep = "01234-567";
        empresa.cidade = "São Paulo";
        empresa.rua = "Rua Teste";
        empresa.bairro = "Bairro Teste";
        empresa.localPagamento = "Local Pagamento";
        empresa.textoResponsabilidade = "Responsabilidade";
        empresa.termoServico = "Termo Serviço";
        empresa.logoMarca = "/logo_256x256.png";
        empresa.dataCriacao = System.currentTimeMillis();
        
        // Inserir no banco para ter um registro para atualizar
        try (var ps = DB.getInstance().connection().prepareStatement(
            "INSERT INTO empresas (id, nome, cpfCnpj, celular, endereco_cep, endereco_cidade, " +
            "endereco_rua, endereco_bairro, local_pagamento, texto_responsabilidade, " +
            "texto_termo_de_servico, logomarca, data_criacao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            
            ps.setLong(1, empresa.id);
            ps.setString(2, empresa.nome);
            ps.setString(3, empresa.cpfCnpj);
            ps.setString(4, empresa.telefone);
            ps.setString(5, empresa.cep);
            ps.setString(6, empresa.cidade);
            ps.setString(7, empresa.rua);
            ps.setString(8, empresa.bairro);
            ps.setString(9, empresa.localPagamento);
            ps.setString(10, empresa.textoResponsabilidade);
            ps.setString(11, empresa.termoServico);
            ps.setString(12, empresa.logoMarca);
            ps.setLong(13, empresa.dataCriacao);
            
            ps.executeUpdate();
        }
        
        return empresa;
    }
}