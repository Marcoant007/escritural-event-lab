package br.com.marco.escritural.application.ports.in;

import br.com.marco.escritural.application.dto.input.NotifyInvoiceEventCommand;

public interface NotifyInvoiceEventUseCase {
    void notify(NotifyInvoiceEventCommand notifyInvoiceEventCommand);
}
