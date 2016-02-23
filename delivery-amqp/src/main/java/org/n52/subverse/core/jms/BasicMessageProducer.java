package org.n52.subverse.core.jms;

import java.util.Date;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;

/**
 *
 */
public class BasicMessageProducer {

    private static final Logger LOG = LoggerFactory.getLogger(BasicMessageProducer.class);

    protected int numberOfMessages = 100;
    private ConnectionFactory connectionFactory;
    private DestinationFactory destinationFactory;

    public void setNumberOfMessages(int numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public DestinationFactory getDestinationFactory() {
        return destinationFactory;
    }

    public void setDestinationFactory(DestinationFactory destinationFactory) {
        this.destinationFactory = destinationFactory;
    }

    public void sendMessages() throws JMSException {
        final StringBuilder buffer = new StringBuilder();

//        for (int i = 0; i < numberOfMessages; ++i) {
//            JmsTemplate jmsTemplate = new JmsTemplate(this.connectionFactory);
//            jmsTemplate.setDefaultDestination(destinationFactory.createDestination("TEST-"+i));
//
//            buffer.append("Message '").append(i).append("' sent at: ").append(new Date());
//
//            final int count = i;
//            final String payload = buffer.toString();
//
//            jmsTemplate.send(new MessageCreator() {
//                public Message createMessage(Session session) throws JMSException {
//                    TextMessage message = session.createTextMessage(payload);
//                    message.setIntProperty("messageCount", count);
//                    LOG.info("Sending message number '{}'", count);
//                    return message;
//                }
//            });
//
//            buffer.delete(0, buffer.length());
//        }
    }
}
