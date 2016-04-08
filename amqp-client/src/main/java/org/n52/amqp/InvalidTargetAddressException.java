
package org.n52.amqp;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class InvalidTargetAddressException extends Exception {

    public InvalidTargetAddressException(String message) {
        super(message);
    }

    public InvalidTargetAddressException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
