package br.com.marco.escritural.application.ports.out;

import br.com.marco.escritural.application.dto.input.ParkInvoiceEventCommand;

public interface ParkedInvoiceEventRepositoryPort {
    void save(ParkInvoiceEventCommand command);
}
