
package org.n52.amqp;

import java.io.IOException;
import java.util.UUID;
import org.apache.activemq.transport.amqp.client.AmqpMessage;
import org.apache.activemq.transport.amqp.client.AmqpSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Publisher {
    
    private static final Logger LOG = LoggerFactory.getLogger(Publisher.class);
    private final AmqpSender sender;
    private final String id = UUID.randomUUID().toString();
    private int messageCount = 0;
    
    protected Publisher(AmqpSender sender) {
        this.sender = sender;
    }
    
    public void publish(CharSequence msg) {
        LOG.debug("publishing message to target '{}'", sender.getAddress());
        if (this.sender.getSession().getConnection().isOpen()) {
            AmqpMessage message = new AmqpMessage();
            message.setMessageId(id+"_"+messageCount++);
            message.setText(msg.toString());
            try {
                sender.send(message);
            } catch (IOException ex) {
                LOG.warn("Could not send message", ex);
            }
        }
        else {
            LOG.warn("Cannot send message. Connection already closed");
        }
    }
    
}
