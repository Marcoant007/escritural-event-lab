package br.com.marco.escritural.application.ports.in;

import br.com.marco.escritural.application.dto.input.IssueInvoiceCommand;
import br.com.marco.escritural.domain.model.aggregate.Invoice;

public interface IssueInvoiceUseCase {
    Invoice execute(IssueInvoiceCommand command);
}
