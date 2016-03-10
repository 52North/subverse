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
package org.n52.subverse.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.n52.subverse.subscription.Subscription;
import org.n52.subverse.subscription.UnknownSubscriptionException;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class InMemorySubscriptionDao implements SubscriptionDao {

    private final Map<String, Subscription> storage = new HashMap<>();

    @Override
    public synchronized void storeSubscription(Subscription sub) {
        this.storage.put(sub.getId(), sub);
    }

    @Override
    public synchronized Stream<Subscription> getAllSubscriptions() {
        return Collections.unmodifiableCollection(this.storage.values()).stream();
    }

    @Override
    public synchronized Optional<Subscription> getSubscription(String id) {
        return Optional.ofNullable(this.storage.get(id));
    }

    @Override
    public synchronized void deleteSubscription(String subscriptionId) throws UnknownSubscriptionException {
        if (!this.storage.containsKey(subscriptionId)) {
            throw new UnknownSubscriptionException("Unknown Subscription id: "+subscriptionId);
        }
        
        this.storage.remove(subscriptionId);
    }

}
