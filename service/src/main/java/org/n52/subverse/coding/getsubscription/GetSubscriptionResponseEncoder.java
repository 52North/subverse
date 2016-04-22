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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.opengis.pubsub.x10.DeliveryMethodDocument;
import net.opengis.pubsub.x10.DeliveryMethodType;
import net.opengis.pubsub.x10.GetSubscriptionResponseDocument;
import net.opengis.pubsub.x10.GetSubscriptionResponseType;
import net.opengis.pubsub.x10.SubscriptionIdentifierDocument;
import net.opengis.pubsub.x10.SubscriptionType;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.HelperValues;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.EncodingException;
import org.n52.iceland.coding.encode.OperationResponseEncoderKey;
import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.util.http.MediaType;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.coding.XmlBeansHelper;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.response.GetSubscriptionResponse;
import org.n52.subverse.subscription.Subscription;
import org.oasisOpen.docs.wsn.b2.ConsumerReferenceDocument;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@Configurable
public class GetSubscriptionResponseEncoder implements Encoder<XmlObject, GetSubscriptionResponse> {

    private static final Set<EncoderKey> ENCODER_KEYS = Sets.<EncoderKey>newHashSet(
            new OperationResponseEncoderKey(SubverseConstants.SERVICE,
                    SubverseConstants.VERSION,
                    SubverseConstants.OPERATION_GET_SUBSCRIPTION,
                    MediaTypes.TEXT_XML),
            new OperationResponseEncoderKey(SubverseConstants.SERVICE,
                    SubverseConstants.VERSION,
                    SubverseConstants.OPERATION_GET_SUBSCRIPTION,
                    MediaTypes.APPLICATION_XML));

    @Override
    public XmlObject encode(GetSubscriptionResponse objectToEncode) throws EncodingException {
        return encode(objectToEncode, Collections.emptyMap());
    }

    @Override
    public XmlObject encode(GetSubscriptionResponse objectToEncode, Map<HelperValues, String> additionalValues) throws EncodingException {
        GetSubscriptionResponseDocument result = GetSubscriptionResponseDocument.Factory.newInstance();
        GetSubscriptionResponseType resp = result.addNewGetSubscriptionResponse();

        List<Subscription> subscriptions = objectToEncode.getSubscriptions();

        for (Subscription subscriptionObject : subscriptions) {
            SubscriptionType sub = resp.addNewSubscription();
            XmlObject loc = sub.addNewDeliveryLocation();

            Optional<DeliveryDefinition> deliveryObject = subscriptionObject.getOptions().getDeliveryDefinition();
            if (deliveryObject.isPresent()) {
                DeliveryMethodDocument deliveryDoc = DeliveryMethodDocument.Factory.newInstance();
                DeliveryMethodType delivery = deliveryDoc.addNewDeliveryMethod();
                delivery.setIdentifier(deliveryObject.get().getIdentifier());

                XmlBeansHelper.insertChild(loc, deliveryDoc);
            }

            ConsumerReferenceDocument conRefDoc = ConsumerReferenceDocument.Factory.newInstance();
            EndpointReferenceType conRef = conRefDoc.addNewConsumerReference();
            conRef.addNewAddress().setStringValue(subscriptionObject.getEndpoint().getDeliveryEndpoint().getEffectiveLocation());

            XmlBeansHelper.insertChild(loc, conRefDoc);

            SubscriptionIdentifierDocument subIdDoc = SubscriptionIdentifierDocument.Factory.newInstance();
            subIdDoc.setSubscriptionIdentifier(subscriptionObject.getId());

            XmlBeansHelper.insertChild(loc, subIdDoc);
        }

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

}
