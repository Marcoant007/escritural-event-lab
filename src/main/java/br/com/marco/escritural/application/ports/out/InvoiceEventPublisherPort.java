package br.com.marco.escritural.application.ports.out;

import br.com.marco.escritural.domain.event.InvoiceEvent;

public interface InvoiceEventPublisherPort {
    void publish(InvoiceEvent event);
}
