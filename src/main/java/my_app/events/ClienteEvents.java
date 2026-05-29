package my_app.events;

import my_app.db.models.ClienteModel;

public final class ClienteEvents {

    private ClienteEvents() {}

    public record Criado(ClienteModel clienteModel) {}
    public record Editado(ClienteModel clienteModel) {}
    public record Excluido(long id) {}
}