package org.n52.subverse.core.jms;

import javax.jms.Destination;

/**
 *
 */
public interface DestinationFactory {

    Destination createDestination(String name);

}
