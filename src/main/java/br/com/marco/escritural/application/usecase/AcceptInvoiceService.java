package br.com.marco.escritural.application.usecase;

import br.com.marco.escritural.application.exception.InvoiceNotFoundException;
import br.com.marco.escritural.application.ports.in.AcceptInvoiceUseCase;
import br.com.marco.escritural.application.ports.out.InvoiceRepositoryPort;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class AcceptInvoiceService implements AcceptInvoiceUseCase {
    private final InvoiceRepositoryPort repository;

    @Override
    @Transactional
    public Invoice accept(UUID id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        invoice.accept();
        return repository.save(invoice);
    }
}
