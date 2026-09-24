package my_app.services.exports;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Interpreta templates JSONC e renderiza dados tabulares sem conhecer JavaFX ou models da aplicação. */
public final class TableExportEngine {

    public static final String DEFAULT_CSV_TEMPLATE = "/export-templates/table.csv.jsonc";
    public static final String DEFAULT_PDF_TEMPLATE = "/export-templates/table.pdf.jsonc";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([A-Z0-9_]+)}");

    private final ObjectMapper mapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .build();

    public record TableData(List<String> headers, List<List<String>> rows) {
        public TableData {
            var immutableHeaders = List.copyOf(headers);
            var immutableRows = rows.stream().map(List::copyOf).toList();
            if (immutableRows.stream().anyMatch(row -> row.size() != immutableHeaders.size())) {
                throw new IllegalArgumentException("Todas as linhas devem ter a mesma quantidade de colunas do cabeçalho");
            }
            headers = immutableHeaders;
            rows = immutableRows;
        }
    }

    public record CsvTemplate(String charset, String delimiter, boolean bom, boolean includeHeader,
                              boolean alwaysQuote, String lineSeparator) {
    }

    public record PdfTemplate(PageDefinition page, FontDefinition fonts, List<ElementDefinition> content,
                              TableDefinition table) {
    }

    public record PageDefinition(String size, String orientation, float margin) {
    }

    public record FontDefinition(String regular, String bold) {
    }

    public record ElementDefinition(String type, String align, String value, float fontSize,
                                    boolean bold, float spacingAfter, int lines) {
    }

    public record TableDefinition(float fontSize, float headerFontSize, float rowHeight,
                                  float cellPadding, boolean repeatPreamble,
                                  List<String> excludedColumns) {
    }

    public void writeCsv(Path destination, TableData data) throws IOException {
        CsvTemplate template = load(DEFAULT_CSV_TEMPLATE, CsvTemplate.class);
        Charset charset = Charset.forName(template.charset());
        try (var writer = Files.newBufferedWriter(destination, charset)) {
            if (template.bom()) writer.write('\ufeff');
            if (template.includeHeader()) writeCsvRow(writer, data.headers(), template);
            for (var row : data.rows()) writeCsvRow(writer, row, template);
        }
    }

    public void writePdf(Path destination, TableData data, Map<String, String> variables) throws IOException {
        PdfTemplate template = load(DEFAULT_PDF_TEMPLATE, PdfTemplate.class);
        validate(template);
        try (var document = new PDDocument();
             var regularStream = resource(template.fonts().regular());
             var boldStream = resource(template.fonts().bold())) {
            PDFont regular = PDType0Font.load(document, regularStream);
            PDFont bold = PDType0Font.load(document, boldStream);
            new PdfRenderer(document, template, regular, bold, variables).render(data);
            document.save(destination.toFile());
        }
    }

    public String resolve(String text, Map<String, String> variables) {
        if (text == null) return "";
        Matcher matcher = PLACEHOLDER.matcher(text);
        var result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(variables.getOrDefault(matcher.group(1), "")));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private <T> T load(String resourcePath, Class<T> type) throws IOException {
        try (var stream = resource(resourcePath)) {
            return mapper.readValue(stream, type);
        }
    }

    private InputStream resource(String path) throws IOException {
        InputStream stream = TableExportEngine.class.getResourceAsStream(path);
        if (stream == null) throw new IOException("Recurso de exportação não encontrado: " + path);
        return stream;
    }

    private void writeCsvRow(java.io.Writer writer, List<String> values, CsvTemplate template) throws IOException {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) writer.write(template.delimiter());
            writer.write(csvValue(Objects.toString(values.get(index), ""), template));
        }
        writer.write(unescape(template.lineSeparator()));
    }

    private String csvValue(String value, CsvTemplate template) {
        boolean quote = template.alwaysQuote()
                || value.contains(template.delimiter()) || value.contains("\"")
                || value.contains("\r") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return quote ? "\"" + escaped + "\"" : escaped;
    }

    private String unescape(String value) {
        return value.replace("\\r", "\r").replace("\\n", "\n").replace("\\t", "\t");
    }

    private void validate(PdfTemplate template) {
        Objects.requireNonNull(template.page(), "page");
        Objects.requireNonNull(template.fonts(), "fonts");
        Objects.requireNonNull(template.content(), "content");
        Objects.requireNonNull(template.table(), "table");
        long tables = template.content().stream().filter(it -> "TABLE".equalsIgnoreCase(it.type())).count();
        if (tables != 1) throw new IllegalArgumentException("O template PDF deve conter exatamente um comando TABLE");
    }

    private final class PdfRenderer {
        private final PDDocument document;
        private final PdfTemplate template;
        private final PDFont regular;
        private final PDFont bold;
        private final Map<String, String> variables;
        private final PDRectangle pageSize;
        private PDPage page;
        private PDPageContentStream content;
        private float y;

        private PdfRenderer(PDDocument document, PdfTemplate template, PDFont regular, PDFont bold,
                            Map<String, String> variables) {
            this.document = document;
            this.template = template;
            this.regular = regular;
            this.bold = bold;
            this.variables = Map.copyOf(variables);
            this.pageSize = pageSize(template.page());
        }

        private void render(TableData source) throws IOException {
            int tableIndex = java.util.stream.IntStream.range(0, template.content().size())
                    .filter(index -> "TABLE".equalsIgnoreCase(template.content().get(index).type()))
                    .findFirst().orElseThrow();
            var preamble = template.content().subList(0, tableIndex);
            var epilogue = template.content().subList(tableIndex + 1, template.content().size());
            try {
                newPage();
                renderElements(preamble);
                renderTable(filterColumns(source), preamble);
                renderElements(epilogue);
            } finally {
                closeContent();
            }
        }

        private void renderElements(List<ElementDefinition> elements) throws IOException {
            for (var element : elements) {
                switch (element.type().toUpperCase()) {
                    case "TEXT" -> drawText(element);
                    case "BREAK" -> y -= Math.max(1, element.lines()) * 12f;
                    case "HORIZONTAL_LINE" -> drawHorizontalLine(element.spacingAfter());
                    default -> throw new IllegalArgumentException("Comando PDF desconhecido: " + element.type());
                }
            }
        }

        private void drawText(ElementDefinition element) throws IOException {
            PDFont font = element.bold() ? bold : regular;
            float size = element.fontSize() > 0 ? element.fontSize() : 10f;
            String value = resolve(element.value(), variables).replaceAll("[\\r\\n]+", " ");
            float textWidth = font.getStringWidth(value) / 1000f * size;
            float left = template.page().margin();
            float available = page.getMediaBox().getWidth() - (left * 2);
            float x = switch (Objects.toString(element.align(), "LEFT").toUpperCase()) {
                case "CENTER" -> left + Math.max(0, (available - textWidth) / 2);
                case "RIGHT" -> left + Math.max(0, available - textWidth);
                default -> left;
            };
            content.beginText();
            content.setFont(font, size);
            content.newLineAtOffset(x, y);
            content.showText(value);
            content.endText();
            y -= size + Math.max(0, element.spacingAfter());
        }

        private void drawHorizontalLine(float spacingAfter) throws IOException {
            float margin = template.page().margin();
            content.moveTo(margin, y);
            content.lineTo(page.getMediaBox().getWidth() - margin, y);
            content.setLineWidth(.6f);
            content.stroke();
            y -= Math.max(4, spacingAfter);
        }

        private TableData filterColumns(TableData source) {
            var excluded = template.table().excludedColumns() == null
                    ? List.<String>of() : template.table().excludedColumns();
            var indexes = new ArrayList<Integer>();
            for (int i = 0; i < source.headers().size(); i++) {
                String header = source.headers().get(i);
                if (excluded.stream().noneMatch(name -> name.equalsIgnoreCase(header))) indexes.add(i);
            }
            var headers = indexes.stream().map(source.headers()::get).toList();
            var rows = source.rows().stream()
                    .map(row -> indexes.stream().map(row::get).toList())
                    .toList();
            return new TableData(headers, rows);
        }

        private void renderTable(TableData data, List<ElementDefinition> preamble) throws IOException {
            if (data.headers().isEmpty()) return;
            float[] widths = columnWidths(data);
            drawTableRow(data.headers(), widths, true);
            for (var row : data.rows()) {
                if (y - template.table().rowHeight() < template.page().margin()) {
                    closeContent();
                    newPage();
                    if (template.table().repeatPreamble()) renderElements(preamble);
                    drawTableRow(data.headers(), widths, true);
                }
                drawTableRow(row, widths, false);
            }
        }

        private float[] columnWidths(TableData data) {
            float available = page.getMediaBox().getWidth() - template.page().margin() * 2;
            float[] weights = new float[data.headers().size()];
            float total = 0;
            for (int col = 0; col < weights.length; col++) {
                int max = Math.min(32, Math.max(5, data.headers().get(col).length()));
                for (var row : data.rows()) max = Math.min(32, Math.max(max, row.get(col).length()));
                weights[col] = max;
                total += max;
            }
            for (int col = 0; col < weights.length; col++) weights[col] = available * weights[col] / total;
            return weights;
        }

        private void drawTableRow(List<String> values, float[] widths, boolean header) throws IOException {
            float height = template.table().rowHeight();
            float x = template.page().margin();
            float bottom = y - height;
            if (header) {
                content.setNonStrokingColor(230, 236, 245);
                content.addRect(x, bottom, sum(widths), height);
                content.fill();
                content.setNonStrokingColor(0, 0, 0);
            }
            for (int col = 0; col < widths.length; col++) {
                content.addRect(x, bottom, widths[col], height);
                content.stroke();
                PDFont font = header ? bold : regular;
                float fontSize = header ? template.table().headerFontSize() : template.table().fontSize();
                String value = fit(values.get(col), font, fontSize,
                        widths[col] - template.table().cellPadding() * 2);
                content.beginText();
                content.setFont(font, fontSize);
                content.newLineAtOffset(x + template.table().cellPadding(), bottom + (height - fontSize) / 2);
                content.showText(value);
                content.endText();
                x += widths[col];
            }
            y = bottom;
        }

        private String fit(String original, PDFont font, float fontSize, float available) throws IOException {
            String value = Objects.toString(original, "").replaceAll("[\\r\\n]+", " ");
            if (font.getStringWidth(value) / 1000f * fontSize <= available) return value;
            String suffix = "...";
            int low = 0;
            int high = value.length();
            while (low < high) {
                int middle = (low + high + 1) / 2;
                String candidate = value.substring(0, middle) + suffix;
                if (font.getStringWidth(candidate) / 1000f * fontSize <= available) low = middle;
                else high = middle - 1;
            }
            return value.substring(0, low) + suffix;
        }

        private void newPage() throws IOException {
            page = new PDPage(pageSize);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - template.page().margin();
        }

        private void closeContent() throws IOException {
            if (content != null) {
                content.close();
                content = null;
            }
        }

        private float sum(float[] values) {
            float result = 0;
            for (float value : values) result += value;
            return result;
        }
    }

    private PDRectangle pageSize(PageDefinition page) {
        PDRectangle size = switch (page.size().toUpperCase()) {
            case "A3" -> PDRectangle.A3;
            case "LETTER" -> PDRectangle.LETTER;
            default -> PDRectangle.A4;
        };
        return "LANDSCAPE".equalsIgnoreCase(page.orientation())
                ? new PDRectangle(size.getHeight(), size.getWidth()) : size;
    }
}
