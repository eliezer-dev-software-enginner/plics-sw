package my_app.events;

import my_app.db.models.TecnicoModel;

public final class TecnicoEvents {

    private TecnicoEvents() {}

    public record Criado(TecnicoModel tecnico) {}
    public record Editado(TecnicoModel tecnico) {}
    public record Excluido(long id) {}
}