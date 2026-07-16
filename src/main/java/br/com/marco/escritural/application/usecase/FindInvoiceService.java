package br.com.marco.escritural.application.usecase;

import br.com.marco.escritural.application.exception.InvoiceNotFoundException;
import br.com.marco.escritural.application.ports.in.FindInvoiceUseCase;
import br.com.marco.escritural.application.ports.out.InvoiceRepositoryPort;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class FindInvoiceService implements FindInvoiceUseCase {
    private final InvoiceRepositoryPort repository;

    @Override
    public Invoice findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
    }
}
