package org.n52.subverse.core.amqp;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.SaslConfig;
import org.junit.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 */
public class AmqpProducerTest {

    @Test
    public void testProducer() {
        ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/producer-amqp-context.xml");
        AmqpTemplate aTemplate = (AmqpTemplate) context.getBean("cloudamqpTemplate");
        for (int i = 1; i < 6; i++) {
            aTemplate.convertAndSend("my.routingkey", "Hello CloudAMQP, Message # " +i);
        }
    }

}
