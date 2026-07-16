package br.com.marco.escritural.adapters.out.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record InvoiceEventMessage(
        UUID eventId,
        String eventType,
        int eventVersion,
        UUID aggregateId,
        Instant occurredAt,
        UUID correlationId,
        String status
) {
}
