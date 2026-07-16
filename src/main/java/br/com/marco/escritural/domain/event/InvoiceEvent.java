package br.com.marco.escritural.domain.event;

import br.com.marco.escritural.domain.model.enums.InvoiceStatus;

import java.time.Instant;
import java.util.UUID;

public interface InvoiceEvent {
    UUID eventId();
    UUID invoiceId();
    Instant occurredAt();
    InvoiceStatus status();
}
