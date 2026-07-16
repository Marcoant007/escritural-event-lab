package br.com.marco.escritural.application.dto.input;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record IssueInvoiceCommand(String number, String issuerDocument, String payerDocument,
                                  BigDecimal amount, LocalDate issueDate, LocalDate dueDate) {
}
