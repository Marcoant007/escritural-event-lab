package br.com.marco.escritural.adapters.out.persistence.repository;

import br.com.marco.escritural.adapters.out.persistence.entity.ParkedInvoiceEventEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class ParkedInvoiceEventPanacheRepository implements PanacheRepositoryBase<ParkedInvoiceEventEntity, UUID> {
}
