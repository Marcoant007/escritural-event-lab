package br.com.marco.escritural.application.usecase;

import br.com.marco.escritural.application.exception.InvoiceNotFoundException;
import br.com.marco.escritural.application.ports.in.RejectInvoiceUseCase;
import br.com.marco.escritural.application.ports.out.InvoiceHistoryRepositoryPort;
import br.com.marco.escritural.application.ports.out.InvoiceRepositoryPort;
import br.com.marco.escritural.domain.event.InvoiceRejected;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class RejectInvoiceService implements RejectInvoiceUseCase {
    private final InvoiceRepositoryPort repository;
    private final InvoiceHistoryRepositoryPort historyRepository;

    @Override
    @Transactional
    public Invoice reject(UUID id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        InvoiceRejected event = invoice.reject();
        Invoice saved = repository.save(invoice);
        historyRepository.save(event);
        return saved;
    }
}
