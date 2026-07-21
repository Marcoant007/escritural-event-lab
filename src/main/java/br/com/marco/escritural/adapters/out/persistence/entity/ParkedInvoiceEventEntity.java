package br.com.marco.escritural.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parked_invoice_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkedInvoiceEventEntity {
    @Id
    private UUID id;
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;
    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;
    @Column(name = "event_version", nullable = false)
    private int eventVersion;
    @Column(nullable = false, length = 60)
    private String status;
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;
    @Column(name = "parked_at", nullable = false)
    private Instant parkedAt;
}
