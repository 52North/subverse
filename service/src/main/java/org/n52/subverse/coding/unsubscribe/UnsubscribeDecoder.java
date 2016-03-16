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
package org.n52.subverse.coding.unsubscribe;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.opengis.pubsub.x10.SubscriptionIdentifierDocument;
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
import org.n52.subverse.coding.XmlBeansHelper;
import org.n52.subverse.request.UnsubscribeRequest;
import org.oasisOpen.docs.wsn.b2.UnsubscribeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnsubscribeDecoder implements Decoder<AbstractServiceRequest, String> {

    private static final Logger LOG = LoggerFactory.getLogger(UnsubscribeDecoder.class);
    private static final DecoderKey KEY = new XmlNamespaceOperationDecoderKey(SubverseConstants.WS_N_NAMESPACE,
            SubverseConstants.OPERATION_UNSUBSCRIBE);
    private static final DecoderKey DCP_KEY = new OperationDecoderKey(SubverseConstants.SERVICE,
            SubverseConstants.VERSION, SubverseConstants.OPERATION_UNSUBSCRIBE, MediaTypes.APPLICATION_XML);

    @Override
    public AbstractServiceRequest decode(String objectToDecode) throws OwsExceptionReport, UnsupportedDecoderInputException {
        UnsubscribeDocument subDoc;
        try {
            subDoc = UnsubscribeDocument.Factory.parse(objectToDecode);
        } catch (XmlException ex) {
            LOG.warn("Could not decode Unsubscribe XML", ex);
            throw new UnsupportedDecoderInputException(this, ex);
        }

        UnsubscribeDocument.Unsubscribe unsub = subDoc.getUnsubscribe();

        Optional<XmlObject> identifier = XmlBeansHelper.findFirstChild(
                SubscriptionIdentifierDocument.type.getDocumentElementName(), unsub);

        if (!identifier.isPresent()) {
            throw new ResourceUnknownFault("No SubscriptionIdentifier provided.");
        }

        String subId = XmlBeansHelper.extractStringContent(identifier.get());

        if (subId == null || subId.isEmpty()) {
            throw new ResourceUnknownFault("Invalid SubscriptionIdentifier provided.");
        }

        return new UnsubscribeRequest(subId);
    }

    @Override
    public Set<DecoderKey> getKeys() {
        Set<DecoderKey> keys = new HashSet<>();
        keys.add(DCP_KEY);
        keys.add(KEY);
        return keys;
    }

}
