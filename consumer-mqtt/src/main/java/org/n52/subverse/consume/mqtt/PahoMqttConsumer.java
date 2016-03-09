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
package org.n52.subverse.consume.mqtt;

import java.util.UUID;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class PahoMqttConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(PahoMqttConsumer.class);
    private final String host;
    private final String clientId;
    private MqttClient client;

    /**
     * the MQTT QoS as enum. use #ordinal() to get the int
     */
    public enum QualityOfService {
        AT_MOST_ONCE,
        AT_LEAST_ONCE,
        EXACTLY_ONCE
    }

    public static void main(String[] args) throws MqttException {
        String host = "192.168.52.130";
        String topic = "adsb52n";

        PahoMqttConsumer c = new PahoMqttConsumer(host, UUID.randomUUID().toString());
        c.connect();
        c.subscribe(topic, QualityOfService.EXACTLY_ONCE);

        while (true) {

        }
    }

    /**
     * @param host the IP or DNS name of the broker
     * @param clientId a client id
     */
    public PahoMqttConsumer(String host, String clientId) {
        this.host = host;
        this.clientId = clientId;
    }

    /**
     * connects the client
     *
     * @throws MqttException
     */
    public void connect() throws MqttException {
        this.client = new MqttClient(String.format("tcp://%s:1883", host), clientId,
                new MemoryPersistence());
        client.connect();

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                LOG.warn("Connection lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                LOG.info("New message on topic '{}': {}", topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                LOG.info("Delivery completed for message id '{}'", token.getMessageId());
            }
        });
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

}
