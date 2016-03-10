/*
 * Copyright 2016 52°North.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.subverse.delivery.wsn;

import java.net.MalformedURLException;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.DeliveryProvider;
import org.n52.subverse.delivery.UnsupportedDeliveryDefinitionException;
import org.springframework.stereotype.Component;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
@Component
public class WsnDeliveryProvider implements DeliveryProvider {

    private static final String IDENTIFIER = "http://docs.oasis-open.org/wsn/b-2/NotificationConsumer";
    private static final String ABSTRACT = "WS-BaseNotification allows a NotificationConsumer to receive a Notification in one of two forms:\n" +
            "\n 1. The NotificationConsumer MAY simply receive the \"raw\" Notification (i.e. the application-specific content).\n" +
            "\n 2. The NotificationConsumer MAY receive the Notification data as a Notify message as described below.";

    @Override
    public DeliveryEndpoint createDeliveryEndpoint(DeliveryDefinition def) throws UnsupportedDeliveryDefinitionException {
        try {
            return new WsnConsumerEndpoint(def.getLocation());
        } catch (MalformedURLException ex) {
            throw new UnsupportedDeliveryDefinitionException("Illegal URL provided", ex);
        }
    }

    @Override
    public boolean supportsDeliveryIdentifier(String id) {
        return IDENTIFIER.equals(id);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String getAbstract() {
        return ABSTRACT;
    }

}
