package br.com.marco.escritural.application.dto.input;

import java.time.Instant;
import java.util.UUID;

public record ParkInvoiceEventCommand(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        int eventVersion,
        String status,
        Instant occurredAt,
        UUID correlationId
) {
}
