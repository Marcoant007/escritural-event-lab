package br.com.marco.escritural.hub;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.apache.camel.ProducerTemplate;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Path("/hub")
public class HubDebugResource {

    @Inject
    HubEventBus hubEventBus;
    @Inject
    ProducerTemplate producerTemplate;

    @GET
    @Path("/events")
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<HubEventBus.HubEvent> events() {
        return hubEventBus.stream();
    }

    @POST
    @Path("/publish")
    @Consumes(MediaType.APPLICATION_JSON)
    public void publish(PublishRequest request) {
        switch (request.target()) {
            case "HUB_IBM_MQ" ->
                    producerTemplate.sendBody("jms:queue:DEV.QUEUE.1?connectionFactory=#ibmMqConnectionFactory", request.message());
            case "BANCO_A_KAFKA" ->
                    producerTemplate.sendBody("kafka:duplicata-recebida?brokers={{kafka.bootstrap.servers}}", request.message());
            default ->
                    producerTemplate.sendBody("kafka:hub-in?brokers={{kafka.bootstrap.servers}}", request.message());
        }
    }

    public record PublishRequest(String target, String message) {
    }
}
