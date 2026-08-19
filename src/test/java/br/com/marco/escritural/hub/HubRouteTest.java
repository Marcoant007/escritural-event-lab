package br.com.marco.escritural.hub;

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

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class HubRouteTest {

    @Inject
    ObjectMapper objectMapper;
    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Test
    void shouldNormalizeKafkaMessageIntoHubEnvelope() throws Exception {
        String key = UUID.randomUUID().toString();
        String payload = "mensagem de teste";

        companion.produceStrings().fromRecords(
                KafkaCompanion.record("hub-in", key, payload)
        );

        ConsumerTask<String, String> records = companion.consumeStrings()
                .fromTopics("hub-out", Duration.ofSeconds(5))
                .awaitCompletion(Duration.ofSeconds(15));

        List<ConsumerRecord<String, String>> matching = records.getRecords().stream()
                .filter(record -> key.equals(record.key()))
                .toList();

        assertEquals(1, matching.size());

        HubMessage envelope = objectMapper.readValue(matching.get(0).value(), HubMessage.class);
        assertEquals("KAFKA", envelope.source());
        assertEquals(payload, envelope.payload());
        assertNotNull(envelope.receivedAt());
    }
}
