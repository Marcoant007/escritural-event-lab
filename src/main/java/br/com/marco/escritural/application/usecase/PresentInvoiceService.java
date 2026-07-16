package br.com.marco.escritural.application.usecase;

import br.com.marco.escritural.application.exception.InvoiceNotFoundException;
import br.com.marco.escritural.application.ports.in.PresentInvoiceUseCase;
import br.com.marco.escritural.application.ports.out.InvoiceHistoryRepositoryPort;
import br.com.marco.escritural.application.ports.out.InvoiceRepositoryPort;
import br.com.marco.escritural.domain.event.InvoicePresented;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class PresentInvoiceService implements PresentInvoiceUseCase {
    private final InvoiceRepositoryPort repository;
    private final InvoiceHistoryRepositoryPort historyRepository;

    @Override
    @Transactional
    public Invoice present(UUID id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        InvoicePresented event = invoice.present();
        Invoice saved = repository.save(invoice);
        historyRepository.save(event);
        return saved;
    }
}
