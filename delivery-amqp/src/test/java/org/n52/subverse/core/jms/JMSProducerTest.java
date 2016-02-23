package org.n52.subverse.core.jms;

import javax.jms.JMSException;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 */
public class JMSProducerTest {

    @Test
    public void testProcuder() throws JMSException {
    	ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/producer-jms-context.xml", JMSProducerTest.class);
        BasicMessageProducer producer = (BasicMessageProducer) context.getBean("messageProducer");
        producer.sendMessages();
    }

}
