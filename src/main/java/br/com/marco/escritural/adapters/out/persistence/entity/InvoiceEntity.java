package br.com.marco.escritural.adapters.out.persistence.entity;

import br.com.marco.escritural.domain.model.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoice")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceEntity {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 50)
    private String number;
    @Column(name = "issuer_document", nullable = false, length = 20)
    private String issuerDocument;
    @Column(name = "payer_document", nullable = false, length = 20)
    private String payerDocument;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
