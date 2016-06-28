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
package org.n52.subverse.consume.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class PahoMqttConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(PahoMqttConsumer.class);
    private final String host;
    private final String clientId;
    private MqttClient client;
    private final MessageCallback callback;
    private boolean connected;

    /**
     * the MQTT QoS as enum. use #ordinal() to get the int
     */
    public enum QualityOfService {
        AT_MOST_ONCE,
        AT_LEAST_ONCE,
        EXACTLY_ONCE
    }

    /**
     * @param host the IP or DNS name of the broker
     * @param clientId a client id
     * @param cb the callback for reception of messages
     */
    public PahoMqttConsumer(String host, String clientId, MessageCallback cb) {
        this.host = host;
        this.clientId = clientId;
        this.callback = cb;
    }

    /**
     * connects the client
     *
     * @throws MqttException
     */
    public void connect() throws MqttException {
        this.client = new MqttClient(String.format("tcp://%s:1883", host), clientId,
                new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setConnectionTimeout(5000);

        client.connect(options);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                LOG.warn("Connection lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                LOG.info("New message on topic '{}': {}", topic, message);
                try {
                    callback.receive(message.getPayload());
                }
                catch (RuntimeException e) {
                    LOG.warn("Error in callback", e);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                LOG.info("Delivery completed for message id '{}'", token.getMessageId());
            }
        });

        this.connected = true;
    }

    /**
     * subscribe for a topic
     *
     * @param topic the topic to subscribe to
     * @param qos the QoS level
     * @throws MqttException if something goes wrong
     */
    public void subscribe(String topic, QualityOfService qos) throws MqttException {
        client.subscribe(topic, qos.ordinal());
    }

    public void destroy() {
        try {
            if (this.client.isConnected() && this.connected) {
                this.client.disconnectForcibly(5000);
            }
        } catch (MqttException ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }

    public static interface MessageCallback {

        void receive(byte[] msg);

    }

}
