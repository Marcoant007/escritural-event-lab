package br.com.marco.escritural.application.usecase;

import br.com.marco.escritural.application.dto.input.ParkInvoiceEventCommand;
import br.com.marco.escritural.application.ports.in.ParkInvoiceEventUseCase;
import br.com.marco.escritural.application.ports.out.ParkedInvoiceEventRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ParkInvoiceEventService implements ParkInvoiceEventUseCase {
    private final ParkedInvoiceEventRepositoryPort repository;

    @Override
    @Transactional
    public void park(ParkInvoiceEventCommand command) {
        repository.save(command);
    }
}
