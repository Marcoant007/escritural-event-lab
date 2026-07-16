package br.com.marco.escritural.application.exception;

public class DuplicateInvoiceException extends RuntimeException {
    public DuplicateInvoiceException(String number) {
        super("An invoice with number %s already exists".formatted(number));
    }
}
