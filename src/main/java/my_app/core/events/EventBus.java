package my_app.core.events;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventBus {
    @Getter
    private static final EventBus instance = new EventBus();
    private final List<Consumer<Object>> listeners = new ArrayList<>();

    private EventBus() {}

    public void subscribe(Consumer<Object> listener) {
        listeners.add(listener);
    }

    public void publish(Object event) {
        for (Consumer<Object> listener : listeners) {
            listener.accept(event);
        }
    }
}
