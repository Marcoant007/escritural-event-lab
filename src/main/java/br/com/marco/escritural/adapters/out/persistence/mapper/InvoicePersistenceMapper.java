package br.com.marco.escritural.adapters.out.persistence.mapper;

import br.com.marco.escritural.adapters.out.persistence.entity.InvoiceEntity;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InvoicePersistenceMapper {
    public InvoiceEntity toEntity(Invoice invoice) {
        return InvoiceEntity.builder()
                .id(invoice.getId()).number(invoice.getNumber())
                .issuerDocument(invoice.getIssuerDocument()).payerDocument(invoice.getPayerDocument())
                .amount(invoice.getAmount()).issueDate(invoice.getIssueDate()).dueDate(invoice.getDueDate())
                .status(invoice.getStatus()).createdAt(invoice.getCreatedAt()).updatedAt(invoice.getUpdatedAt())
                .build();
    }

    public Invoice toDomain(InvoiceEntity entity) {
        return Invoice.reconstitutionBuilder()
                .id(entity.getId()).number(entity.getNumber())
                .issuerDocument(entity.getIssuerDocument()).payerDocument(entity.getPayerDocument())
                .amount(entity.getAmount()).issueDate(entity.getIssueDate()).dueDate(entity.getDueDate())
                .status(entity.getStatus()).createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt())
                .build();
    }
}
