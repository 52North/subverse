package org.n52.subverse.core.jms;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 */
public class JMSConsumerTestDisabled {

    @Test
    public void testConsumer() {
        new ClassPathXmlApplicationContext("/META-INF/spring/consumer-jms-context.xml", JMSConsumerTestDisabled.class);

        while(true);
    }

}
