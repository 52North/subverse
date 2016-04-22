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
package org.n52.subverse.delivery.amqp;

import java.net.URISyntaxException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.n52.subverse.delivery.DeliveryDefinition;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class AmqpDeliveryEndpointTest {

    @Test
    public void testLocationWithTopic() throws URISyntaxException {
        DeliveryDefinition def = new DeliveryDefinition("amqp10", "localhost", "pubId");
        AmqpDeliveryEndpoint ep = new AmqpDeliveryEndpoint(def, "amqp://localhost");

        Assert.assertThat(ep.getEffectiveLocation(), CoreMatchers.startsWith("amqp://localhost/subverse.pubId."));

        def = new DeliveryDefinition("amqp10", "remote-host/trying-to-path", "pubId");
        ep = new AmqpDeliveryEndpoint(def, "localhoster");

        Assert.assertThat(ep.getEffectiveLocation(), CoreMatchers.startsWith("amqp://remote-host/trying-to-path"));

        def = new DeliveryDefinition("amqp10", "defaulthost/trying-to-path", "pubId");
        ep = new AmqpDeliveryEndpoint(def, "defaulthost");

        Assert.assertThat(ep.getEffectiveLocation(), CoreMatchers.startsWith("amqp://defaulthost/trying-to-path"));

        def = new DeliveryDefinition("amqp10", "localhost", "pubId");
        ep = new AmqpDeliveryEndpoint(def, "defaulthost");

        Assert.assertThat(ep.getEffectiveLocation(), CoreMatchers.is("amqp://localhost"));
    }

}
