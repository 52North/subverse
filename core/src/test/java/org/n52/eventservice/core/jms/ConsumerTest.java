package org.n52.eventservice.core.jms;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 */
public class ConsumerTest {
    
    @Test
    public void testConsumer() {
        new ClassPathXmlApplicationContext("/META-INF/spring/consumer-jms-context.xml", ConsumerTest.class);
        
        while(true);
    }

}
