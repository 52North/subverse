package org.n52.amqp;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class AmqpConnectionCreationFailedException extends Exception {

    public AmqpConnectionCreationFailedException(String message) {
        super(message);
    }

    public AmqpConnectionCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmqpConnectionCreationFailedException(Throwable cause) {
        super(cause);
    }

}
