package br.com.marco.escritural.adapters.in.web.dto.response;

import br.com.marco.escritural.domain.model.enums.InvoiceStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record InvoiceResponse(UUID id, String number, String issuerDocument, String payerDocument,
                              BigDecimal amount, LocalDate issueDate, LocalDate dueDate,
                              InvoiceStatus status, Instant createdAt, Instant updatedAt) {
}
