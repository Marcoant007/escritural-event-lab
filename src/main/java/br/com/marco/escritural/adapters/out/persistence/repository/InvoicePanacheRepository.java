package br.com.marco.escritural.adapters.out.persistence.repository;

import br.com.marco.escritural.adapters.out.persistence.entity.InvoiceEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class InvoicePanacheRepository implements PanacheRepositoryBase<InvoiceEntity, UUID> {
    public boolean existsByNumber(String number) { return count("number", number) > 0; }
}
