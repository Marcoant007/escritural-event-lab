package br.com.marco.escritural.adapters.out.persistence.repository;

import br.com.marco.escritural.adapters.out.persistence.entity.InvoiceEntity;
import br.com.marco.escritural.adapters.out.persistence.mapper.InvoicePersistenceMapper;
import br.com.marco.escritural.application.ports.out.InvoiceRepositoryPort;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {
    private final InvoicePanacheRepository repository;
    private final InvoicePersistenceMapper mapper;

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceEntity entity = mapper.toEntity(invoice);
        InvoiceEntity managed = repository.getEntityManager().merge(entity);
        return mapper.toDomain(managed);
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return repository.findByIdOptional(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByNumber(String number) { return repository.existsByNumber(number); }
}
