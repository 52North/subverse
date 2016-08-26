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

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.message.facade.JmsMessageFacade;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsMessageFacade;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.n52.amqp.AmqpMessage;
import org.n52.amqp.Connection;
import org.n52.amqp.ContentType;
import org.n52.amqp.Publisher;
import org.n52.amqp.PublisherCreationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class JmsOverAmqpConnection extends Connection {

    private static final Logger LOG = LoggerFactory.getLogger(JmsOverAmqpConnection.class);
    protected static final String TOPIC_PREFIX = "topic://";

    private final String host;
    private final int port;
    private final String destination;

    public JmsOverAmqpConnection(URI remoteURI, String username, String password) {
        super(remoteURI, username, password);
        this.host = remoteURI.getHost();
        //the provided, or the default IANA-assigned AMQP port
        this.port = remoteURI.getPort() == -1 ? 5672 : remoteURI.getPort();
        String p = remoteURI.getPath();
        this.destination = (p != null && p.startsWith("/")) ? p.substring(1, p.length()) : p;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public Observable<AmqpMessage> createObservable() {

        return Observable.create(t -> {
            MessageConsumer consumer;
            URI uri = this.getRemoteURI();
            LOG.debug("Creating observable for {}", uri);

            javax.jms.Connection connection;
            try {
                connection = createInternalConnection(uri);
                consumer = createConsumer(connection);
            } catch (JMSException ex) {
                LOG.warn("Could not spawn subscriber", ex);
                t.onError(ex);
                return;
            }

            //wait for messages until unsubscribed
            while (this.isOpen() && !t.isUnsubscribed()) {
                try {
                    Message msg = consumer.receive();
                    AmqpMessage parsed = parseMessage(msg);
                    if (parsed != null) {
                        t.onNext(parsed);
                    }
                } catch (JMSException ex) {
                    LOG.warn("Could not receive message", ex);
                }
            }

            try {
                connection.close();
            } catch (JMSException ex) {
                LOG.warn("Could not close connection", ex);
            }

            if (!t.isUnsubscribed()) {
                t.unsubscribe();
            }

            t.onCompleted();
        });
    }

    protected javax.jms.Connection createInternalConnection(URI uri) throws JMSException {
        javax.jms.Connection connection;
        JmsConnectionFactory factory = new JmsConnectionFactory(String.format("%s://%s:%s",
                uri.getScheme(),
                this.host,
                this.port));
        connection = factory.createConnection(this.getUsername(), this.getPassword());
        connection.start();
        return connection;
    }

    private MessageConsumer createConsumer(javax.jms.Connection connection) throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        MessageConsumer consumer;
        Destination jmsDest;
        if (this.destination.startsWith(TOPIC_PREFIX)) {
            jmsDest = session.createTopic(this.destination.substring(TOPIC_PREFIX.length()));
        } else {
            jmsDest = session.createQueue(this.destination);
        }
        consumer = session.createConsumer(jmsDest);
        return consumer;
    }

    private AmqpMessage parseMessage(Message msg) throws JMSException {
        Map<String, String> messageAnnotations = extractMessageAnnotations(msg);
        Map<String, String> deliveryAnnotations = extractDeliveryAnnotations(msg);

        if (messageAnnotations == null) {
            messageAnnotations = parseMessageAnnotations(msg);
        }
        else {
            messageAnnotations.putAll(parseMessageAnnotations(msg));
        }

        String ct;
        if (messageAnnotations.containsKey("Content__Type")) {
            ct = messageAnnotations.get("Content__Type");
        }
        else {
            ct = extractContentType(msg);
        }

        String text;
        if (msg instanceof TextMessage) {
            text = ((TextMessage) msg).getText();
        }
        else if (msg instanceof BytesMessage) {
            byte[] buffer = new byte[(int) ((BytesMessage) msg).getBodyLength()];
            ((BytesMessage) msg).readBytes(buffer, buffer.length);
            text = new String(buffer);
        }
        else {
            LOG.warn("Unsupported message type: {}", msg.getClass());
            return null;
        }

        return new AmqpMessage(text, ct != null ? new ContentType(ct) : null, msg.getJMSType(),
                deliveryAnnotations, messageAnnotations);
    }

    private String extractContentType(Message msg) {
        if (msg instanceof JmsMessage) {
            JmsMessageFacade facade = ((JmsMessage) msg).getFacade();
            if (facade instanceof AmqpJmsMessageFacade) {
                return ((AmqpJmsMessageFacade) facade).getContentType();
            }
        }

        return null;
    }

    private Map<String, String> extractDeliveryAnnotations(Message msg) {
        if (msg instanceof JmsMessage) {
            JmsMessageFacade facade = ((JmsMessage) msg).getFacade();
            if (facade instanceof AmqpJmsMessageFacade) {
                org.apache.qpid.proton.message.Message amqpMessage = ((AmqpJmsMessageFacade) facade).getAmqpMessage();
                DeliveryAnnotations origDeliveryAnnos = amqpMessage.getDeliveryAnnotations();
                if (origDeliveryAnnos != null && origDeliveryAnnos.getValue() != null) {
                    Map<String, String> result = new HashMap<>();
                    origDeliveryAnnos.getValue().forEach((Symbol k, Object v) -> {
                        result.put(k.toString(), v.toString());
                    });
                    return result;
                }
            }
        }

        return Collections.emptyMap();
    }

    private Map<String, String> extractMessageAnnotations(Message msg) {
        if (msg instanceof JmsMessage) {
            JmsMessageFacade facade = ((JmsMessage) msg).getFacade();
            if (facade instanceof AmqpJmsMessageFacade) {
                org.apache.qpid.proton.message.Message amqpMessage = ((AmqpJmsMessageFacade) facade).getAmqpMessage();
                MessageAnnotations origMessageAnnos = amqpMessage.getMessageAnnotations();
                if (origMessageAnnos != null && origMessageAnnos.getValue() != null) {
                    Map<String, String> result = new HashMap<>();
                    origMessageAnnos.getValue().forEach((Symbol k, Object v) -> {
                        result.put(k.toString(), v.toString());
                    });
                    return result;
                }
            }
        }

        return Collections.emptyMap();
    }

    private Map<String, String> parseMessageAnnotations(Message msg) throws JMSException {
        Enumeration names = msg.getPropertyNames();

        Map<String, String> messageAnnotations = new HashMap<>();
        while (names.hasMoreElements()) {
            Object next = names.nextElement();
            String val = parseToString(msg, next.toString());
            if (val != null) {
                messageAnnotations.put(next.toString(), val);
            }
        }
        return messageAnnotations;
    }

    private String parseToString(Message msg, String key) {
        String result;
        try {
            result = msg.getStringProperty(key);
            return result;
        } catch (JMSException ex) {
        }

        try {
            result = String.format("%s", msg.getLongProperty(key));
            return result;
        } catch (JMSException ex) {
        }

        try {
            result = String.format("%s", msg.getBooleanProperty(key));
            return result;
        } catch (JMSException ex) {
        }

        try {
            result = String.format("%s", msg.getDoubleProperty(key));
            return result;
        } catch (JMSException ex) {
        }

        return null;
    }

    @Override
    public Publisher createPublisher() throws PublisherCreationFailedException {
        return new JmsOverAmqpPublisher(this);
    }



}
