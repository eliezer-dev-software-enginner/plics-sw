package my_app.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventBus {
    private static final EventBus instance = new EventBus();
    private final List<Consumer<Object>> listeners = new ArrayList<>();

    private EventBus() {}

    public static EventBus getInstance() {
        return instance;
    }

    public void subscribe(Consumer<Object> listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Consumer<Object> listener) {
        listeners.remove(listener);
    }

    public void publish(Object event) {
        for (Consumer<Object> listener : listeners) {
            listener.accept(event);
        }
    }
}