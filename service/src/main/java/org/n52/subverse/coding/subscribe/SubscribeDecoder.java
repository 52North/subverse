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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import net.opengis.pubsub.x10.DeliveryMethodDocument;
import net.opengis.pubsub.x10.DeliveryMethodType;
import net.opengis.pubsub.x10.PublicationIdentifierDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.DecodingException;
import org.n52.iceland.coding.decode.OperationDecoderKey;
import org.n52.iceland.coding.decode.XmlNamespaceOperationDecoderKey;
import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.exception.CodedException;
import org.n52.iceland.exception.ows.InvalidParameterValueException;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.coding.XmlBeansHelper;
import org.n52.subverse.coding.capabilities.filter.FilterCapabilitiesProducer;
import org.n52.subverse.coding.capabilities.publications.Publications;
import org.n52.subverse.coding.capabilities.publications.PublicationsProducer;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.request.SubscribeRequest;
import org.n52.subverse.subscription.SubscribeOptions;
import org.n52.subverse.util.InvalidTerminationTimeException;
import org.n52.subverse.util.TerminationTimeHelper;
import org.oasisOpen.docs.wsn.b2.FilterType;
import org.oasisOpen.docs.wsn.b2.MessageContentDocument;
import org.oasisOpen.docs.wsn.b2.QueryExpressionType;
import org.oasisOpen.docs.wsn.b2.SubscribeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2005.x08.addressing.AttributedURIType;

@Configurable
public class SubscribeDecoder implements Decoder<AbstractServiceRequest, String> {

    private static final Logger LOG = LoggerFactory.getLogger(SubscribeDecoder.class);
    private static final DecoderKey KEY = new XmlNamespaceOperationDecoderKey(SubverseConstants.WS_N_NAMESPACE,
            SubverseConstants.OPERATION_SUBSCRIBE);
    private static final DecoderKey DCP_KEY = new OperationDecoderKey(SubverseConstants.SERVICE,
            SubverseConstants.VERSION, SubverseConstants.OPERATION_SUBSCRIBE, MediaTypes.APPLICATION_XML);

    private static final QName PUBLICATION_ID_QN = PublicationIdentifierDocument.type.getDocumentElementName();
    private static final QName DELIVERY_METHOD_QN = DeliveryMethodDocument.type.getDocumentElementName();
    private static final QName USE_RAW_QN = new QName(SubverseConstants.WS_N_NAMESPACE, "UseRaw");

    private PublicationsProducer publicationsProducer;
    private FilterCapabilitiesProducer filterProducer;

    @Inject
    public void setPublicationsProducer(PublicationsProducer publicationsProducer) {
        this.publicationsProducer = publicationsProducer;
    }

    @Inject
    public void setFilterProducer(FilterCapabilitiesProducer filterProducer) {
        this.filterProducer = filterProducer;
    }

