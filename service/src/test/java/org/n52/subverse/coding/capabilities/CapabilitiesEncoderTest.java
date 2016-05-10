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
package org.n52.subverse.coding.capabilities;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import net.opengis.pubsub.x10.PublisherCapabilitiesDocument;
import net.opengis.pubsub.x10.PublisherCapabilitiesType;
import org.junit.Test;
import org.n52.subverse.coding.capabilities.delivery.DeliveryCapabilities;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.DeliveryParameter;
import org.n52.subverse.delivery.DeliveryProvider;
import org.n52.subverse.delivery.Streamable;
import org.n52.subverse.delivery.UnsupportedDeliveryDefinitionException;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class CapabilitiesEncoderTest {

    @Test
    public void testDeliveryParameterEncoding() {
        CapabilitiesEncoder enc = new CapabilitiesEncoder();

        PublisherCapabilitiesDocument doc = PublisherCapabilitiesDocument.Factory.newInstance();
        PublisherCapabilitiesType caps = doc.addNewPublisherCapabilities();

        DeliveryCapabilities deliveryObject = new DeliveryCapabilities(Collections.singleton(new DummyDeliveryProvider()));

        enc.createDeliveryCapabilites(caps, deliveryObject);
    }

    public class DummyDeliveryProvider implements DeliveryProvider {

        @Override
        public boolean supportsDeliveryIdentifier(String id) {
            return true;
        }

        @Override
        public String getIdentifier() {
            return "dummy";
        }

        @Override
        public String getAbstract() {
            return "dummy";
        }

        @Override
        public DeliveryParameter[] getParameters() {
            DeliveryParameter p1 = new DeliveryParameter("mytestspace", "myprop", "whatever");
            DeliveryParameter p2 = new DeliveryParameter("mytestspace", "myprop2", "you");
            DeliveryParameter p3 = new DeliveryParameter("mytestspace", "myprop3", "want");
            return new DeliveryParameter[] {
                p1, p2, p3
            };
        }

        @Override
        public DeliveryEndpoint createDeliveryEndpoint(DeliveryDefinition def) throws UnsupportedDeliveryDefinitionException {
            return new DeliveryEndpoint() {
                @Override
                public void deliver(Optional<Streamable> o) {
                }

                @Override
                public String getEffectiveLocation() {
                    return "";
                }

                @Override
                public void destroy() {
                }
            };
        }

        @Override
        public Map<? extends String, ? extends String> getNamespacePrefixMap() {
            return Collections.emptyMap();
        }



    }
}
