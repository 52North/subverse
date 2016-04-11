package org.n52.amqp;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.messenger.Messenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Connection {
    
    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);
    
    private final String username;
    private final String password;
    private final URI remoteURI;
    private boolean open = true;
    
    public Connection(URI remoteURI, String username, String password) {
        this.username = username;
        this.password = password;
        this.remoteURI = remoteURI;
    }

    public boolean isOpen() {
        return open;
    }

    public URI getRemoteURI() {
        return remoteURI;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    public void close() {
        this.open = false;
        LOG.info("Closed connection for client {}", remoteURI);
    }
    
    public Publisher createPublisher() throws PublisherCreationFailedException {
        try {
            return new Publisher(this);
        } catch (Exception ex) {
            throw new PublisherCreationFailedException("Could not create publisher", ex);
        }
    }
    
    public SubscriptionReference subscribe(Subscriber s) throws SubscriptionFailedException {
        Objects.requireNonNull(s);
        try {
            return spawnReceiverThread(s);
        } catch (Exception ex) {
            throw new SubscriptionFailedException("Could not establish receiver", ex);
        }
    }
    
    private SubscriptionReference spawnReceiverThread(Subscriber s) {
        SubscriptionReference ref = new SubscriptionReference();
        
        new Thread(() -> {
            Messenger messenger = null;
            while (this.open && ref.isActive()) {
                messenger = Messenger.Factory.create();
                try {
                    messenger.start();
                } catch (IOException ex) {
                    LOG.warn("Could not spawn subscriber", ex);
                    return;
                }
                messenger.subscribe(this.remoteURI.toString());
                
                while (!messenger.stopped()) {
                    LOG.debug("starting recv()");
                    messenger.recv();
                    while (messenger.incoming() > 0) {
                        LOG.debug("starting recv() loop");
                        Message msg = messenger.get();
                        Section body = msg.getBody();
                        if (body instanceof AmqpValue) {
                            s.receive(((AmqpValue) body).getValue());
                        }
                    }
                }
            }
            
            if (messenger != null && !messenger.stopped()) {
                messenger.stop();
            }
        }).start();
        
        return ref;
    }
    
}
