package br.com.marco.escritural.domain.exception;

import br.com.marco.escritural.domain.model.enums.InvoiceStatus;

public class InvalidStatusTransitionException extends BusinessRuleException {
    public InvalidStatusTransitionException(InvoiceStatus current, InvoiceStatus target) {
        super("Cannot change invoice status from %s to %s".formatted(current, target));
    }
}
