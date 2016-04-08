
package org.n52.amqp;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscriptionReference {

    private boolean active = true;
    
    public void unsubscribe() {
        this.active = false;
    }
    
    public boolean isActive() {
        return this.active;
    }
    
}
