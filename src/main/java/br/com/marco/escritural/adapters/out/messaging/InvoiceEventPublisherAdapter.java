package br.com.marco.escritural.adapters.out.messaging;

import br.com.marco.escritural.adapters.out.messaging.dto.InvoiceEventMessage;
import br.com.marco.escritural.application.ports.out.InvoiceEventPublisherPort;
import br.com.marco.escritural.domain.event.InvoiceEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import io.smallrye.reactive.messaging.kafka.Record;
import org.eclipse.microprofile.reactive.messaging.Channel;

import java.util.UUID;

@ApplicationScoped
public class InvoiceEventPublisherAdapter implements InvoiceEventPublisherPort {

    @Channel("duplicata-events")
    Emitter<Record<String, InvoiceEventMessage>> emitter;

    @Override
    public void publish(InvoiceEvent event) {
        InvoiceEventMessage message = new InvoiceEventMessage(
                event.eventId(),
                event.getClass().getSimpleName(),
                1,
                event.invoiceId(),
                event.occurredAt(),
                UUID.randomUUID(),
                event.status().name()
        );

        emitter.send(Record.of(event.invoiceId().toString(), message));
    }
}
