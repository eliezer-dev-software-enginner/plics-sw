package my_app.services.exports;

import javafx.stage.FileChooser;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.base.route.v2.ScreenContextInterface;
import megalodonte.components.SimpleTable;
import my_app.core.components.Components;
import my_app.core.db.models.EmpresaModel;
import my_app.core.db.services.EmpresaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/** Liga o seletor de arquivo e os dados da empresa ao motor genérico de exportação. */
public final class TableExportActions {

    private static final Logger log = LoggerFactory.getLogger(TableExportActions.class);

    private TableExportActions() {
    }

    public static void csv(ScreenContextInterface context, String title, SimpleTable<?> table) {
        File destination = choose(context, title, "CSV", "*.csv");
        if (destination == null) return;
        var data = snapshot(table);
        Async.Run(() -> {
            try {
                new TableExportEngine().writeCsv(destination.toPath(), data);
                success(context, destination);
            } catch (Exception exception) {
                failure("CSV", exception);
            }
        });
    }

    public static void pdf(ScreenContextInterface context, String title, SimpleTable<?> table) {
        File destination = choose(context, title, "PDF", "*.pdf");
        if (destination == null) return;
        var data = snapshot(table);
        Async.Run(() -> {
            try (var empresaService = new EmpresaService()) {
                EmpresaModel company = empresaService.buscarUnico();
                new TableExportEngine().writePdf(destination.toPath(), data, variables(company, title));
                success(context, destination);
            } catch (Exception exception) {
                failure("PDF", exception);
            }
        });
    }

    static Map<String, String> variables(EmpresaModel company, String title) {
        var variables = new LinkedHashMap<String, String>();
        variables.put("NOME_EMPRESA", company == null || blank(company.getNome()) ? "Plics SW" : company.getNome());
        variables.put("DOCUMENTO_EMPRESA", labeled("CPF/CNPJ", company == null ? null : company.getCpfCnpj()));
        variables.put("TELEFONE", labeled("Telefone", company == null ? null : company.getTelefone()));
        variables.put("ENDERECO", address(company));
        variables.put("TITULO_TABELA", title == null || title.isBlank() ? "Tabela" : title);
        variables.put("GERADO_EM", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return Map.copyOf(variables);
    }

    private static TableExportEngine.TableData snapshot(SimpleTable<?> table) {
        var snapshot = table.exportData();
        return new TableExportEngine.TableData(snapshot.headers(), snapshot.rows());
    }

    private static File choose(ScreenContextInterface context, String title, String format, String pattern) {
        var chooser = new FileChooser();
        chooser.setTitle("Salvar tabela em " + format);
        chooser.setInitialFileName(fileName(title) + "." + format.toLowerCase());
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(format, pattern));
        return chooser.showSaveDialog(context.selfStage());
    }

    private static String fileName(String title) {
        String normalized = Normalizer.normalize(title == null ? "tabela" : title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return normalized.isBlank() ? "tabela" : normalized;
    }

    private static String address(EmpresaModel company) {
        if (company == null) return "";
        String address = java.util.stream.Stream.of(company.getRua(), company.getBairro(), company.getCidade())
                .filter(value -> !blank(value))
                .collect(java.util.stream.Collectors.joining(" - "));
        String cep = formatCep(company.getCep());
        if (!blank(cep)) address += (address.isBlank() ? "" : " - ") + "CEP: " + cep;
        return address.isBlank() ? "" : "Endereço: " + address;
    }

    private static String formatCep(String value) {
        if (blank(value)) return "";
        String digits = value.replaceAll("\\D", "");
        return digits.length() == 8 ? digits.substring(0, 5) + "-" + digits.substring(5) : value;
    }

    private static String labeled(String label, String value) {
        return blank(value) ? "" : label + ": " + value;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static void success(ScreenContextInterface context, File destination) {
        UI.runOnUi(() -> Components.ShowPopup(context, "Arquivo salvo em: " + destination.getAbsolutePath()));
    }

    private static void failure(String format, Exception exception) {
        log.error("Erro ao exportar tabela para {}", format, exception);
        UI.runOnUi(() -> Components.ShowAlertError("Erro ao exportar " + format + ": " + exception.getMessage()));
    }
}
