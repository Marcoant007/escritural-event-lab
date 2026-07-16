package br.com.marco.escritural.domain.model.aggregate;

import br.com.marco.escritural.domain.event.InvoiceAccepted;
import br.com.marco.escritural.domain.event.InvoiceIssued;
import br.com.marco.escritural.domain.event.InvoicePresented;
import br.com.marco.escritural.domain.event.InvoiceRejected;
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
    void shouldReturnDomainEventWhenIssuing() {
        Invoice invoice = validInvoice();
        InvoiceIssued event = invoice.issuedEvent();
        assertNotNull(event.eventId());
        assertEquals(invoice.getId(), event.invoiceId());
        assertEquals(invoice.getCreatedAt(), event.occurredAt());
        assertEquals(InvoiceStatus.ISSUED, event.status());
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
    void shouldReturnDomainEventWhenPresenting() {
        Invoice invoice = validInvoice();
        InvoicePresented event = invoice.present();
        assertNotNull(event.eventId());
        assertEquals(invoice.getId(), event.invoiceId());
        assertEquals(invoice.getUpdatedAt(), event.occurredAt());
    }

    @Test
    void shouldGenerateDifferentEventIdsForDifferentPresentations() {
        InvoicePresented first = validInvoice().present();
        InvoicePresented second = validInvoice().present();
        assertNotEquals(first.eventId(), second.eventId());
    }

    @Test
    void shouldReturnDomainEventWhenAccepting() {
        Invoice invoice = validInvoice();
        invoice.present();
        InvoiceAccepted event = invoice.accept();
        assertNotNull(event.eventId());
        assertEquals(invoice.getId(), event.invoiceId());
        assertEquals(InvoiceStatus.ACCEPTED, event.status());
    }

    @Test
    void shouldReturnDomainEventWhenRejecting() {
        Invoice invoice = validInvoice();
        invoice.present();
        InvoiceRejected event = invoice.reject();
        assertNotNull(event.eventId());
        assertEquals(invoice.getId(), event.invoiceId());
        assertEquals(InvoiceStatus.REJECTED, event.status());
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
