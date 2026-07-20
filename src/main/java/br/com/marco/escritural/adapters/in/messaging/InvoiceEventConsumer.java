package br.com.marco.escritural.adapters.in.messaging;

import br.com.marco.escritural.adapters.out.messaging.dto.InvoiceEventMessage;
import br.com.marco.escritural.application.dto.input.NotifyInvoiceEventCommand;
import br.com.marco.escritural.application.ports.in.NotifyInvoiceEventUseCase;
import br.com.marco.escritural.domain.model.enums.InvoiceStatus;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
@RequiredArgsConstructor
public class InvoiceEventConsumer {
    private final NotifyInvoiceEventUseCase notifyInvoiceEventUseCase;

    @Incoming("duplicata-events-in")
    public void consume(InvoiceEventMessage message) {
        notifyInvoiceEventUseCase.notify(new NotifyInvoiceEventCommand(
                message.aggregateId(),
                InvoiceStatus.valueOf(message.status()),
                message.occurredAt()
        ));
    }
}
