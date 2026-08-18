package br.com.marco.escritural.hub;

import com.ibm.msg.client.jakarta.jms.JmsConnectionFactory;
import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class IbmMqConnectionFactoryProducer {

    @ConfigProperty(name = "ibmmq.host") String host;
    @ConfigProperty(name = "ibmmq.port") int port;
    @ConfigProperty(name = "ibmmq.channel") String channel;
    @ConfigProperty(name = "ibmmq.queue-manager") String queueManager;
    @ConfigProperty(name = "ibmmq.user") String user;
    @ConfigProperty(name = "ibmmq.password") String password;

    @Produces
    @Named("ibmMqConnectionFactory")
    @ApplicationScoped
    public ConnectionFactory createConnectionFactory() throws JMSException {
        JmsFactoryFactory jmsConnectionFactory = JmsFactoryFactory.getInstance(WMQConstants.JAKARTA_WMQ_PROVIDER);
        JmsConnectionFactory connectionFactory = (JmsConnectionFactory) jmsConnectionFactory.createConnectionFactory();
        connectionFactory.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
        connectionFactory.setIntProperty(WMQConstants.WMQ_PORT, port);
        connectionFactory.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
        connectionFactory.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
        connectionFactory.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, queueManager);
        connectionFactory.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "escritural-event-lab-hub");
        connectionFactory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
        connectionFactory.setStringProperty(WMQConstants.USERID, user);
        connectionFactory.setStringProperty(WMQConstants.PASSWORD, password);

        return connectionFactory;
    }
}
