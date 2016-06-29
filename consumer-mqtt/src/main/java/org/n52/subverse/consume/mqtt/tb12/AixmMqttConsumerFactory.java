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
package org.n52.subverse.consume.mqtt.tb12;

import java.util.UUID;
import javax.inject.Inject;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.n52.iceland.lifecycle.Constructable;
import org.n52.iceland.lifecycle.Destroyable;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.subverse.consume.mqtt.PahoMqttConsumer;
import org.n52.subverse.engine.FilterEngine;
import org.n52.subverse.publications.PublicationsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class AixmMqttConsumerFactory implements Constructable, Destroyable, PublicationsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AixmMqttConsumerFactory.class);
    private PahoMqttConsumer consumer;
    private FilterEngine engine;
    protected static final String IDENTIFIER = "AIXM";

    public FilterEngine getEngine() {
        return engine;
    }

    @Inject
    public void setEngine(FilterEngine engine) {
        this.engine = engine;
    }

    @Override
    public void init() {
        String host = "ows.dev.52north.org";
        String topic = "tb12/aixm";

        this.consumer = new PahoMqttConsumer(host, UUID.randomUUID().toString(), (byte[] msg) -> {
            LOG.info("New message on topic '{}'", topic);
            String content = new String(msg);
            engine.filterMessage(content, IDENTIFIER, determineContentType(content));
        });

        new Thread(() -> {
            try {
                this.consumer.connect();
                this.consumer.subscribe(topic, PahoMqttConsumer.QualityOfService.EXACTLY_ONCE);
            } catch (MqttException ex) {
                LOG.warn("Could not start MQTT consumer", ex);
            }
        }).start();


        LOG.info("listening for messages on topic '{}' of MQTT host {}", topic, host);

    }

    private String determineContentType(String content) {
        return MediaTypes.APPLICATION_XML.toString();
    }

    @Override
    public void destroy() {
        if (this.consumer == null) {
            return;
        }

        new Thread(() -> {
            this.consumer.destroy();
        }).start();
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String getAbstract() {
        return "provides AIXM data as DNOTAM";
    }

    @Override
    public String getContentType() {
        return MediaTypes.APPLICATION_XML.toString();
    }

}
