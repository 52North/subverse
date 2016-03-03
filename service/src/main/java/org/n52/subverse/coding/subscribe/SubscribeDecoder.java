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
package org.n52.subverse.coding.subscribe;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import javax.xml.namespace.QName;
import net.opengis.pubsub.x10.DeliveryMethodDocument;
import net.opengis.pubsub.x10.DeliveryMethodType;
import net.opengis.pubsub.x10.PublicationIdentifierDocument;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.XmlNamespaceOperationDecoderKey;
import org.n52.iceland.exception.CodedException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.util.DateTimeHelper;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.coding.XmlBeansHelper;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.request.SubscribeRequest;
import org.n52.subverse.subscription.SubscribeOptions;
import org.oasisOpen.docs.wsn.b2.AbsoluteOrRelativeTimeType;
import org.oasisOpen.docs.wsn.b2.FilterType;
import org.oasisOpen.docs.wsn.b2.MessageContentDocument;
import org.oasisOpen.docs.wsn.b2.QueryExpressionType;
import org.oasisOpen.docs.wsn.b2.SubscribeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2005.x08.addressing.AttributedURIType;

public class SubscribeDecoder implements Decoder<AbstractServiceRequest, String> {

    private static final Logger LOG = LoggerFactory.getLogger(SubscribeDecoder.class);
    private static final DecoderKey KEY = new XmlNamespaceOperationDecoderKey(SubverseConstants.WS_N_NAMESPACE,
            SubverseConstants.OPERATION_SUBSCRIBE);

    private static final QName PUBLICATION_ID_QN = PublicationIdentifierDocument.type.getDocumentElementName();
    private static final QName DELIVERY_METHOD_QN = DeliveryMethodDocument.type.getDocumentElementName();

    @Override
    public AbstractServiceRequest decode(String objectToDecode) throws OwsExceptionReport, UnsupportedDecoderInputException {
        SubscribeDocument subDoc;
        try {
            subDoc = SubscribeDocument.Factory.parse(objectToDecode);
        } catch (XmlException ex) {
            LOG.warn("Could not decode Subscribe XML", ex);
            throw new UnsupportedDecoderInputException(this, ex);
        }

        /*
        * Publication id
        */
        SubscribeDocument.Subscribe subscribe = subDoc.getSubscribe();
        Optional<String> pubId = XmlBeansHelper.findFirstChild(PUBLICATION_ID_QN, subscribe)
                .map(c -> XmlBeansHelper.extractStringContent(c));

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

        /*
         * delivery location
         */
        AttributedURIType consumer = subscribe.getConsumerReference().getAddress();
        deliveryDef = new DeliveryDefinition(deliveryIdentifier, consumer.getStringValue());

        /*
        * termination time
        */
        DateTime terminationTime = null;
        if (subscribe.isSetInitialTerminationTime()) {
            terminationTime = parseDateTime(subscribe.xgetInitialTerminationTime());
            if (terminationTime.isBeforeNow()) {
                throw new UnacceptableInitialTerminationTimeFault(
                        "The termination time must be in the futuer: "+terminationTime);
            }
        }

        /*
        * filter
        */
        String filterLanguage = parseFilterLanguage(subscribe);

        Optional<XmlObject> filter;
        if (filterLanguage != null) {
            filter = extractFilter(subscribe);
        }
        else {
            filter = Optional.empty();
        }

        /*
        * TODO: parse deliveryParameters
        * TODO: content type is not supported yet by SOAP binding
        */

        SubscribeOptions options = new SubscribeOptions(pubId.get(),
                Optional.ofNullable(terminationTime),
                filter,
                Optional.ofNullable(filterLanguage),
                Optional.ofNullable(deliveryDef),
                Collections.emptyMap(),
                Optional.ofNullable(null));
        return new SubscribeRequest(options);
    }

    @Override
    public Set<DecoderKey> getKeys() {
        return Collections.singleton(KEY);
    }

    protected DateTime parseDateTime(AbsoluteOrRelativeTimeType time) throws CodedException {
        SchemaType type = time.instanceType();

        if (type == null || type.getName() == null) {
            try {
                return DateTimeHelper.makeDateTime(time.getStringValue());
            }
            catch (Exception e) {
                throw new UnacceptableInitialTerminationTimeFault(
                        "Cannot parse date time object").causedBy(e);
            }
        }

        if (type.getName().getLocalPart().equals("dateTime")) {
            return DateTimeHelper.makeDateTime(time.getStringValue());
        }
        else if (type.getName().getLocalPart().equals("duration")) {
            String string = time.getStringValue().trim();

            if (string.startsWith("-")) {
                throw new UnacceptableInitialTerminationTimeFault(
                        "Termination time cannot be in the past");
            }

            if (string.startsWith("P")) {
                PeriodFormatter formatter = ISOPeriodFormat.standard();
                Period period = formatter.parsePeriod(string);
                return DateTime.now().plus(period);
            }
        }

        throw new UnacceptableInitialTerminationTimeFault(
                        "Cannot determine type of date time object");
    }

    private String parseFilterLanguage(SubscribeDocument.Subscribe subscribe) {
        Optional<QueryExpressionType> content = extractFilterContent(subscribe);

        if (content.isPresent()) {
            return content.get().getDialect().trim();
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

}
