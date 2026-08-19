package br.com.marco.escritural.hub;

import jakarta.enterprise.inject.spi.CDI;
import org.apache.camel.builder.RouteBuilder;

import java.time.Instant;

public class HubRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        HubEventBus hubEventBus = CDI.current().select(HubEventBus.class).get();

        from("kafka:hub-in?brokers={{kafka.bootstrap.servers}}")
                .routeId("hub-in-to-out")
                .log("Hub recebeu do Kafka: ${body}")
                .process(exchange -> hubEventBus.publish("KAFKA", "RECEBIDO", exchange.getIn().getBody(String.class)))
                .process(exchange -> {
                    String payload = exchange.getIn().getBody(String.class);
                    exchange.getIn().setBody(new HubMessage("KAFKA", payload, Instant.now().toString()));
                })
                .marshal().json()
                .process(exchange -> hubEventBus.publish("KAFKA", "PUBLICADO_HUB_OUT", exchange.getIn().getBody(String.class)))
                .to("kafka:hub-out?brokers={{kafka.bootstrap.servers}}");
    }
}
