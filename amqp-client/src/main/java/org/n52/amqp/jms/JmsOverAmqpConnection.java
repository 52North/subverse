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
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.n52.amqp.AmqpMessage;
import org.n52.amqp.Connection;
import org.n52.amqp.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class JmsOverAmqpConnection extends Connection {

    private static final Logger LOG = LoggerFactory.getLogger(JmsOverAmqpConnection.class);
    private static final String TOPIC_PREFIX = "topic://";

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
                JmsConnectionFactory factory = new JmsConnectionFactory(String.format("%s://%s:%s",
                        uri.getScheme(),
                        this.host,
                        this.port));
                connection = factory.createConnection(this.getUsername(), this.getPassword());
                connection.start();

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
                    if (msg instanceof TextMessage) {
                        t.onNext(parseMessage((TextMessage) msg));
                    } else {
                        LOG.warn("Unsupported message type: {}", msg.getClass());
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

    private AmqpMessage parseMessage(TextMessage msg) throws JMSException {
        Enumeration names = msg.getPropertyNames();

        Map<String, String> messageAnnotations = new HashMap<>();
        String ct = null;
        while (names.hasMoreElements()) {
            Object next = names.nextElement();
            if (next.toString().equals("Content-Type")) {
                ct = msg.getStringProperty(next.toString());
            }
            else {
                messageAnnotations.put(next.toString(), msg.getStringProperty(next.toString()));
            }
        }

        return new AmqpMessage(msg.getText(), ct != null ? new ContentType(ct) : null, null,
                Collections.emptyMap(), messageAnnotations);
    }

}
