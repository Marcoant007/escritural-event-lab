package br.com.marco.escritural.application.ports.in;

import br.com.marco.escritural.domain.model.aggregate.Invoice;

import java.util.UUID;

public interface AcceptInvoiceUseCase {
    Invoice accept(UUID id);
}
