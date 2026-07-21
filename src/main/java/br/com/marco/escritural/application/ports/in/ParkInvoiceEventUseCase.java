package br.com.marco.escritural.application.ports.in;

import br.com.marco.escritural.application.dto.input.ParkInvoiceEventCommand;

public interface ParkInvoiceEventUseCase {
    void park(ParkInvoiceEventCommand command);
}
