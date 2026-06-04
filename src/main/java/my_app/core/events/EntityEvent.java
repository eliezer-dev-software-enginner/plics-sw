package my_app.core.events;

public record EntityEvent<T>(T entity, EventType type, long entityId) {

    public enum EventType { CRIADO, EDITADO, EXCLUIDO }

    public static <T> EntityEvent<T> criado(T entity) {
        return new EntityEvent<>(entity, EventType.CRIADO, 0);
    }

    public static <T> EntityEvent<T> editado(T entity) {
        return new EntityEvent<>(entity, EventType.EDITADO, 0);
    }

    public static <T> EntityEvent<T> excluido(long id) {
        return new EntityEvent<>(null, EventType.EXCLUIDO, id);
    }

    public boolean is(EventType eventType) {
        return this.type == eventType;
    }
}
