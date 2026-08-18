package br.com.marco.escritural.hub;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;
import org.apache.camel.ConsumerTemplate;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
@QuarkusTestResource(IbmMqTestResource.class)
class BankForwardRouteTest {

    @Inject
    ConsumerTemplate consumerTemplate;
    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Test
    void shouldForwardMessageFromKafkaToIbmMqQueue() {
        String payload = "duplicata teste " + UUID.randomUUID();

        companion.produceStrings().fromRecords(
                KafkaCompanion.record("duplicata-recebida", null, payload)
        );

        String received = consumerTemplate.receiveBody(
                "jms:queue:DEV.QUEUE.2?connectionFactory=#ibmMqConnectionFactory", 20000, String.class);

        assertEquals(payload, received);
    }
}
