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
package org.n52.amqp;

import java.io.IOException;
import java.net.URI;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.messenger.Messenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Connection {

    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);

    private final String username;
    private final String password;
    private final URI remoteURI;
    private boolean open = true;

    public Connection(URI remoteURI, String username, String password) {
        this.username = username;
        this.password = password;
        this.remoteURI = remoteURI;
    }

    public boolean isOpen() {
        return open;
    }

    public URI getRemoteURI() {
        return remoteURI;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void close() {
        this.open = false;
        LOG.info("Closed connection for client {}", remoteURI);
    }

    public Publisher createPublisher() throws PublisherCreationFailedException {
        try {
            return new Publisher(this);
        } catch (Exception ex) {
            throw new PublisherCreationFailedException("Could not create publisher", ex);
        }
    }

    public Observable<AmqpMessage> createObservable() {
        return Observable.create(t -> {
            LOG.debug("Creating observable for {}", this.remoteURI);

            Messenger messenger = null;
            while (this.open && !t.isUnsubscribed()) {
                messenger = Messenger.Factory.create();
                try {
                    messenger.start();
                } catch (IOException ex) {
                    LOG.warn("Could not spawn subscriber", ex);
                    return;
                }
                messenger.subscribe(this.remoteURI.toString());

                while (!messenger.stopped()) {
                    LOG.debug("start receiving");
                    messenger.recv();
                    while (messenger.incoming() > 0) {
                        LOG.debug("starting receiving loop");
                        Message msg = messenger.get();
                        Section body = msg.getBody();
                        if (body instanceof AmqpValue) {
                            t.onNext(createMessage(msg));
                        }
                        else {
                            LOG.warn("Unsupported type of body: " + (body == null ? "n/a" : body.getClass()));
                        }
                    }
                }
            }

            if (messenger != null && !messenger.stopped()) {
                messenger.stop();
            }

            if (!t.isUnsubscribed()) {
                t.unsubscribe();
            }

            t.onCompleted();
        });
    }

    private AmqpMessage createMessage(Message msg) {
        return new AmqpMessage(((AmqpValue) msg.getBody()).getValue(),
            createContentType(msg.getContentType()),
            msg.getSubject());
    }

    private ContentType createContentType(String contentType) {
        if (contentType == null) {
            return null;
        }

        return new ContentType(contentType);
    }
}
