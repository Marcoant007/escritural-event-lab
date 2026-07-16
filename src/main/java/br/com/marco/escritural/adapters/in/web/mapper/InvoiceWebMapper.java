package br.com.marco.escritural.adapters.in.web.mapper;

import br.com.marco.escritural.adapters.in.web.dto.request.IssueInvoiceRequest;
import br.com.marco.escritural.adapters.in.web.dto.response.InvoiceResponse;
import br.com.marco.escritural.application.dto.input.IssueInvoiceCommand;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InvoiceWebMapper {
    public IssueInvoiceCommand toCommand(IssueInvoiceRequest request) {
        return IssueInvoiceCommand.builder()
                .number(request.number()).issuerDocument(request.issuerDocument())
                .payerDocument(request.payerDocument()).amount(request.amount())
                .issueDate(request.issueDate()).dueDate(request.dueDate())
                .build();
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId()).number(invoice.getNumber())
                .issuerDocument(invoice.getIssuerDocument()).payerDocument(invoice.getPayerDocument())
                .amount(invoice.getAmount()).issueDate(invoice.getIssueDate()).dueDate(invoice.getDueDate())
                .status(invoice.getStatus()).createdAt(invoice.getCreatedAt()).updatedAt(invoice.getUpdatedAt())
                .build();
    }
}
