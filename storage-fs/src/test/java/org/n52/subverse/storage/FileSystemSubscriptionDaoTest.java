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

package org.n52.subverse.storage;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.xmlbeans.XmlObject;
import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.Streamable;
import org.n52.subverse.subscription.SubscribeOptions;
import org.n52.subverse.subscription.Subscription;
import org.n52.subverse.subscription.SubscriptionEndpoint;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class FileSystemSubscriptionDaoTest {

    @Test
    public void testReadWrite() {
        Subscription sub = createSubscription();

        FileSystemSubscriptionDao dao = new FileSystemSubscriptionDao();
        dao.setStorageDirectory("test-subscriptions-"+UUID.randomUUID().toString());
        dao.init();

        dao.storeSubscription(sub);

        Optional<Subscription> recovered = dao.getSubscription(sub.getId());

        Assert.assertThat(recovered.get(), CoreMatchers.equalTo(sub));
    }

//    @Test
    public void testReadAll() {
        Subscription sub1 = createSubscription();
        Subscription sub2 = createSubscription();
        Subscription sub3 = createSubscription();
        Subscription sub4 = createSubscription();

        FileSystemSubscriptionDao dao = new FileSystemSubscriptionDao();
        dao.setStorageDirectory("test-subscriptions-"+UUID.randomUUID().toString());
        dao.init();

        dao.storeSubscription(sub1);
        dao.storeSubscription(sub2);
        dao.storeSubscription(sub3);

        Stream<Subscription> recovered = dao.getAllSubscriptions();
        List<Subscription> list = recovered.collect(Collectors.toList());

        Assert.assertThat(list, CoreMatchers.hasItem(sub1));
        Assert.assertThat(list, CoreMatchers.hasItem(sub2));
        Assert.assertThat(list, CoreMatchers.hasItem(sub3));
        Assert.assertThat(list, CoreMatchers.not(CoreMatchers.hasItem(sub4)));
    }

    private Subscription createSubscription() {
        DeliveryDefinition delDef = new DeliveryDefinition("my-deldef", "http://for.you", "my-pub", true);
        SubscribeOptions options = new SubscribeOptions("my-id",
                new DateTime(),
                XmlObject.Factory.newInstance(),
                "my-filter-lang",
                delDef,
                Collections.singletonMap("my", "val"),
                "my-content-type");

        SubscriptionEndpoint endpoint = new SubscriptionEndpoint(new DummyEndpoint("magic-endpoint"), delDef);

        Subscription sub = new Subscription(UUID.randomUUID().toString(), options, endpoint);
        return sub;
    }

    public static class DummyEndpoint implements DeliveryEndpoint {

        private final String effectiveLocation;

        public DummyEndpoint(String effectiveLocation) {
            this.effectiveLocation = effectiveLocation;
        }

        @Override
        public void deliver(Optional<Streamable> o, boolean useRaw) {
        }

        @Override
        public String getEffectiveLocation() {
            return effectiveLocation;
        }

        @Override
        public void destroy() {
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + Objects.hashCode(this.effectiveLocation);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DummyEndpoint other = (DummyEndpoint) obj;
            if (!Objects.equals(this.effectiveLocation, other.effectiveLocation)) {
                return false;
            }
            return true;
        }

    }
}
