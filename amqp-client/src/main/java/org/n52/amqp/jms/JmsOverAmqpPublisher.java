/*
 * Copyright 2016 52°North Initiative for Geospatial Open Source Software GmbH.
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

package org.n52.amqp.jms;

import java.util.Map;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.n52.amqp.ContentType;
import org.n52.amqp.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class JmsOverAmqpPublisher extends Publisher {

    private static final Logger LOG = LoggerFactory.getLogger(JmsOverAmqpPublisher.class);

    private Session session;
    private final JmsOverAmqpConnection jmsConnection;
    private MessageProducer producer;

    public JmsOverAmqpPublisher(JmsOverAmqpConnection conn) {
        super(conn);
        this.jmsConnection = conn;
    }

    @Override
    public void publish(CharSequence msg, String subject, ContentType ct,
            Map<String, String> deliveryAnnotations,
            Map<String, String> messageAnnotations) {
        
        if (!this.connection.isOpen()) {
            LOG.warn("Cannot send message. Connection already closed");
            return;
        }
        
        try {
            synchronized (this) {
                if (this.producer == null) {
                    this.producer = createProducer(
                            this.jmsConnection.createInternalConnection(
                                    this.jmsConnection.getRemoteURI()));
                }
            }

            TextMessage tm = session.createTextMessage(msg.toString());

            messageAnnotations.forEach((String k, String v) -> {
                try {
                    tm.setStringProperty(k, v);
                } catch (JMSException ex) {
                    LOG.warn("Could not set string property", ex);
                }
            });

            if (ct != null) {
                tm.setStringProperty("Content__Type", ct.getName());
            }

            producer.send(tm);
        } catch (JMSException ex) {
            LOG.warn("Could not published message", ex);
        }

    }

    private MessageProducer createProducer(javax.jms.Connection connection) throws JMSException {
        synchronized (this) {
            if (this.session == null) {
                this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            }
        }

        Destination jmsDest;
        String dest = this.jmsConnection.getDestination();
        if (dest.startsWith(JmsOverAmqpConnection.TOPIC_PREFIX)) {
            jmsDest = this.session.createTopic(dest.substring(JmsOverAmqpConnection.TOPIC_PREFIX.length()));
        } else {
            jmsDest = this.session.createQueue(dest);
        }

        return this.session.createProducer(jmsDest);
    }

}
