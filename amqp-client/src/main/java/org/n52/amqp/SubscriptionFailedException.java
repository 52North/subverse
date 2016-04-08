
package org.n52.amqp;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscriptionFailedException extends Exception {

    public SubscriptionFailedException(String message) {
        super(message);
    }

    public SubscriptionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
