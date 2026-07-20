package br.com.marco.escritural.application.dto.input;

import br.com.marco.escritural.domain.model.enums.InvoiceStatus;

import java.time.Instant;
import java.util.UUID;

public record NotifyInvoiceEventCommand(
        UUID invoiceId,
        InvoiceStatus invoiceStatus,
        Instant occurredAt
) {
}
