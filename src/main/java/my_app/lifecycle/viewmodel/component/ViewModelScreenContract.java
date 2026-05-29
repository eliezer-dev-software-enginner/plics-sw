package my_app.lifecycle.viewmodel.component;

import megalodonte.ComputedState;
import megalodonte.State;
import megalodonte.base.UI;
import megalodonte.router.v4.ScreenContext;
import my_app.db.DB;
import my_app.domain.components.Components;
import net.sf.persism.Session;
import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class ViewModelScreenContract {
    protected final ScreenContext ctx;
    protected final State<Boolean> modoEdicao = State.of(false);

    public final State<Boolean> focusState = new State<>(false);

    public ComputedState<String> btnText = ComputedState.of(() -> modoEdicao.get() ? "Atualizar" : "+ Adicionar", modoEdicao);

    public ViewModelScreenContract(ScreenContext ctx){
        this.ctx = ctx;
    }

    protected void onInit() {
        // ciclo de vida futuro
    }

    protected void onDispose() {
        // limpeza futura
    }

    public abstract void populateFromModel();
    public abstract void clearForm();
    public abstract void handleAddOrUpdate();
    public abstract void handleClickMenuDelete();

    public State<Boolean> modoEdicaoState(){
        return modoEdicao;
    }


    public void handleFocusChange(boolean focus) {
        focusState.set(focus);
    }

    protected Session getPersismSession() throws SQLException {
        var db = DB.production();
        Flyway.configure()
                .dataSource(db.url(),"","")
                .locations("classpath:flyway_migrations")
                .load()
                .migrate();

        return new Session(db.connection());
    }
}
