package br.com.marco.escritural.application.usecase;

import br.com.marco.escritural.application.exception.InvoiceNotFoundException;
import br.com.marco.escritural.application.ports.in.PresentInvoiceUseCase;
import br.com.marco.escritural.application.ports.out.InvoiceRepositoryPort;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class PresentInvoiceService implements PresentInvoiceUseCase {
    private final InvoiceRepositoryPort repository;

    @Override
    @Transactional
    public Invoice present(UUID id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        invoice.present();
        return repository.save(invoice);
    }
}
