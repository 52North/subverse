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
package org.n52.subverse.coding.subscribe;

import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.opengis.pubsub.x10.SubscriptionIdentifierDocument;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.OperationResponseEncoderKey;
import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.config.annotation.Setting;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.iceland.ogc.ows.OWSConstants;
import org.n52.iceland.service.ServiceSettings;
import org.n52.iceland.util.http.MediaType;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.response.SubscribeResponse;
import org.n52.subverse.subscription.Subscription;
import org.oasisOpen.docs.wsn.b2.SubscribeResponseDocument;
import org.w3.x2005.x08.addressing.AttributedURIType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.w3.x2005.x08.addressing.ReferenceParametersType;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
@Configurable
public class SubscribeResponseEncoder implements Encoder<XmlObject, SubscribeResponse> {

    private static final Set<EncoderKey> ENCODER_KEYS = Sets.<EncoderKey>newHashSet(
            new OperationResponseEncoderKey(SubverseConstants.SERVICE,
                    SubverseConstants.VERSION,
                    SubverseConstants.OPERATION_SUBSCRIBE,
                    MediaTypes.TEXT_XML),
            new OperationResponseEncoderKey(SubverseConstants.SERVICE,
                    SubverseConstants.VERSION,
                    SubverseConstants.OPERATION_SUBSCRIBE,
                    MediaTypes.APPLICATION_XML));
    private URI serviceURL;

    @Setting(ServiceSettings.SERVICE_URL)
    public void setServiceURL(URI serviceURL) {
        this.serviceURL = serviceURL;
    }

    @Override
    public XmlObject encode(SubscribeResponse objectToEncode) throws OwsExceptionReport, UnsupportedEncoderInputException {
        return encode(objectToEncode, Collections.emptyMap());
    }

    @Override
    public XmlObject encode(SubscribeResponse objectToEncode, Map<OWSConstants.HelperValues, String> additionalValues) throws OwsExceptionReport, UnsupportedEncoderInputException {
        SubscribeResponseDocument result = SubscribeResponseDocument.Factory.newInstance();
        SubscribeResponseDocument.SubscribeResponse resp = result.addNewSubscribeResponse();

        Subscription subscriptionObject = objectToEncode.getSubscription();

        /*
         * tiem stamps
         */
        resp.setCurrentTime(new DateTime(DateTimeZone.UTC).toCalendar(Locale.getDefault()));
        Optional<DateTime> termTime = subscriptionObject.getOptions().getTerminationTime();
        if (termTime.isPresent()) {
            resp.setTerminationTime(termTime.get().toCalendar(Locale.getDefault()));
        }

        /*
        * sub ID
        */

        EndpointReferenceType ref = resp.addNewSubscriptionReference();
        AttributedURIType add = ref.addNewAddress();
        add.setStringValue(this.serviceURL.toString());

        ReferenceParametersType refParams = ref.addNewReferenceParameters();
        SubscriptionIdentifierDocument subIdDoc = SubscriptionIdentifierDocument.Factory.newInstance();
        subIdDoc.setSubscriptionIdentifier(subscriptionObject.getId());
        refParams.set(subIdDoc);

        return result;
    }

    @Override
    public MediaType getContentType() {
        return MediaTypes.APPLICATION_XML;
    }

    @Override
    public Set<EncoderKey> getKeys() {
        return ENCODER_KEYS;
    }

    @Override
    public void addNamespacePrefixToMap(Map<String, String> prefixMap) {
        prefixMap.put(SubverseConstants.PUB_SUB_NAMESPACE, "pubsub");
        prefixMap.put(SubverseConstants.WS_N_NAMESPACE, "wsn");
    }



}
