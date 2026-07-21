package br.com.marco.escritural.adapters.in.messaging;

import br.com.marco.escritural.adapters.out.messaging.dto.InvoiceEventMessage;
import br.com.marco.escritural.adapters.out.persistence.entity.ParkedInvoiceEventEntity;
import br.com.marco.escritural.adapters.out.persistence.repository.ParkedInvoiceEventPanacheRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class InvoiceParkingLotConsumerTest {
    @Inject
    ParkedInvoiceEventPanacheRepository repository;
    @Inject
    ObjectMapper objectMapper;
    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Test
    void shouldPersistParkedEventWhenMessageFailsProcessing() throws JsonProcessingException {
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        InvoiceEventMessage malformedMessage = new InvoiceEventMessage(
                eventId,
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

        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertTrue(findById(eventId).isPresent()));

        ParkedInvoiceEventEntity parked = findById(eventId).orElseThrow();
        assertEquals(aggregateId, parked.getAggregateId());
        assertEquals("InvoiceIssued", parked.getEventType());
        assertEquals("BOGUS_STATUS", parked.getStatus());
    }

    @ActivateRequestContext
    Optional<ParkedInvoiceEventEntity> findById(UUID eventId) {
        return repository.findByIdOptional(eventId);
    }
}
