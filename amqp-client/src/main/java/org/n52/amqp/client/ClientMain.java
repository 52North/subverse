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

package org.n52.amqp.client;

import java.net.URI;
import java.net.URISyntaxException;
import org.n52.amqp.AmqpConnectionCreationFailedException;
import org.n52.amqp.AmqpMessage;
import org.n52.amqp.Connection;
import org.n52.amqp.ConnectionBuilder;
import org.n52.amqp.PublisherCreationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class ClientMain {

    private static final Logger LOG = LoggerFactory.getLogger(ClientMain.class);

    public static void main(String[] args) throws AmqpConnectionCreationFailedException, URISyntaxException {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Host must be provided as argument");
        }

        Connection conn = ConnectionBuilder.create(new URI(String.format("amqp://%s:8001/N52-test", args[0]))).build();
        LOG.info("Connecting to: "+conn.getRemoteURI());
        conn.createObservable().subscribe(new Subscriber<AmqpMessage>() {
            @Override
            public void onCompleted() {
                LOG.info("completed observable");
            }

            @Override
            public void onError(Throwable e) {
                LOG.warn("AMQP Error: "+e.getMessage(), e);
            }

            @Override
            public void onNext(AmqpMessage t) {
                LOG.info("[new message] "+t);
            }
        });

        new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(5000);
                    LOG.info("Publishing message #"+i);
                    conn.createPublisher().publish("echo "+i);
                }
            } catch (InterruptedException | PublisherCreationFailedException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
            
            LOG.info("Finished publishing");
        }).start();

    }

}
