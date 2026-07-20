package br.com.marco.escritural.application.usecase;

import br.com.marco.escritural.application.dto.input.NotifyInvoiceEventCommand;
import br.com.marco.escritural.application.ports.in.NotifyInvoiceEventUseCase;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mock
@ApplicationScoped
public class NotifyInvoiceEventUseCaseFake implements NotifyInvoiceEventUseCase {
    private final List<NotifyInvoiceEventCommand> received = new CopyOnWriteArrayList<>();

    @Override
    public void notify(NotifyInvoiceEventCommand command) {
        received.add(command);
    }

    public List<NotifyInvoiceEventCommand> received() {
        return received;
    }
}
