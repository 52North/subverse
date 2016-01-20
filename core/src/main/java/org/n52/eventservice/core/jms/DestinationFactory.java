package org.n52.eventservice.core.jms;

import javax.jms.Destination;

/**
 *
 */
public interface DestinationFactory {

    Destination createDestination(String name);
    
}
