/*
 * Copyright (C) 2016-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.subverse.coding.notify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.OperationDecoderKey;
import org.n52.iceland.coding.decode.XmlNamespaceOperationDecoderKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.util.http.MediaTypes;
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
    private static final DecoderKey DCP_KEY = new OperationDecoderKey(SubverseConstants.SERVICE,
            SubverseConstants.VERSION, SubverseConstants.OPERATION_NOTIFY, MediaTypes.APPLICATION_XML);

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
        Set<DecoderKey> keys = new HashSet<>();
        keys.add(DCP_KEY);
        keys.add(KEY);
        return keys;
    }

}
