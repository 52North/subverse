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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.schedulers.Schedulers;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class PublishConsumeIT {

    private static final Logger LOG = LoggerFactory.getLogger(PublishConsumeIT.class);

    @Test
    public void testWorkflow() throws AmqpConnectionCreationFailedException, URISyntaxException,
            SubscriptionFailedException, InvalidTargetAddressException, PublisherCreationFailedException,
            InterruptedException, BrokenBarrierException, TimeoutException {

        //for qpid java broker, a queue has to be defined by the admin before running the test
        //Connection conn = ConnectionBuilder.create(new URI("amqp://localhost/test-queue.abcd")).build();

        //random queues work with activemq
        Connection conn = ConnectionBuilder.create(new URI("amqp://localhost/" + UUID.randomUUID().toString())).build();

        CyclicBarrier barrier = new CyclicBarrier(2);

        List<Object> received = new ArrayList<>();

        conn.createObservable()
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.io())
                .subscribe((Object message) -> {

            received.add(message);
            LOG.info("Received message: "+ message);
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException ex) {
                Assert.fail(ex.getMessage());
            }
        });

        String time = new Date().toString();
        conn.createPublisher().publish("<test>"+time+"</test>", ContentType.APPLICATION_XML);

        barrier.await(10, TimeUnit.SECONDS);

        Assert.assertThat(received.size(), CoreMatchers.is(1));
        Assert.assertThat(received.get(0).toString(), CoreMatchers.containsString(time));
    }

}
