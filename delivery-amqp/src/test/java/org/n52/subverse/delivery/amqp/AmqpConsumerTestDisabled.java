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

import java.net.URI;
import org.apache.activemq.transport.amqp.client.AmqpClient;
import org.apache.activemq.transport.amqp.client.AmqpConnection;
import org.apache.activemq.transport.amqp.client.AmqpMessage;
import org.apache.activemq.transport.amqp.client.AmqpReceiver;
import org.apache.activemq.transport.amqp.client.AmqpSession;
import org.apache.qpid.proton.messenger.Messenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class AmqpConsumerTestDisabled {

    private static final Logger LOG = LoggerFactory.getLogger(AmqpConsumerTestDisabled.class);

    public static void main(String[] args) {
        new AmqpConsumerTestDisabled().startListening();
    }

    private final Messenger messenger;
    private boolean running = true;
    private AmqpClient client;
    private AmqpConnection connection;
    private AmqpReceiver receiver;

    public AmqpConsumerTestDisabled() {
        this.messenger = Messenger.Factory.create();
        this.messenger.setBlocking(true);
    }


    public void startListening() {
        new Thread(() -> {
            if (this.client == null) {
                String uri = "tcp://localhost";
                this.client = new AmqpClient(URI.create(uri), null, null);
                LOG.info("consumer Client for {} created", uri);
            }


            try {
                if (this.connection == null || !this.connection.isConnected()) {
                    this.connection = client.connect();
                    LOG.info("consumer Client connected");
                }
                AmqpSession session = connection.createSession();
                String add = "queue://foknunss";
                receiver = session.createReceiver(add);
            } catch (Exception ex) {
                LOG.warn(ex.getMessage(), ex);
            }

            int i = 0;
            while (running) {
                try {
                    receiver.flow(++i);
                    AmqpMessage msg = receiver.receive();
                    LOG.info("Received message: {}", msg.getWrappedMessage().getBody());
                    Thread.sleep(50);
                } catch (Exception ex) {
                    LOG.warn(ex.getMessage(), ex);
                }
            }
        }).start();
    }

}
