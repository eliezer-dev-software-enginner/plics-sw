package my_app.events;

import my_app.db.models.TecnicoModel;

public class TecnicoCriadoEvent {
    private final TecnicoModel tecnico;

    public TecnicoCriadoEvent(TecnicoModel tecnico) {
        this.tecnico = tecnico;
    }

    public TecnicoModel getTecnico() {
        return tecnico;
    }
}