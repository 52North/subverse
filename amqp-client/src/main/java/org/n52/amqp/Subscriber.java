
package org.n52.amqp;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@FunctionalInterface
public interface Subscriber {

    void receive(Object message);
    
}
