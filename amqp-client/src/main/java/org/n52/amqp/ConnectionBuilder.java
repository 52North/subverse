package org.n52.amqp;

import java.net.URI;
import java.util.Objects;
import org.apache.activemq.transport.amqp.client.AmqpClient;
import org.apache.activemq.transport.amqp.client.AmqpConnection;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class ConnectionBuilder {
    
    private final URI address;
    private String user;
    private String password;
    
    
    private ConnectionBuilder(URI address) {
        this.address = address;
    }
    
    public static ConnectionBuilder create(URI address) throws AmqpConnectionCreationFailedException {
        Objects.requireNonNull(address, "Please provide an AMQP broker address");
        
        String scheme = address.getScheme();
        if (!scheme.equals("amqp") && !scheme.equals("amqps") && !scheme.equals("tcp")) {
            throw new AmqpConnectionCreationFailedException(String.format("%s is not a valid amqp address", address));
        }
        return new ConnectionBuilder(address);
    }
    
    public ConnectionBuilder user(String user) {
        this.user = user;
        return this;
    }
    
    public ConnectionBuilder password(String pwd) {
        this.password = pwd;
        return this;
    }
    
    public Connection build() throws AmqpConnectionCreationFailedException {
        if (this.user != null) {
            if (this.password == null) {
                throw new AmqpConnectionCreationFailedException("If a user is provided, a password is required as well");
            }
        }
        
        try {
            AmqpClient client = new AmqpClient(this.address, this.user, this.password);
            AmqpConnection conn = client.connect();
            return new Connection(this.address, this.user, this.password);
        } catch (Exception ex) {
            throw new AmqpConnectionCreationFailedException(ex);
        }
    }
    
}
