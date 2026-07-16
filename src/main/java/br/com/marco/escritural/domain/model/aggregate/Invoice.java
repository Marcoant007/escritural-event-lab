package br.com.marco.escritural.domain.model.aggregate;

import br.com.marco.escritural.domain.event.InvoiceAccepted;
import br.com.marco.escritural.domain.event.InvoiceIssued;
import br.com.marco.escritural.domain.event.InvoicePresented;
import br.com.marco.escritural.domain.event.InvoiceRejected;
import br.com.marco.escritural.domain.exception.BusinessRuleException;
import br.com.marco.escritural.domain.exception.InvalidStatusTransitionException;
import br.com.marco.escritural.domain.model.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Getter
public final class Invoice {
    private final UUID id;
    private final String number;
    private final String issuerDocument;
    private final String payerDocument;
    private final BigDecimal amount;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private InvoiceStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Invoice(UUID id, String number, String issuerDocument, String payerDocument,
                    BigDecimal amount, LocalDate issueDate, LocalDate dueDate,
                    InvoiceStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.number = required(number, "Number");
        this.issuerDocument = required(issuerDocument, "Issuer document");
        this.payerDocument = required(payerDocument, "Payer document");
        this.amount = validateAmount(amount);
        this.issueDate = Objects.requireNonNull(issueDate, "Issue date is required");
        this.dueDate = validateDueDate(this.issueDate, dueDate);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    @Builder(builderMethodName = "builder", builderClassName = "IssueBuilder")
    private static Invoice issue(String number, String issuerDocument, String payerDocument,
                                 BigDecimal amount, LocalDate issueDate, LocalDate dueDate) {
        Instant now = Instant.now();
        return new Invoice(null, number, issuerDocument, payerDocument, amount, issueDate,
                dueDate, InvoiceStatus.ISSUED, now, now);
    }

    @Builder(builderMethodName = "reconstitutionBuilder", builderClassName = "ReconstitutionBuilder")
    private static Invoice reconstitute(UUID id, String number, String issuerDocument, String payerDocument,
                                        BigDecimal amount, LocalDate issueDate, LocalDate dueDate,
                                        InvoiceStatus status, Instant createdAt, Instant updatedAt) {
        return new Invoice(id, number, issuerDocument, payerDocument, amount, issueDate,
                dueDate, status, createdAt, updatedAt);
    }

    public InvoiceIssued issuedEvent() {
        return new InvoiceIssued(UUID.randomUUID(), id, createdAt);
    }

    public InvoicePresented present() {
        transitionFrom(InvoiceStatus.ISSUED, InvoiceStatus.PRESENTED);
        return new InvoicePresented(UUID.randomUUID(), id, updatedAt);
    }

    public InvoiceAccepted accept() {
        transitionFrom(InvoiceStatus.PRESENTED, InvoiceStatus.ACCEPTED);
        return new InvoiceAccepted(UUID.randomUUID(), id, updatedAt);
    }

    public InvoiceRejected reject() {
        transitionFrom(InvoiceStatus.PRESENTED, InvoiceStatus.REJECTED);
        return new InvoiceRejected(UUID.randomUUID(), id, updatedAt);
    }

    private void transitionFrom(InvoiceStatus expected, InvoiceStatus target) {
        if (status != expected) {
            throw new InvalidStatusTransitionException(status, target);
        }
        status = target;
        updatedAt = Instant.now();
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(field + " is required");
        }
        return value.trim();
    }

    private static BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("Amount must be greater than zero");
        }
        return amount;
    }

    private static LocalDate validateDueDate(LocalDate issueDate, LocalDate dueDate) {
        if (dueDate == null) {
            throw new BusinessRuleException("Due date is required");
        }
        if (dueDate.isBefore(issueDate)) {
            throw new BusinessRuleException("Due date cannot be before issue date");
        }
        return dueDate;
    }
}
