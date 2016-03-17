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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import org.apache.activemq.transport.amqp.client.AmqpClient;
import org.apache.activemq.transport.amqp.client.AmqpConnection;
import org.apache.activemq.transport.amqp.client.AmqpMessage;
import org.apache.activemq.transport.amqp.client.AmqpSender;
import org.apache.activemq.transport.amqp.client.AmqpSession;
import org.apache.qpid.proton.messenger.Messenger;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.Streamable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class AmqpDeliveryEndpoint implements DeliveryEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(AmqpDeliveryEndpoint.class);
    private static final String BASE_TOPIC = "subverse";

    private final Optional<String> subject;
    private final Optional<String> broker;
    private final String defaultBroker;
    private final Optional<String> topic;
    private final String address;
    private final String parentPublicationId;
    private Messenger messenger;
    private AmqpClient client;
    private AmqpConnection connection;
    private final String id;
    private int messageCount;


    public AmqpDeliveryEndpoint(DeliveryDefinition def, String defaultBroker) {
        Objects.requireNonNull(def);
        Objects.requireNonNull(defaultBroker);

        this.id = ShortId.randomString(8, 10);

        this.subject = def.getParameter("amqp.subject");
        this.topic = def.getParameter("amqp.topic");
        this.broker = Optional.ofNullable(def.getLocation());
        this.defaultBroker = defaultBroker;
        this.parentPublicationId = def.getPublicationId();
        this.address = createQueueAddress();
    }

    @Override
    public synchronized void deliver(Optional<Streamable> o) {
        try {
            if (!o.isPresent()) {
                LOG.warn("Cannot delivery null object");
                return;
            }

            if (this.client == null) {
                String uri = "tcp://"+removeProtocol(broker.orElse(this.defaultBroker));
                this.client = new AmqpClient(URI.create(uri), null, null);
                LOG.info("AMQP Client for {} created", uri);
            }

            if (this.connection == null || !this.connection.isConnected()) {
                this.connection = client.connect();
                LOG.info("AMQP Client connected");
            }

            LOG.info("Sending message to {}", this.address);
            AmqpSession session = connection.createSession();
            AmqpSender sender = session.createSender(this.address);
            AmqpMessage message = new AmqpMessage();
            message.setMessageId(id+"_"+messageCount++);
            message.setText(prepareBody(o.get()));
            sender.send(message);
            sender.close();
            LOG.info("Message sent to {}", this.address);
        } catch (Exception ex) {
            LOG.warn("Could not send AMQP message", ex);
        }
    }

//    private String prepareAddress() {
//        String add = broker.orElse(this.defaultBroker);
//
//        if (add.startsWith("queue://")) {
//            if (topic.isPresent()) {
//                return add+ "/" +topic.get();
//            }
//            return add;
//        }
//        else if (add.startsWith("topic://")) {
//            if (!topic.isPresent()) {
//                return add.replace("topic://", "queue://");
//            }
//            return add.replace("topic://", "queue://")+ "/" +topic.get();
//        }
//        else if (add.startsWith("amqp://")) {
//            if (topic.isPresent()) {
//                return add.replace("amqp://", "queue://")+ "/" +topic.get();
//            }
//            return add.replace("amqp://", "queue://");
//        }
//
//        if (topic.isPresent()) {
//            return "queue://".concat(add)+ "/" +topic.get();
//        }
//
//        return "queue://".concat(add);
//    }

    private String prepareBody(Streamable s) throws IOException {
        if (s.originalObject() instanceof String) {
            return (String) s.originalObject();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream asStream = s.asStream()) {
            while (asStream.available() > 0) {
                baos.write(asStream.read());
            }
        }

        return new String(baos.toByteArray());
    }

    @Override
    public String getEffectiveLocation() {
        return this.address;
    }

    private String createQueueAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append("queue://");
        sb.append(BASE_TOPIC);
        sb.append(".");
        sb.append(parentPublicationId);
        sb.append(".");
        sb.append(id);
        return sb.toString();
    }

    @Override
    public void destroy() {
        if (this.messenger != null) {
            this.messenger.stop();
            LOG.info("Messenger for {} stopped.", this.address);
        }
    }

    private String removeProtocol(String b) {
        if (b.contains("://")) {
            return b.substring(b.indexOf("://")+3, b.length());
        }

        return b;
    }

    private static class ShortId {

        public static String randomString(int lo, int hi){
            int n = makeRandom(lo, hi);
            byte b[] = new byte[n];
            for (int i = 0; i < n; i++) {
                b[i] = (byte) makeRandom('a', 'z');
            }
            return new String(b);
        }

        private static int makeRandom(int lo, int hi){
            SecureRandom rn = new SecureRandom();
            int n = hi - lo + 1;
            int i = rn.nextInt(n);
            if (i < 0) {
                i = -i;
            }
            return lo + i;
        }


    }

}
