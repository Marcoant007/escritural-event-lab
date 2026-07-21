package br.com.marco.escritural.adapters.in.messaging;

import br.com.marco.escritural.adapters.out.messaging.dto.InvoiceEventMessage;
import br.com.marco.escritural.application.dto.input.IssueInvoiceCommand;
import br.com.marco.escritural.application.dto.input.NotifyInvoiceEventCommand;
import br.com.marco.escritural.application.ports.in.IssueInvoiceUseCase;
import br.com.marco.escritural.application.usecase.NotifyInvoiceEventUseCaseFake;
import br.com.marco.escritural.domain.model.aggregate.Invoice;
import br.com.marco.escritural.domain.model.enums.InvoiceStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class InvoiceEventConsumerTest {
    @Inject
    IssueInvoiceUseCase issueInvoice;
    @Inject
    NotifyInvoiceEventUseCaseFake notifyFake;
    @InjectKafkaCompanion
    KafkaCompanion companion;
    @Inject
    ObjectMapper objectMapper;

    @Test
    void shouldNotifyWhenInvoiceIssuedEventIsConsumed() {
        Invoice invoice = issueInvoice.execute(validCommand());

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertTrue(receivedFor(invoice.getId()).isPresent()));

        NotifyInvoiceEventCommand command = receivedFor(invoice.getId()).orElseThrow();
        assertEquals(InvoiceStatus.ISSUED, command.invoiceStatus());
        assertNotNull(command.occurredAt());
    }

    @Test
    void shouldSendToDeadLetterQueueWhenStatusIsInvalid() throws JsonProcessingException {
        UUID aggregateId = UUID.randomUUID();
        InvoiceEventMessage malformedMessage = new InvoiceEventMessage(
                UUID.randomUUID(),
                "InvoiceIssued",
                1,
                aggregateId,
                Instant.now(),
                UUID.randomUUID(),
                "BOGUS_STATUS"
        );
        String json = objectMapper.writeValueAsString(malformedMessage);

        companion.produceStrings().fromRecords(
                KafkaCompanion.record("duplicata-events", aggregateId.toString(), json)
        );

        ConsumerTask<String, String> dlqRecords = companion.consumeStrings()
                .fromTopics("duplicata-events-dlq", Duration.ofSeconds(5))
                .awaitCompletion(Duration.ofSeconds(10));

        ConsumerRecord<String, String> dlqRecord = dlqRecords.getRecords().stream()
                .filter(request -> aggregateId.toString().equals(request.key()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Nenhuma mensagem chegou na DLQ pra " + aggregateId));
    }

    @Test
    void shouldKeepProcessingSubsequentEventsWhenAnEarlierOneIsParked() throws JsonProcessingException {
        UUID aggregateId = UUID.randomUUID();
        InvoiceEventMessage malformedMessage = new InvoiceEventMessage(
                UUID.randomUUID(),
                "InvoiceIssued",
                1,
                aggregateId,
                Instant.now(),
                UUID.randomUUID(),
                "BOGUS_STATUS"
        );
        String json = objectMapper.writeValueAsString(malformedMessage);

        companion.produceStrings().fromRecords(
                KafkaCompanion.record("duplicata-events", aggregateId.toString(), json)
        );

        Invoice invoice = issueInvoice.execute(validCommand());
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertTrue(receivedFor(invoice.getId()).isPresent()));

        NotifyInvoiceEventCommand command = receivedFor(invoice.getId()).orElseThrow();
        assertEquals(InvoiceStatus.ISSUED, command.invoiceStatus());
        assertNotNull(command.occurredAt());

    }

    private Optional<NotifyInvoiceEventCommand> receivedFor(UUID invoiceId) {
        return notifyFake.received().stream()
                .filter(command -> command.invoiceId().equals(invoiceId))
                .findFirst();
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
