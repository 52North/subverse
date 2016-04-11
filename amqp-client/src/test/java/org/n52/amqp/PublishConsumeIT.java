
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
        Connection conn = ConnectionBuilder.create(new URI("amqp://localhost/test-queue.abc")).build();
        
        CyclicBarrier barrier = new CyclicBarrier(2);
        
        List<Object> received = new ArrayList<>();
        
        conn.subscribe((Object message) -> {
            received.add(message);
            LOG.info("Received message: "+ message);
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException ex) {
                Assert.fail(ex.getMessage());
            }
        });
        
        String time = new Date().toString();
        conn.createPublisher().publish("<test>"+time+"</test>");
        
        barrier.await(10, TimeUnit.SECONDS);
        
        Assert.assertThat(received.size(), CoreMatchers.is(1));
        Assert.assertThat(received.get(0).toString(), CoreMatchers.containsString(time));
    }
    
}
