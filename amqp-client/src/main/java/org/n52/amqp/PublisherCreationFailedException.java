
package org.n52.amqp;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class PublisherCreationFailedException extends Exception {

    public PublisherCreationFailedException(String message) {
        super(message);
    }

    public PublisherCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
