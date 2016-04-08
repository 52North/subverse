package org.n52.amqp;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.activemq.transport.amqp.client.AmqpClient;
import org.apache.activemq.transport.amqp.client.AmqpConnection;
import org.apache.activemq.transport.amqp.client.AmqpMessage;
import org.apache.activemq.transport.amqp.client.AmqpReceiver;
import org.apache.activemq.transport.amqp.client.AmqpSender;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Connection {
    
    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);
    
    private final AmqpClient client;
    private final AmqpConnection connection;
    private boolean open = true;
    
    protected Connection(AmqpClient client, AmqpConnection connection) {
        this.client = client;
        this.connection = connection;
    }
    
    public void close() {
        this.open = false;
        this.connection.close();
        LOG.info("Closed connection for client {}", this.client.getRemoteURI());
    }
    
    public SubscriptionReference subscribeQueue(String queue, Subscriber s) throws SubscriptionFailedException, InvalidTargetAddressException {
        assertStructure(queue, "queue://");
        return subscribe(queue, s);
    }
    
    public SubscriptionReference subscribeTopic(String topic, Subscriber s) throws SubscriptionFailedException, InvalidTargetAddressException {
        assertStructure(topic, "topic://");
        return subscribe(topic, s);
    }

    private void assertStructure(String target, String expected) throws InvalidTargetAddressException {
        Objects.requireNonNull(target);
        if (!target.startsWith(expected)) {
            throw new InvalidTargetAddressException(String.format("Expected target address has to start with '%s'", expected));
        }
    }
    
    public Publisher createPublisherForTopic(String topic) throws PublisherCreationFailedException, InvalidTargetAddressException {
        assertStructure(topic, "topic://");
        return createPublisher(topic);
    }
    
    public Publisher createPublisherForQueue(String queue) throws PublisherCreationFailedException, InvalidTargetAddressException {
        assertStructure(queue, "queue://");
        return createPublisher(queue);
    }
    
    private Publisher createPublisher(String target) throws PublisherCreationFailedException {
        try {
            AmqpSender sender = this.connection.createSession().createSender(target);
            return new Publisher(sender);
        } catch (Exception ex) {
            throw new PublisherCreationFailedException("Could not create publisher", ex);
        }
    }

    private SubscriptionReference subscribe(String target, Subscriber s) throws SubscriptionFailedException {
        Objects.requireNonNull(s);
        try {
            AmqpReceiver receiver = this.connection.createSession().createReceiver(target);
            return spawnReceiverThread(receiver, s);
        } catch (Exception ex) {
            throw new SubscriptionFailedException("Could not establish receiver", ex);
        }
    }
    
    private SubscriptionReference spawnReceiverThread(AmqpReceiver receiver, Subscriber s) {
        SubscriptionReference ref = new SubscriptionReference();
        
        new Thread(() -> {
            int i = 0;
            while (open && ref.isActive()) {
                try {
                    receiver.flow(++i);
                    AmqpMessage msg = receiver.receive(10, TimeUnit.SECONDS);
                    if (msg != null) {
                        Section val = msg.getWrappedMessage().getBody();
                        if (val instanceof AmqpValue) {
                            LOG.debug("Received message: {}", val);
                            s.receive(((AmqpValue) val).getValue());
                        }
                    }
                } catch (Exception ex) {
                    LOG.warn(ex.getMessage(), ex);
                }
            }
        }).start();
        
        return ref;
    }
    
}
