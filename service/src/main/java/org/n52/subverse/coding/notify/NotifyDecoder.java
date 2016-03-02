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
package org.n52.subverse.coding.notify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.XmlNamespaceOperationDecoderKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.notify.NotificationMessage;
import org.n52.subverse.request.NotifyRequest;
import org.oasisOpen.docs.wsn.b2.NotificationMessageHolderType;
import org.oasisOpen.docs.wsn.b2.NotifyDocument;
import org.oasisOpen.docs.wsn.b2.TopicExpressionType;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class NotifyDecoder implements Decoder<AbstractServiceRequest, String> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NotifyDecoder.class);
    private static final DecoderKey KEY = new XmlNamespaceOperationDecoderKey(SubverseConstants.WS_N_NAMESPACE,
        SubverseConstants.OPERATION_NOTIFY);

    private static final String SIMPLE_TOPIC_DIALECT = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple";


    @Override
    public AbstractServiceRequest decode(String objectToDecode) throws OwsExceptionReport, UnsupportedDecoderInputException {
        NotifyDocument doc;
        try {
            doc = NotifyDocument.Factory.parse(objectToDecode);
        } catch (XmlException ex) {
            LOG.warn("Could not parse Notify XML document", ex);
            throw new UnsupportedDecoderInputException(this, objectToDecode);
        }

        NotifyDocument.Notify notify = doc.getNotify();


        List<NotificationMessage> messages = new ArrayList<>(notify.sizeOfNotificationMessageArray());
        for (NotificationMessageHolderType n : notify.getNotificationMessageArray()) {
            XmlCursor cur;
            String procducerAddress = null;
            if (n.isSetProducerReference()) {
                procducerAddress = n.getProducerReference().getAddress().getStringValue();
            }

            String subRef = null;
            if (n.isSetSubscriptionReference()) {
                subRef = n.getSubscriptionReference().getAddress().getStringValue();
            }

            String topic = null;
            if (n.isSetTopic() && n.getTopic().getDialect().equals(SIMPLE_TOPIC_DIALECT)) {
                TopicExpressionType topicElem = n.getTopic();
                cur = topicElem.newCursor();
                cur.toFirstContentToken();
                topic = cur.getTextValue();

                if (topic != null) {
                    topic = topic.trim();
                }
            }

            NotificationMessageHolderType.Message messageHolder = n.getMessage();
            cur = messageHolder.newCursor();
            cur.toFirstChild();
            XmlObject message = cur.getObject();

            messages.add(new NotificationMessage(message, Optional.ofNullable(topic),
                    Optional.ofNullable(procducerAddress), Optional.ofNullable(subRef)));
        }

        return new NotifyRequest(messages.stream());
    }

    @Override
    public Set<DecoderKey> getKeys() {
        return Collections.singleton(KEY);
    }

}
