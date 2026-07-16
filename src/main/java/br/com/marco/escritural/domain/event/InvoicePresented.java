package br.com.marco.escritural.domain.event;

import br.com.marco.escritural.domain.model.enums.InvoiceStatus;

import java.time.Instant;
import java.util.UUID;

public record InvoicePresented(UUID eventId, UUID invoiceId, Instant occurredAt) implements InvoiceEvent {
    @Override
    public InvoiceStatus status() {
        return InvoiceStatus.PRESENTED;
    }
}