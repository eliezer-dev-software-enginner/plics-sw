package my_app.services.exports;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableExportEngineTest {

    @TempDir
    Path tempDir;

    @Test
    void csvSegueTemplateComBomSeparadorEscapeECabecalho() throws Exception {
        var data = new TableExportEngine.TableData(
                List.of("Nome", "Observação"),
                List.of(List.of("Café", "usa ; e \"aspas\""), List.of("Pão", "linha\nnova"))
        );
        Path destination = tempDir.resolve("tabela.csv");

        new TableExportEngine().writeCsv(destination, data);

        String csv = Files.readString(destination, StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\ufeffNome;Observação\r\n"));
        assertTrue(csv.contains("Café;\"usa ; e \"\"aspas\"\"\"\r\n"));
        assertTrue(csv.contains("Pão;\"linha\nnova\"\r\n"));
    }

    @Test
    void pdfInterpretaLayoutSubstituiVariaveisERepeteCabecalho() throws Exception {
        var rows = java.util.stream.IntStream.rangeClosed(1, 120)
                .mapToObj(index -> List.of(String.valueOf(index), "Descrição " + index))
                .toList();
        var data = new TableExportEngine.TableData(List.of("ID", "Descrição"), rows);
        Path destination = tempDir.resolve("tabela.pdf");

        new TableExportEngine().writePdf(destination, data, Map.of(
                "NOME_EMPRESA", "Mercado São João",
                "DOCUMENTO_EMPRESA", "CNPJ: 00.000.000/0001-00",
                "TELEFONE", "Telefone: (11) 99999-0000",
                "ENDERECO", "Rua Central - São Paulo",
                "TITULO_TABELA", "Cadastro de produtos"
        ));

        try (PDDocument document = PDDocument.load(destination.toFile())) {
            String text = new PDFTextStripper().getText(document);
            assertTrue(document.getNumberOfPages() > 1);
            assertTrue(text.contains("Mercado São João"));
            assertTrue(text.contains("Cadastro de produtos"));
            assertTrue(text.contains("Descrição 120"));
        }
    }

    @Test
    void placeholdersDesconhecidosSaoSubstituidosPorVazio() {
        String result = new TableExportEngine().resolve(
                "${NOME_EMPRESA} - ${NAO_EXISTE}", Map.of("NOME_EMPRESA", "Plics"));

        assertEquals("Plics - ", result);
    }
}
