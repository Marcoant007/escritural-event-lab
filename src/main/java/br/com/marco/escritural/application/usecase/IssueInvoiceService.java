package br.com.marco.escritural.application.usecase;

import br.com.marco.escritural.application.dto.input.IssueInvoiceCommand;
import br.com.marco.escritural.application.exception.DuplicateInvoiceException;
import br.com.marco.escritural.application.ports.in.IssueInvoiceUseCase;
import br.com.marco.escritural.application.ports.out.InvoiceHistoryRepositoryPort;
import br.com.marco.escritural.application.ports.out.InvoiceRepositoryPort;
import br.com.marco.escritural.domain.event.InvoiceIssued;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class IssueInvoiceService implements IssueInvoiceUseCase {
    private final InvoiceRepositoryPort repository;
    private final InvoiceHistoryRepositoryPort historyRepository;

    @Override
    @Transactional
    public Invoice execute(IssueInvoiceCommand command) {
        if (repository.existsByNumber(command.number())) {
            throw new DuplicateInvoiceException(command.number());
        }
        Invoice invoice = Invoice.builder()
                .number(command.number())
                .issuerDocument(command.issuerDocument())
                .payerDocument(command.payerDocument())
                .amount(command.amount())
                .issueDate(command.issueDate())
                .dueDate(command.dueDate())
                .build();
        InvoiceIssued event = invoice.issuedEvent();
        Invoice saved = repository.save(invoice);
        historyRepository.save(event);
        return saved;
    }
}
