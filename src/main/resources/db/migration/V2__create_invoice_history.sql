CREATE TABLE invoice_history (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_invoice_history_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id)
);

CREATE INDEX ix_invoice_history_invoice_id ON invoice_history (invoice_id);
