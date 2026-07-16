package br.com.marco.escritural.adapters.out.persistence.entity;

import br.com.marco.escritural.domain.model.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoice_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceHistoryEntity {
    @Id
    private UUID id;
    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status;
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}