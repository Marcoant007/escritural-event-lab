CREATE TABLE invoice (
    id UUID PRIMARY KEY,
    number VARCHAR(50) NOT NULL,
    issuer_document VARCHAR(20) NOT NULL,
    payer_document VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_invoice_number UNIQUE (number),
    CONSTRAINT ck_invoice_positive_amount CHECK (amount > 0),
    CONSTRAINT ck_invoice_due_date CHECK (due_date >= issue_date)
);
