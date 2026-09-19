package my_app.core.events;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventBusTest {

    @Test
    void subscriberRecebeEvento() {
        var contador = new AtomicInteger();
        Consumer<Object> listener = e -> contador.incrementAndGet();
        EventBus.getInstance().subscribe(listener);

        EventBus.getInstance().publish("evento");
        EventBus.getInstance().unsubscribe(listener);

        assertEquals(1, contador.get());
    }

    @Test
    void unsubscribedNaoRecebeMaisEvento() {
        var contador = new AtomicInteger();
        Consumer<Object> listener = e -> contador.incrementAndGet();
        EventBus.getInstance().subscribe(listener);
        EventBus.getInstance().unsubscribe(listener);

        EventBus.getInstance().publish("evento");

        assertEquals(0, contador.get());
    }
}