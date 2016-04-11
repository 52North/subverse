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

import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.config.annotation.Setting;
import org.n52.subverse.SubverseSettings;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.DeliveryParameter;
import org.n52.subverse.delivery.DeliveryProvider;
import org.n52.subverse.delivery.UnsupportedDeliveryDefinitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
@Configurable
public class AmqpDeliveryProvider implements DeliveryProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AmqpDeliveryProvider.class);
    protected static final String EXTENSION_NAMESPACE = "http://52north.org/pubsub/amqp-10-delivery";

    private static final String IDENTIFIER = "amqp10";
    private String defaultHost;

    @Setting(SubverseSettings.AMQP_DEFAULT_HOST)
    public void setDefaultHost(String defaultHost) {
        this.defaultHost = defaultHost;
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
        return "Advanced Message Queuing Protocol 1.0";
    }

    @Override
    public DeliveryEndpoint createDeliveryEndpoint(DeliveryDefinition def) throws UnsupportedDeliveryDefinitionException {
        return new AmqpDeliveryEndpoint(def, defaultHost);
    }

    @Override
    public DeliveryParameter[] getParameters() {
        DeliveryParameter defaultHostParam = new DeliveryParameter(EXTENSION_NAMESPACE,
                "defaultHost", defaultHost);
        return new DeliveryParameter[] {defaultHostParam};
    }



}
