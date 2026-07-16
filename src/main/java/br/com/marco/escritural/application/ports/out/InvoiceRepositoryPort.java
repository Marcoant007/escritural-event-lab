package br.com.marco.escritural.application.ports.out;

import br.com.marco.escritural.domain.model.aggregate.Invoice;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepositoryPort {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(UUID id);
    boolean existsByNumber(String number);
}
