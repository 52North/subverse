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
package org.n52.subverse.coding.getsubscription;

import com.google.common.collect.Sets;
import org.n52.subverse.request.GetSubscriptionRequest;
import java.util.Objects;
import java.util.Set;
import net.opengis.pubsub.x10.GetSubscriptionDocument;
import net.opengis.pubsub.x10.GetSubscriptionType;
import org.apache.xmlbeans.XmlException;
import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.DecodingException;
import org.n52.iceland.coding.decode.OperationDecoderKey;
import org.n52.iceland.coding.decode.XmlNamespaceOperationDecoderKey;
import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.subverse.SubverseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
@Configurable
public class GetSubscriptionDecoder implements Decoder<AbstractServiceRequest, String> {

    private static final Logger LOG = LoggerFactory.getLogger(GetSubscriptionDecoder.class);

    private static final DecoderKey KEY = new XmlNamespaceOperationDecoderKey(SubverseConstants.PUB_SUB_NAMESPACE,
            SubverseConstants.OPERATION_GET_SUBSCRIPTION);
    private static final DecoderKey DCP_KEY = new OperationDecoderKey(SubverseConstants.SERVICE,
            SubverseConstants.VERSION, SubverseConstants.OPERATION_GET_SUBSCRIPTION, MediaTypes.APPLICATION_XML);

    @Override
    public AbstractServiceRequest decode(String objectToDecode) throws DecodingException {
        Objects.requireNonNull(objectToDecode);

        GetSubscriptionDocument getSubDoc;
        try {
            getSubDoc = GetSubscriptionDocument.Factory.parse(objectToDecode);
        } catch (XmlException ex) {
            LOG.warn("Could not parse request", ex);
            throw new DecodingException("Could not parse request", ex);
        }

        GetSubscriptionType getSub = getSubDoc.getGetSubscription();
        String[] identifiers = getSub.getSubscriptionIdentifierArray();

        if (identifiers == null) {
            return new GetSubscriptionRequest();
        }

        return new GetSubscriptionRequest(identifiers);
    }

    @Override
    public Set<DecoderKey> getKeys() {
        return Sets.newHashSet(KEY, DCP_KEY);
    }

}
