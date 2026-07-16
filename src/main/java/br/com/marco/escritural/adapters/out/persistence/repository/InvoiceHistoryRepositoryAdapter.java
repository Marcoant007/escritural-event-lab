package br.com.marco.escritural.adapters.out.persistence.repository;

import br.com.marco.escritural.adapters.out.persistence.entity.InvoiceHistoryEntity;
import br.com.marco.escritural.application.ports.out.InvoiceHistoryRepositoryPort;
import br.com.marco.escritural.domain.event.InvoiceEvent;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class InvoiceHistoryRepositoryAdapter implements InvoiceHistoryRepositoryPort {
    private final InvoiceHistoryPanacheRepository repository;

    @Override
    public void save(InvoiceEvent event) {
        InvoiceHistoryEntity entity = InvoiceHistoryEntity.builder()
                .id(event.eventId())
                .invoiceId(event.invoiceId())
                .status(event.status())
                .occurredAt(event.occurredAt())
                .build();
        repository.persist(entity);
    }
}
