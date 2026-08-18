package br.com.marco.escritural.hub;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

public class IbmMqTestResource implements QuarkusTestResourceLifecycleManager {

    private GenericContainer<?> container;

    @Override
    public Map<String, String> start() {
        container = new GenericContainer<>(DockerImageName.parse("icr.io/ibm-messaging/mq:9.4.5.1-r1"))
                .withEnv("LICENSE", "accept")
                .withEnv("MQ_QMGR_NAME", "QM1")
                .withEnv("MQ_APP_PASSWORD", "passw0rd")
                .withExposedPorts(1414, 9443)
                .waitingFor(Wait.forLogMessage(".*Started queue manager.*\\n", 1));
        container.start();

        Map<String, String> config = new HashMap<>();
        config.put("ibmmq.host", container.getHost());
        config.put("ibmmq.port", String.valueOf(container.getMappedPort(1414)));
        return config;
    }

    @Override
    public void stop() {
        if (container != null) {
            container.stop();
        }
    }
}
