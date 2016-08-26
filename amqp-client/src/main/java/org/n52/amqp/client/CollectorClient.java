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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.n52.amqp.AmqpConnectionCreationFailedException;
import org.n52.amqp.AmqpMessage;
import org.n52.amqp.Connection;
import org.n52.amqp.ConnectionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class CollectorClient {

    private static final Logger LOG = LoggerFactory.getLogger(CollectorClient.class);

    public static void main(String[] args) throws AmqpConnectionCreationFailedException, URISyntaxException, InterruptedException, IOException {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("'schema://[user:pwd@]host[:port]/[destination]' must be provided as argument");
        }

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);

        Path storageDir;
        if (args.length > 1) {
            //storage dir
            storageDir = Paths.get(args[1]);
        }
        else {
            storageDir = Files.createTempDirectory("amqp-collector");
        }

        String prefix = UUID.randomUUID().toString().substring(0, 6).concat("_");
        LOG.info("Storing messages in folder: {}, with prefix: {}", storageDir, prefix);

        AtomicInteger count = new AtomicInteger();
        Connection connConsumer = ConnectionBuilder.create(new URI(args[0])).jmsFlavor().build();
        LOG.info("Connecting to: "+connConsumer.getRemoteURI());
        connConsumer.createObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Subscriber<AmqpMessage>() {
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
                try {
                    storeToFile(t, storageDir, prefix+count.getAndIncrement(), df);
                } catch (IOException e) {
                    LOG.warn("storage Error: "+e.getMessage(), e);
                }
            }

        });

        while (true) {
            Thread.sleep(1000);
        }
    }

    private static void storeToFile(AmqpMessage t, Path storageDir, String fileName, DateFormat df) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(ZonedDateTime.now().format( DateTimeFormatter.ISO_INSTANT ));
        sb.append(System.getProperty("line.separator"));
        sb.append("deliveryAnnotations=");
        sb.append(t.getDeliveryAnnotations());
        sb.append(System.getProperty("line.separator"));
        sb.append("messageAnnotations=");
        sb.append(t.getMessageAnnotations());
        sb.append(System.getProperty("line.separator"));
        sb.append("subject=");
        sb.append(t.getSubject().orElse(null));
        sb.append(System.getProperty("line.separator"));
        sb.append("Content-Type=");
        sb.append(t.getContentType().orElse(null));
        sb.append(System.getProperty("line.separator"));
        sb.append(System.getProperty("line.separator"));
        sb.append(t.getBody());
        sb.append(System.getProperty("line.separator"));

        Files.write(storageDir.resolve(fileName), sb.toString().getBytes(), StandardOpenOption.CREATE);
    }

}
