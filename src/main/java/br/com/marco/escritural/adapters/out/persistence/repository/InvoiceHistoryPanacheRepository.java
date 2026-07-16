package br.com.marco.escritural.adapters.out.persistence.repository;

import br.com.marco.escritural.adapters.out.persistence.entity.InvoiceHistoryEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class InvoiceHistoryPanacheRepository implements PanacheRepositoryBase<InvoiceHistoryEntity, UUID> {
}