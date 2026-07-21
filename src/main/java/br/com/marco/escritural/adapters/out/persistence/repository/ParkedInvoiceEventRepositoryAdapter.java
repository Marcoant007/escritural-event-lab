package br.com.marco.escritural.adapters.out.persistence.repository;

import br.com.marco.escritural.adapters.out.persistence.entity.ParkedInvoiceEventEntity;
import br.com.marco.escritural.application.dto.input.ParkInvoiceEventCommand;
import br.com.marco.escritural.application.ports.out.ParkedInvoiceEventRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@ApplicationScoped
@RequiredArgsConstructor
public class ParkedInvoiceEventRepositoryAdapter implements ParkedInvoiceEventRepositoryPort {
    private final ParkedInvoiceEventPanacheRepository repository;

    @Override
    public void save(ParkInvoiceEventCommand command) {
        ParkedInvoiceEventEntity entity = ParkedInvoiceEventEntity.builder()
                .id(command.eventId())
                .aggregateId(command.aggregateId())
                .eventType(command.eventType())
                .eventVersion(command.eventVersion())
                .status(command.status())
                .occurredAt(command.occurredAt())
                .correlationId(command.correlationId())
                .parkedAt(Instant.now())
                .build();
        repository.persist(entity);
    }
}
