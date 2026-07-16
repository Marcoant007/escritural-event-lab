package br.com.marco.escritural.application.exception;

import java.util.UUID;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(UUID id) {
        super("Invoice %s was not found".formatted(id));
    }
}
