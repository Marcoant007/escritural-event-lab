package br.com.marco.escritural.hub;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;

@ApplicationScoped
public class HubEventBus {

    private final BroadcastProcessor<HubEvent> processor = BroadcastProcessor.create();

    public void publish(String source, String stage, String detail) {
        processor.onNext(new HubEvent(Instant.now(), source, stage, detail));
    }

    public Multi<HubEvent> stream() {
        return processor;
    }

    public record HubEvent(Instant timestamp, String source, String stage, String detail) {
    }
}
