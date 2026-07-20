package br.com.marco.escritural.adapters.in.messaging;

import br.com.marco.escritural.adapters.out.messaging.dto.InvoiceEventMessage;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class InvoiceEventMessageDeserializer extends ObjectMapperDeserializer<InvoiceEventMessage> {
    public InvoiceEventMessageDeserializer() {
        super(InvoiceEventMessage.class);
    }
}
