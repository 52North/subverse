package org.n52.subverse.core.jms;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 */
public class JMSConsumerTest {
    
    @Test
    public void testConsumer() {
        new ClassPathXmlApplicationContext("/META-INF/spring/consumer-jms-context.xml", JMSConsumerTest.class);
        
        while(true);
    }

}
