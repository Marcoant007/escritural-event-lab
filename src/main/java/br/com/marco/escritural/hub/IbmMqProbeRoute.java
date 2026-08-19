package br.com.marco.escritural.hub;

import jakarta.enterprise.inject.spi.CDI;
import org.apache.camel.builder.RouteBuilder;

import java.time.Instant;

public class IbmMqProbeRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        HubEventBus hubEventBus = CDI.current().select(HubEventBus.class).get();

        from("jms:queue:DEV.QUEUE.1?connectionFactory=#ibmMqConnectionFactory")
                .routeId("ibmmq-to-hub-out")
                .log("Hub recebeu do IBM MQ: ${body}")
                .process(exchange -> hubEventBus.publish("IBM_MQ", "RECEBIDO", exchange.getIn().getBody(String.class)))
                .process(exchange -> {
                    String payload = exchange.getIn().getBody(String.class);
                    exchange.getIn().setBody(new HubMessage("IBM_MQ", payload, Instant.now().toString()));
                })
                .marshal().json()
                .process(exchange -> hubEventBus.publish("IBM_MQ", "PUBLICADO_HUB_OUT", exchange.getIn().getBody(String.class)))
                .to("kafka:hub-out?brokers={{kafka.bootstrap.servers}}");
    }
}