    @Override
    public AbstractServiceRequest decode(String objectToDecode) throws DecodingException {
        SubscribeDocument subDoc;
        try {
            subDoc = SubscribeDocument.Factory.parse(objectToDecode);
        } catch (XmlException ex) {
            LOG.warn("Could not decode Subscribe XML", ex);
            throw new DecodingException("Could not decode Subscribe XML", ex);
        }

        /*
        * Publication id
        */
        SubscribeDocument.Subscribe subscribe = subDoc.getSubscribe();
        Optional<String> pubId = XmlBeansHelper.findFirstChild(PUBLICATION_ID_QN, subscribe)
                .map(c -> XmlBeansHelper.extractStringContent(c));

        //check if this publication is defined
        Publications pubs = this.publicationsProducer.get();
        long matching = pubs.getPublicationList().stream().filter(p -> p.getIdentifier().equals(pubId.get())).count();
        if (matching == 0) {
            CodedException e = new InvalidPublicationIdentifierFault(
                    String.format("Publication identifier '%s' is not registered with this service. Valid are: %s",
                            pubId.get(), pubs.getPublicationList()));
            throw new DecodingException(e.getMessage(), e);
        }

        /*
        * delivery method id
        */
        DeliveryDefinition deliveryDef = null;
        String deliveryIdentifier = null;
        Optional<XmlObject> delivery = XmlBeansHelper.findFirstChild(DELIVERY_METHOD_QN, subscribe);
        if (delivery.isPresent()) {
            DeliveryMethodType deliveryElem = (DeliveryMethodType) delivery.get();
            deliveryIdentifier = deliveryElem.getIdentifier();
        }

        boolean useRaw = false;
        if (subscribe.isSetSubscriptionPolicy()) {
            SubscribeDocument.Subscribe.SubscriptionPolicy policy = subscribe.getSubscriptionPolicy();
            Optional<XmlObject> useRawElem = XmlBeansHelper.findFirstChild(USE_RAW_QN, policy);
            if (useRawElem.isPresent()) {
                useRaw = true;
            }
        }

        /*
         * delivery location
         */
        AttributedURIType consumer = subscribe.getConsumerReference().getAddress();
        deliveryDef = new DeliveryDefinition(deliveryIdentifier, consumer.getStringValue(), pubId.get(), useRaw);

        /*
        * termination time
        */
        DateTime terminationTime = null;
        if (subscribe.isSetInitialTerminationTime()) {
            try {
                terminationTime = TerminationTimeHelper.parseDateTime(subscribe.xgetInitialTerminationTime());
            } catch (InvalidTerminationTimeException ex) {
                CodedException e = new UnacceptableInitialTerminationTimeFault(ex.getMessage()).causedBy(ex);
                throw new DecodingException(e.getMessage(), e);
            }
            if (terminationTime.isBeforeNow()) {
                CodedException e = new UnacceptableInitialTerminationTimeFault(
                        "The termination time must be in the future: "+terminationTime);
                throw new DecodingException(e.getMessage(), e);
            }
        }

        /*
        * filter
        */
        String filterLanguage;
        Optional<XmlObject> filter;
        try {
            filterLanguage = parseFilterLanguage(subscribe);
            if (filterLanguage != null) {
                checkValidFilterLanguage(filterLanguage);
                filter = extractFilter(subscribe);
            }
            else {
                filter = Optional.empty();
            }
        } catch (InvalidParameterValueException e) {
            throw new DecodingException(e.getMessage(), e);
        }

        /*
        * TODO: parse deliveryParameters
        * TODO: content type is not supported yet by SOAP binding
        */

        SubscribeOptions options = new SubscribeOptions(pubId.get(),
                terminationTime,
                filter.orElse(null),
                filterLanguage,
                deliveryDef,
                Collections.emptyMap(),
                null);
        return new SubscribeRequest(options);
    }

    @Override
    public Set<DecoderKey> getKeys() {
        Set<DecoderKey> keys = new HashSet<>();
        keys.add(DCP_KEY);
        keys.add(KEY);
        return keys;
    }


    private String parseFilterLanguage(SubscribeDocument.Subscribe subscribe) throws InvalidParameterValueException {
        Optional<QueryExpressionType> content = extractFilterContent(subscribe);

        if (content.isPresent()) {
            String dialect = content.get().getDialect();
            if (dialect == null || dialect.isEmpty()) {
                /*
                 * PubSub SOAP Req 7
                 */
                throw new InvalidParameterValueException("wns:Dialect", dialect);
            }
            return dialect.trim();
        }

        return null;
    }

    private Optional<XmlObject> extractFilter(SubscribeDocument.Subscribe subscribe) {
        Optional<QueryExpressionType> content = extractFilterContent(subscribe);

        if (content.isPresent()) {
            return XmlBeansHelper.findFirstChild(content.get());
        }

        return Optional.empty();
    }

    private Optional<QueryExpressionType> extractFilterContent(SubscribeDocument.Subscribe subscribe) {
        if (subscribe.isSetFilter()) {
            FilterType filter = subscribe.getFilter();
            Optional<XmlObject> content = XmlBeansHelper.findFirstChild(
                    MessageContentDocument.type.getDocumentElementName(), filter);

            return Optional.ofNullable(content.isPresent() ? (QueryExpressionType) content.get() : null);
        }

        return Optional.empty();
    }

    private void checkValidFilterLanguage(String filterLanguage) throws InvalidParameterValueException {
        long matching = this.filterProducer.get().getLanguages().stream().filter(fl -> {
            return filterLanguage.equals(fl.getIdentifier());
        }).count();

        if (matching == 0) {
            /*
             * PubSub SOAP Req 8
             */
            throw new InvalidParameterValueException("wsn:Dialect", filterLanguage);
        }
    }

}
