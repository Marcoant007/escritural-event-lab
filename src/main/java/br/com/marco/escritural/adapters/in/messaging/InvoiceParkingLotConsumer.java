package br.com.marco.escritural.adapters.in.messaging;

import br.com.marco.escritural.adapters.out.messaging.dto.InvoiceEventMessage;
import br.com.marco.escritural.application.dto.input.ParkInvoiceEventCommand;
import br.com.marco.escritural.application.ports.in.ParkInvoiceEventUseCase;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
@RequiredArgsConstructor
public class InvoiceParkingLotConsumer {
    private final ParkInvoiceEventUseCase parkInvoiceEventUseCase;

    @Incoming("duplicata-events-dlq-in")
    @Blocking
    public void consume(InvoiceEventMessage message) {
        parkInvoiceEventUseCase.park(new ParkInvoiceEventCommand(
                message.eventId(),
                message.aggregateId(),
                message.eventType(),
                message.eventVersion(),
                message.status(),
                message.occurredAt(),
                message.correlationId()
        ));
    }
}
