package br.com.marco.escritural.application.usecase;

import br.com.marco.escritural.adapters.out.messaging.dto.InvoiceEventMessage;
import br.com.marco.escritural.adapters.out.persistence.entity.InvoiceHistoryEntity;
import br.com.marco.escritural.adapters.out.persistence.repository.InvoiceHistoryPanacheRepository;
import br.com.marco.escritural.application.dto.input.IssueInvoiceCommand;
import br.com.marco.escritural.application.ports.in.AcceptInvoiceUseCase;
import br.com.marco.escritural.application.ports.in.IssueInvoiceUseCase;
import br.com.marco.escritural.application.ports.in.PresentInvoiceUseCase;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import br.com.marco.escritural.domain.model.enums.InvoiceStatus;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class AcceptInvoiceServiceTest {
    @Inject
    IssueInvoiceUseCase issueInvoice;
    @Inject
    PresentInvoiceUseCase presentInvoice;
    @Inject
    AcceptInvoiceUseCase acceptInvoice;
    @Inject
    InvoiceHistoryPanacheRepository historyRepository;
    @Inject
    ObjectMapper objectMapper;
    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Test
    void shouldRecordHistoryWhenInvoiceIsAccepted() {
        Invoice invoice = issueInvoice.execute(validCommand());
        presentInvoice.present(invoice.getId());

        acceptInvoice.accept(invoice.getId());

        List<InvoiceHistoryEntity> history = historyRepository.list("invoiceId", invoice.getId());
        assertEquals(3, history.size());
        assertEquals(1, history.stream().filter(h -> h.getStatus() == InvoiceStatus.ACCEPTED).count());
    }

    @Test
    void shouldPublishInvoiceAcceptedEventWhenInvoiceIsAccepted() throws Exception {
        Invoice invoice = issueInvoice.execute(validCommand());
        presentInvoice.present(invoice.getId());

        acceptInvoice.accept(invoice.getId());

        ConsumerTask<String, String> records = companion.consumeStrings()
                .fromTopics("duplicata-events", Duration.ofSeconds(5))
                .awaitCompletion(Duration.ofSeconds(10));

        ConsumerRecord<String, String> record = records.getRecords().stream()
                .filter(r -> invoice.getId().toString().equals(r.key()))
                .filter(r -> readEventType(r.value()).equals("InvoiceAccepted"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Nenhum InvoiceAccepted publicado para a invoice " + invoice.getId()));

        InvoiceEventMessage message = objectMapper.readValue(record.value(), InvoiceEventMessage.class);
        assertEquals(invoice.getId(), message.aggregateId());
        assertEquals(InvoiceStatus.ACCEPTED.name(), message.status());
        assertNotNull(message.eventId());
        assertNotNull(message.occurredAt());
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
