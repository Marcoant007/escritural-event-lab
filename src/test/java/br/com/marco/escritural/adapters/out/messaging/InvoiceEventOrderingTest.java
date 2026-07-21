package br.com.marco.escritural.adapters.out.messaging;

import br.com.marco.escritural.adapters.out.messaging.dto.InvoiceEventMessage;
import br.com.marco.escritural.application.dto.input.IssueInvoiceCommand;
import br.com.marco.escritural.application.ports.in.AcceptInvoiceUseCase;
import br.com.marco.escritural.application.ports.in.IssueInvoiceUseCase;
import br.com.marco.escritural.application.ports.in.PresentInvoiceUseCase;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class InvoiceEventOrderingTest {
    @Inject
    IssueInvoiceUseCase issueInvoice;
    @Inject
    PresentInvoiceUseCase presentInvoice;
    @Inject
    AcceptInvoiceUseCase acceptInvoice;
    @Inject
    ObjectMapper objectMapper;
    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Test
    void shouldPreserveEventOrderForSameInvoice() {
        Invoice invoice = issueInvoice.execute(validCommand());
        presentInvoice.present(invoice.getId());
        acceptInvoice.accept(invoice.getId());

        ConsumerTask<String, String> records = companion.consumeStrings()
                .fromTopics("duplicata-events", Duration.ofSeconds(5))
                .awaitCompletion(Duration.ofSeconds(10));

        List<String> eventTypesInArrivalOrder = records.getRecords().stream()
                .filter(record -> invoice.getId().toString().equals(record.key()))
                .sorted(Comparator.comparingLong(ConsumerRecord::offset))
                .map(record -> readEventType(record.value()))
                .toList();

        assertEquals(List.of("InvoiceIssued", "InvoicePresented", "InvoiceAccepted"), eventTypesInArrivalOrder);
    }

    private String readEventType(String json) {
        try {
            return objectMapper.readValue(json, InvoiceEventMessage.class).eventType();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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