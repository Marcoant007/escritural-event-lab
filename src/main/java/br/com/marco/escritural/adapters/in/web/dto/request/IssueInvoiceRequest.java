package br.com.marco.escritural.adapters.in.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record IssueInvoiceRequest(
        @NotBlank String number,
        @NotBlank String issuerDocument,
        @NotBlank String payerDocument,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull LocalDate issueDate,
        @NotNull LocalDate dueDate) {
}
