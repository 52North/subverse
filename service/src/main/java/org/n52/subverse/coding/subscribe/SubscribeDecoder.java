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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.XmlNamespaceOperationDecoderKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.coding.XmlBeansHelper;
import org.n52.subverse.request.SubscribeRequest;
import org.n52.subverse.subscription.SubscribeOptions;
import org.oasisOpen.docs.wsn.b2.SubscribeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        SubscribeDocument.Subscribe subscribe = subDoc.getSubscribe();
        Optional<String> pubId = XmlBeansHelper.findFirstChild(PUBLICATION_ID_QN, subscribe)
                .map(c -> XmlBeansHelper.extractStringContent(c));

        String deliveryIdentifier = null;
        Optional<XmlObject> delivery = XmlBeansHelper.findFirstChild(DELIVERY_METHOD_QN, subscribe);
        if (delivery.isPresent()) {
            DeliveryMethodType deliveryElem = (DeliveryMethodType) delivery.get();
            deliveryIdentifier = deliveryElem.getIdentifier();
        }

        /*
        * TODO: parse other parameters
        */

        SubscribeOptions options = new SubscribeOptions(pubId.get(),
                Optional.ofNullable(null),
                Optional.ofNullable(null),
                Optional.ofNullable(null),
                Optional.ofNullable(null),
                Optional.ofNullable(deliveryIdentifier),
                Collections.emptyMap(),
                Optional.ofNullable(null));
        return new SubscribeRequest(options);
    }

    @Override
    public Set<DecoderKey> getKeys() {
        return Collections.singleton(KEY);
    }

}
