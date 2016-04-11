
package org.n52.amqp;

import java.io.IOException;
import java.util.UUID;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.messenger.Messenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Publisher {
    
    private static final Logger LOG = LoggerFactory.getLogger(Publisher.class);
    private final Connection connection;
    private final String id = UUID.randomUUID().toString();
    private Messenger messenger;
    
    protected Publisher(Connection c) {
        this.connection = c;
    }
    
    public void publish(CharSequence msg) {
        publish(msg, null);
    }
    
    public void publish(CharSequence msg, String subject) {
        LOG.debug("publishing message to target '{}'", connection.getRemoteURI());
        if (this.connection.isOpen()) {
            try {
                synchronized (this) {
                    if (this.messenger == null || this.messenger.stopped()) {
                        this.messenger = Messenger.Factory.create(id);
                        messenger.start();
                    }
                }
                
                Message message = Message.Factory.create();
                message.setAddress(this.connection.getRemoteURI().toString());
                
                if (subject != null) {
                    message.setSubject(subject);
                }
                
                message.setBody(new AmqpValue(msg));
                
                synchronized (this) {
                    messenger.put(message);
                    messenger.send();
                }
            } catch (IOException ex) {
                LOG.warn("Could not send message", ex);
            }
        }
        else {
            LOG.warn("Cannot send message. Connection already closed");
        }
    }
    
}
