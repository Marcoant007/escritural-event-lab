package br.com.marco.escritural.hub;

import jakarta.enterprise.inject.spi.CDI;
import org.apache.camel.builder.RouteBuilder;

public class BankForwardRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        HubEventBus hubEventBus = CDI.current().select(HubEventBus.class).get();

        from("kafka:duplicata-recebida?brokers={{kafka.bootstrap.servers}}")
                .routeId("kafka-to-ibmmq-banco-b")
                .log("Recebido do banco A (Kafka): ${body}")
                .process(exchange -> hubEventBus.publish("KAFKA", "RECEBIDO_BANCO_A", exchange.getIn().getBody(String.class)))
                .to("jms:queue:DEV.QUEUE.2?connectionFactory=#ibmMqConnectionFactory")
                .process(exchange -> hubEventBus.publish("IBM_MQ", "ENCAMINHADO_BANCO_B", exchange.getIn().getBody(String.class)));
    }
}
