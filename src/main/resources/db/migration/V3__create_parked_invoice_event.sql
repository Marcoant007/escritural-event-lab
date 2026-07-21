CREATE TABLE parked_invoice_event (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    event_version INT NOT NULL,
    status VARCHAR(60) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    correlation_id UUID NOT NULL,
    parked_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_parked_invoice_event_aggregate_id ON parked_invoice_event (aggregate_id);
