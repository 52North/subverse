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
package org.n52.subverse.engine;

import org.n52.subverse.subscription.UnknownSubscriptionException;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.subscription.Subscription;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public interface FilterEngine {

    void filterMessage(Object message);

    void register(Subscription result, DeliveryEndpoint deliveryEndpoint)
            throws SubscriptionRegistrationException;

    void removeSubscription(String subscriptionId) throws UnknownSubscriptionException;

}
