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
package org.n52.subverse.notify;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import org.apache.xmlbeans.XmlObject;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class NotificationMessage {

    private final XmlObject message;
    private final Optional<String> topic;
    private final Optional<String> producerReference;
    private final Optional<String> subscriptionReference;

    public NotificationMessage(XmlObject message, Optional<String> topic,
            Optional<String> producerReference, Optional<String> subscriptionReference) {
        this.message = message;
        this.topic = topic;
        this.producerReference = producerReference;
        this.subscriptionReference = subscriptionReference;
    }

    public XmlObject getMessage() {
        return message;
    }

    public Optional<String> getTopic() {
        return topic;
    }

    public Optional<String> getProducerReference() {
        return producerReference;
    }

    public Optional<String> getSubscriptionReference() {
        return subscriptionReference;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).
                add("messageQName", message.schemaType() != null ? message.schemaType().getName() : "n/a").
                add("topic", topic).
                add("producerReference", producerReference).
                add("subscriptionReference", subscriptionReference).toString();
    }



}
