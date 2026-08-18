package br.com.marco.escritural.hub;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;
import org.apache.camel.ProducerTemplate;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
@QuarkusTestResource(IbmMqTestResource.class)
class IbmMqRouteTest {

    @Inject
    ObjectMapper objectMapper;
    @Inject
    ProducerTemplate producerTemplate;
    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Test
    void shouldNormalizeIbmMqMessageIntoHubEnvelope() {
        String payload = "mensagem de teste ibm mq " + UUID.randomUUID();

        producerTemplate.sendBody("jms:queue:DEV.QUEUE.1?connectionFactory=#ibmMqConnectionFactory", payload);

        ConsumerTask<String, String> records = companion.consumeStrings()
                .fromTopics("hub.out", Duration.ofSeconds(10))
                .awaitCompletion(Duration.ofSeconds(30));

        List<HubMessage> matching = records.getRecords().stream()
                .map(this::readEnvelope)
                .filter(envelope -> payload.equals(envelope.payload()))
                .toList();

        assertEquals(1, matching.size());
        HubMessage envelope = matching.get(0);
        assertEquals("IBM_MQ", envelope.source());
        assertNotNull(envelope.receivedAt());
    }

    private HubMessage readEnvelope(ConsumerRecord<String, String> record) {
        try {
            return objectMapper.readValue(record.value(), HubMessage.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
