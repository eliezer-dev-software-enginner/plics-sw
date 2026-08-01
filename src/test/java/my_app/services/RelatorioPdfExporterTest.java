package my_app.services;

import my_app.db.models.EmpresaModel;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RelatorioPdfExporterTest {

    private final RelatorioPdfExporter exporter = new RelatorioPdfExporter();

    private EmpresaModel empresaValida() {
        var empresa = new EmpresaModel();
        empresa.setNome("Loja Teste Ltda");
        empresa.setCpfCnpj("11222333000181");
        return empresa;
    }

    private RelatorioDados dadosValidos() {
        return new RelatorioDados(
                BigDecimal.valueOf(100), BigDecimal.valueOf(10), BigDecimal.valueOf(30),
                BigDecimal.valueOf(50), BigDecimal.valueOf(20),
                BigDecimal.valueOf(140), BigDecimal.valueOf(70), BigDecimal.valueOf(70),
                BigDecimal.valueOf(15), BigDecimal.valueOf(5),
                java.util.List.of(),
                java.util.List.of()
        );
    }

    @Test
    void deveGerarPdfValidoComOsValoresDoRelatorio(@TempDir Path tempDir) throws Exception {
        File destino = tempDir.resolve("relatorio.pdf").toFile();

        exporter.gerar(destino, empresaValida(), LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), dadosValidos());

        assertTrue(destino.exists());
        assertTrue(destino.length() > 0);

        String texto;
        try (PDDocument doc = PDDocument.load(destino)) {
            texto = new PDFTextStripper().getText(doc);
        }

        assertTrue(texto.contains("Loja Teste Ltda"), "deveria conter o nome da empresa");
        assertTrue(texto.contains("Relatório Financeiro"), "deveria conter o título");
        assertTrue(texto.contains("01/07/2026"), "deveria conter a data de início formatada");
        assertTrue(texto.contains("31/07/2026"), "deveria conter a data de fim formatada");
        assertTrue(texto.contains("R$ 140,00"), "deveria conter o total de receitas");
        assertTrue(texto.contains("R$ 70,00"), "deveria conter o total de despesas/lucro (aparecem duas vezes)");
    }

    @Test
    void deveGerarPdfMesmoSemEmpresaCadastrada(@TempDir Path tempDir) throws Exception {
        File destino = tempDir.resolve("relatorio-sem-empresa.pdf").toFile();

        exporter.gerar(destino, null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), dadosValidos());

        assertTrue(destino.exists());
        String texto;
        try (PDDocument doc = PDDocument.load(destino)) {
            texto = new PDFTextStripper().getText(doc);
        }
        assertTrue(texto.contains("Plics SW"), "sem empresa cadastrada, usa 'Plics SW' como fallback");
    }

    @Test
    void deveIncluirProdutosMaisVendidosNoPdf(@TempDir Path tempDir) throws Exception {
        File destino = tempDir.resolve("relatorio-produtos.pdf").toFile();

        var dados = new RelatorioDados(
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(100),
                BigDecimal.ZERO, BigDecimal.ZERO,
                java.util.List.of(
                        new ProdutoMaisVendido("111", "Camiseta", "UN", BigDecimal.valueOf(12)),
                        new ProdutoMaisVendido("222", "Calça", "UN", BigDecimal.valueOf(8))
                ),
                java.util.List.of()
        );

        exporter.gerar(destino, null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), dados);

        String texto;
        try (PDDocument doc = PDDocument.load(destino)) {
            texto = new PDFTextStripper().getText(doc);
        }
        assertTrue(texto.contains("PRODUTOS MAIS VENDIDOS DO PERÍODO"), "deveria conter a seção de mais vendidos");
        assertTrue(texto.contains("1. Camiseta"), "deveria conter o 1º produto mais vendido");
        assertTrue(texto.contains("12 UN"), "deveria conter a quantidade e unidade do 1º produto");
        assertTrue(texto.contains("2. Calça"), "deveria conter o 2º produto mais vendido");
    }
}
