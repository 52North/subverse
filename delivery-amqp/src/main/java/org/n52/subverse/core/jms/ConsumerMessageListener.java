package org.n52.subverse.core.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ConsumerMessageListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerMessageListener.class);

    public void onMessage(Message message) {
        try {
            TextMessage msg = (TextMessage) message;
            LOG.info("Consumed message: " + msg.getText());
        } catch (JMSException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

}
