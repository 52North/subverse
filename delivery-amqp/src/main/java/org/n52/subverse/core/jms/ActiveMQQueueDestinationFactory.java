package org.n52.subverse.core.jms;

import javax.jms.Destination;
import org.apache.activemq.command.ActiveMQQueue;

/**
 *
 */
public class ActiveMQQueueDestinationFactory implements DestinationFactory {

    public Destination createDestination(String name) {
        return new ActiveMQQueue(name);
    }

}
