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
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import org.n52.amqp.AmqpConnectionCreationFailedException;
import org.n52.amqp.Connection;
import org.n52.amqp.ConnectionBuilder;
import org.n52.amqp.ContentType;
import org.n52.amqp.Publisher;
import org.n52.amqp.PublisherCreationFailedException;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.Streamable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class AmqpDeliveryEndpoint implements DeliveryEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(AmqpDeliveryEndpoint.class);
    private static final String BASE_TOPIC = "subverse";

    private final String broker;
    private final String address;
    private final String parentPublicationId;
    private Publisher client;
    private final String id;
    private Connection connection;


    public AmqpDeliveryEndpoint(DeliveryDefinition def, String defaultBroker) throws URISyntaxException {
        Objects.requireNonNull(def);
        Objects.requireNonNull(defaultBroker);

        this.id = ShortId.randomString(8, 10);
        this.parentPublicationId = def.getPublicationId();

        URI defaultBrokerUri = new URI(ensureSchemeInAddress(defaultBroker));

        if (def.getLocation() != null) {
            URI locationUri = new URI(ensureSchemeInAddress(def.getLocation()));

            if (defaultBrokerUri.getHost().equals(locationUri.getHost())) {
                /*
                 * same host, we can check for a path (=queue) and
                 * add one if not present
                 */
                this.broker = ensureSchemeInAddress(def.getLocation());
                this.address = createQueueAddress();
            }
            else {
                /*
                 * the location host is not the same as the default broker
                 * - we cannot assume that adhoc queue work, do not use
                 * them
                 */
                this.broker = ensureSchemeInAddress(def.getLocation());
                this.address = this.broker;
            }
        }
        else {
            /*
             * use the default broker and add an adhoc queue
             */
            this.broker = ensureSchemeInAddress(defaultBroker);
            this.address = createQueueAddress();
        }

//        def.addParameter(new DeliveryParameter(AmqpDeliveryProvider.EXTENSION_NAMESPACE, "queue", this.address));
    }

    @Override
    public synchronized void deliver(Optional<Streamable> o, boolean asRaw) {
        try {
            if (!o.isPresent()) {
                LOG.warn("Cannot delivery null object");
                return;
            }

            if (this.client == null) {
                this.connection = ConnectionBuilder.create(URI.create(this.address)).build();
                this.client = this.connection.createPublisher();
                LOG.info("AMQP Client for {} created", this.address);
            }

            LOG.info("Sending message to {}", this.address);
            this.client.publish(prepareBody(o.get()), new ContentType(o.get().getContentType()));
            LOG.info("Message sent to {}", this.address);
        } catch (PublisherCreationFailedException | IOException | AmqpConnectionCreationFailedException ex) {
            LOG.warn("Could not send AMQP message", ex);
        }
    }

    private String ensureSchemeInAddress(String add) {
        if (add.startsWith("queue://")) {
            return add.replace("queue://", "amqp://");
        }
        else if (add.startsWith("topic://")) {
            return add.replace("topic://", "amqp://");
        }
        else if (add.startsWith("amqp://")) {
            return add;
        }
        else if (add.startsWith("amqps://")) {
            return add;
        }

        return "amqp://".concat(add);
    }

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
        if (hasPath(this.broker)) {
            return this.broker;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(this.broker);

        if (!this.broker.endsWith("/")) {
            sb.append("/");
        }

        sb.append(BASE_TOPIC);
        sb.append(".");
        sb.append(parentPublicationId);
        sb.append(".");
        sb.append(id);
        return sb.toString();
    }

    @Override
    public synchronized void destroy() {
        if (this.connection != null) {
            this.connection.close();
        }

        if (this.client != null) {
            this.client.destroy();
            LOG.info("Messenger for {} stopped.", this.address);
        }
    }

    private boolean hasPath(String b) {
        int i = b.indexOf("/", 8);
        return i > 0;
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
