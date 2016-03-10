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
package org.n52.subverse.core.jms;

import java.util.Date;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;

/**
 *
 */
public class BasicMessageProducer {

    private static final Logger LOG = LoggerFactory.getLogger(BasicMessageProducer.class);

    protected int numberOfMessages = 100;
    private ConnectionFactory connectionFactory;
    private DestinationFactory destinationFactory;

    public void setNumberOfMessages(int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public DestinationFactory getDestinationFactory() {
        return destinationFactory;
    }

    public void setDestinationFactory(DestinationFactory destinationFactory) {
        this.destinationFactory = destinationFactory;
    }

    public void sendMessages() throws JMSException {
        final StringBuilder buffer = new StringBuilder();

//        for (int i = 0; i < numberOfMessages; ++i) {
//            JmsTemplate jmsTemplate = new JmsTemplate(this.connectionFactory);
//            jmsTemplate.setDefaultDestination(destinationFactory.createDestination("TEST-"+i));
//
//            buffer.append("Message '").append(i).append("' sent at: ").append(new Date());
//
//            final int count = i;
//            final String payload = buffer.toString();
//
//            jmsTemplate.send(new MessageCreator() {
//                public Message createMessage(Session session) throws JMSException {
//                    TextMessage message = session.createTextMessage(payload);
//                    message.setIntProperty("messageCount", count);
//                    LOG.info("Sending message number '{}'", count);
//                    return message;
//                }
//            });
//
//            buffer.delete(0, buffer.length());
//        }
    }
}
