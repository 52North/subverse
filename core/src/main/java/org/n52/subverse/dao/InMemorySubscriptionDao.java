/*
 * Copyright (C) 2016-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.subverse.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.joda.time.DateTime;
import org.n52.subverse.subscription.SubscribeOptions;
import org.n52.subverse.subscription.Subscription;
import org.n52.subverse.subscription.UnknownSubscriptionException;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
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
    public synchronized Subscription deleteSubscription(String subscriptionId) throws UnknownSubscriptionException {
        if (!this.storage.containsKey(subscriptionId)) {
            throw new UnknownSubscriptionException("Unknown Subscription id: "+subscriptionId);
        }

        return this.storage.remove(subscriptionId);
    }

    @Override
    public void updateTerminationTime(Subscription sub, DateTime terminationTime) {
        SubscribeOptions opts = sub.getOptions();
        SubscribeOptions newOpts = new SubscribeOptions(opts.getPublicationIdentifier(),
                Optional.of(terminationTime),
                opts.getFilter(),
                opts.getFilterLanguageId(),
                opts.getDeliveryDefinition(),
                opts.getDeliveryParameters(),
                opts.getContentType());

        sub.updateOptions(newOpts);

        this.storage.put(sub.getId(), sub);
    }

}
