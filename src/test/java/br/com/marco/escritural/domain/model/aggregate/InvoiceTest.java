package br.com.marco.escritural.domain.model.aggregate;

import br.com.marco.escritural.domain.exception.BusinessRuleException;
import br.com.marco.escritural.domain.exception.InvalidStatusTransitionException;
import br.com.marco.escritural.domain.model.enums.InvoiceStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceTest {
    @Test
    void shouldIssueAValidInvoice() {
        Invoice invoice = validInvoice();
        assertNotNull(invoice.getId());
        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());
    }

    @Test
    void shouldRejectNonPositiveAmount() {
        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> invoiceBuilder().amount(BigDecimal.ZERO).build());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldRejectDueDateBeforeIssueDate() {
        assertThrows(BusinessRuleException.class,
                () -> invoiceBuilder().dueDate(LocalDate.of(2026, 1, 9)).build());
    }

    @Test
    void shouldPresentAndAcceptInvoice() {
        Invoice invoice = validInvoice();
        invoice.present();
        invoice.accept();
        assertEquals(InvoiceStatus.ACCEPTED, invoice.getStatus());
    }

    @Test
    void shouldPresentAndRejectInvoice() {
        Invoice invoice = validInvoice();
        invoice.present();
        invoice.reject();
        assertEquals(InvoiceStatus.REJECTED, invoice.getStatus());
    }

    @Test
    void shouldRejectInvalidTransition() {
        Invoice invoice = validInvoice();
        assertThrows(InvalidStatusTransitionException.class, invoice::accept);
        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());
    }

    private Invoice validInvoice() { return invoiceBuilder().build(); }

    private Invoice.IssueBuilder invoiceBuilder() {
        return Invoice.builder().number("INV-001").issuerDocument("12345678901")
                .payerDocument("10987654321").amount(new BigDecimal("100.00"))
                .issueDate(LocalDate.of(2026, 1, 10)).dueDate(LocalDate.of(2026, 2, 10));
    }
}
