package my_app.screens.empresaScreen;

import javafx.stage.FileChooser;
import megalodonte.base.state.State;
import megalodonte.base.UI;
import megalodonte.base.async.Async;
import megalodonte.router.v4.ScreenContext;
import my_app.db.models.EmpresaModel;
import my_app.db.services.EmpresaService;
import my_app.domain.components.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;

public class EmpresaViewModel {
    private static final Logger log = LoggerFactory.getLogger(EmpresaViewModel.class);

    private final ScreenContext ctx;
    private final EmpresaService empresaService;

    final State<String> nome = State.of("");
    final State<String> celular = State.of("");
    final State<String> logoMarca = State.of("/logo_256x256.png");

    final State<String> cep = State.of("");
    final State<String> cidade = State.of("");
    final State<String> bairro = State.of("");
    final State<String> rua = State.of("");

    final State<String> localPagamento = State.of("");
    final State<String> textoResponsabilidade = State.of("");

    public EmpresaViewModel(ScreenContext ctx) {
        this.ctx = ctx;
        this.empresaService = createOrReport(EmpresaService::new);
    }

    private static <T> T createOrReport(megalodonte.utils.ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            megalodonte.application.ErrorReporter.handle(e);
            throw new IllegalStateException(e);
        }
    }

    public void fetchData() {
        Async.Run(()->{
            try {
                var model = empresaService.buscarUnico();
                if(model != null){

                    UI.runOnUi(()->{
                        nome.set(model.getNome());
                        celular.set(model.getTelefone());
                        logoMarca.set(model.getLogoMarca() != null ? model.getLogoMarca() : "/logo_256x256.png");
                        cep.set(model.getCep());
                        cidade.set(model.getCidade());
                        bairro.set(model.getBairro());
                        rua.set(model.getRua());
                        localPagamento.set(model.getLocalPagamento());
                        textoResponsabilidade.set(model.getTextoResponsabilidade());
                    });
                }

            } catch (Exception e) {
                log.error("Erro ao carregar dados da empresa", e);
                throw new RuntimeException("Erro ao carregar dados da empresa", e);
            }
        });
    }

    public void handleUpdateLogoMarca(){
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar imagem");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text files", "*.png","*.jpg","*.jpeg"));

        File arquivo = fileChooser.showOpenDialog(this.ctx.selfStage());
        if(arquivo != null){
            IO.println("abs: " + arquivo.getAbsolutePath());
            String imagePath = arquivo.toURI().toString();
            IO.println("uri: " + imagePath);

            logoMarca.set(imagePath);
        }
    }

    public void handleSave(){
        var model = new EmpresaModel();
        model.setId(1);
        model.setNome(nome.get());
        model.setLogoMarca(logoMarca.get());
        model.setCep(cep.get());
        model.setBairro(bairro.get());
        model.setRua(rua.get());
        model.setCidade(cidade.get());
        model.setLocalPagamento(localPagamento.get());
        model.setTermoServico(textoResponsabilidade.get());
        model.setTelefone(celular.get());
        model.setTextoResponsabilidade(textoResponsabilidade.get());

        Async.Run(()->{
            try{
                empresaService.salvarOuAtualizar(model);
                UI.runOnUi(()->  Components.ShowPopup(ctx, "Fornecedor atualizado com sucesso"));

            } catch (Exception e) {
                log.error("Erro ao salvar dados da empresa", e);
                UI.runOnUi(() -> Components.ShowAlertError(e.getMessage()));
            }
        });
    }

    //@Override
    public void onDestroy() throws Exception {
        this.empresaService.close();
    }
}
