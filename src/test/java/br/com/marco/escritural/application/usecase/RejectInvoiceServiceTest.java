package br.com.marco.escritural.application.usecase;

import br.com.marco.escritural.adapters.out.persistence.entity.InvoiceHistoryEntity;
import br.com.marco.escritural.adapters.out.persistence.repository.InvoiceHistoryPanacheRepository;
import br.com.marco.escritural.application.dto.input.IssueInvoiceCommand;
import br.com.marco.escritural.application.ports.in.IssueInvoiceUseCase;
import br.com.marco.escritural.application.ports.in.PresentInvoiceUseCase;
import br.com.marco.escritural.application.ports.in.RejectInvoiceUseCase;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import br.com.marco.escritural.domain.model.enums.InvoiceStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class RejectInvoiceServiceTest {
    @Inject
    IssueInvoiceUseCase issueInvoice;
    @Inject
    PresentInvoiceUseCase presentInvoice;
    @Inject
    RejectInvoiceUseCase rejectInvoice;
    @Inject
    InvoiceHistoryPanacheRepository historyRepository;

    @Test
    void shouldRecordHistoryWhenInvoiceIsRejected() {
        Invoice invoice = issueInvoice.execute(validCommand());
        presentInvoice.present(invoice.getId());

        rejectInvoice.reject(invoice.getId());

        List<InvoiceHistoryEntity> history = historyRepository.list("invoiceId", invoice.getId());
        assertEquals(3, history.size());
        assertEquals(1, history.stream().filter(h -> h.getStatus() == InvoiceStatus.REJECTED).count());
    }

    private IssueInvoiceCommand validCommand() {
        return IssueInvoiceCommand.builder()
                .number("INV-" + UUID.randomUUID())
                .issuerDocument("12345678901")
                .payerDocument("10987654321")
                .amount(new BigDecimal("100.00"))
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .build();
    }
}
