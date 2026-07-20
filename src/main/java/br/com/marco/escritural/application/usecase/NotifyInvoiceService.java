package br.com.marco.escritural.application.usecase;

import br.com.marco.escritural.application.dto.input.NotifyInvoiceEventCommand;
import br.com.marco.escritural.application.ports.in.NotifyInvoiceEventUseCase;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotifyInvoiceService implements NotifyInvoiceEventUseCase {


    @Override
    public void notify(NotifyInvoiceEventCommand command) {
        Log.infof("Cliente notificado: duplicata %s agora %s",
                command.invoiceId(),
                command.invoiceStatus());
    }
}
