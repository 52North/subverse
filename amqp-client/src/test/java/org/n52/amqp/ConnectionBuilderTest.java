
package org.n52.amqp;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class ConnectionBuilderTest {

    @Test
    public void testCreation() throws AmqpConnectionCreationFailedException, URISyntaxException {
        try {
            ConnectionBuilder.create(new URI("amqp://localhost")).build();
        }
        catch (AmqpConnectionCreationFailedException e) {
            if (e.getCause() instanceof java.net.ConnectException) {
                //probably no broker running
            }
            else {
                throw e;
            }
        }
    }
    
    @Test (expected = AmqpConnectionCreationFailedException.class)
    public void testCreationWithUserWithoutPassword() throws AmqpConnectionCreationFailedException, URISyntaxException {
        try {
            ConnectionBuilder.create(new URI("amqp://localhost")).user("test").build();
        }
        catch (AmqpConnectionCreationFailedException e) {
            if (e.getCause() instanceof java.net.ConnectException) {
                //probably no broker running
            }
            else {
                throw e;
            }
        }
    }
    
    @Test (expected = AmqpConnectionCreationFailedException.class)
    public void testCreationWrongScheme() throws AmqpConnectionCreationFailedException, URISyntaxException {
        try {
            ConnectionBuilder.create(new URI("http://localhost")).build();
        }
        catch (AmqpConnectionCreationFailedException e) {
            if (e.getCause() instanceof java.net.ConnectException) {
                //probably no broker running
            }
            else {
                throw e;
            }
        }
    }
}
