package my_app.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.CellType.STRING;

public class PlanilhaFornecedorReader {
    private static final int COL_NOME = 0;   // A
    private static final int COL_CUSTO = 1;  // B
    private static final int COL_QTD = 3;    // D
    private static final int COL_PORC = 4;   // E
    private static final int COL_VENDA = 5;  // F

    public List<ItemFornecedor> ler(Path arquivoXlsx) throws IOException {
        List<ItemFornecedor> itens = new ArrayList<>();
        String fornecedorAtual = null;

        try (InputStream is = Files.newInputStream(arquivoXlsx);
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                Cell nomeCell = row.getCell(COL_NOME);
                Cell custoCell = row.getCell(COL_CUSTO);

                String nome = nomeCell != null ? formatter.formatCellValue(nomeCell).trim() : "";
                String custoTexto = custoCell != null ? formatter.formatCellValue(custoCell).trim() : "";

                if (nome.isBlank()) {
                    continue; // linha em branco entre blocos
                }

                if ("CUSTO".equalsIgnoreCase(custoTexto)) {
                    // linha de cabeçalho do bloco: A = nome do fornecedor, B/D/E/F = rótulos fixos
                    fornecedorAtual = nome;
                    continue;
                }

                boolean temCusto = custoCell != null && custoCell.getCellType() == CellType.NUMERIC;
                if (!temCusto) {
                    continue; // linha inesperada, ignora
                }

                BigDecimal custo = lerNumero(custoCell);
                BigDecimal quantidadeBd = lerNumero(row.getCell(COL_QTD));
                BigDecimal venda = lerNumero(row.getCell(COL_VENDA));

                itens.add(new ItemFornecedor(
                        fornecedorAtual,
                        nome,
                        custo.setScale(2, RoundingMode.HALF_UP),
                        quantidadeBd != null ? quantidadeBd.intValue() : 0,
                        venda != null ? venda.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO
                ));
            }
        }
        return itens;
    }

    private BigDecimal lerNumero(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> {
                String v = cell.getStringCellValue().trim()
                        .replace("R$", "")
                        .replace(",", ".")
                        .trim();
                yield v.isBlank() ? null : new BigDecimal(v);
            }
            default -> null;
        };
    }


    public record ItemFornecedor(
            String fornecedor,
            String produto,
            BigDecimal custo,
            int quantidade,
            BigDecimal venda
    ) {}
}
