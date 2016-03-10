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
package org.n52.subverse.coding.unsubscribe;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import net.opengis.pubsub.x10.SubscriptionIdentifierDocument;
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
import org.n52.subverse.request.UnsubscribeRequest;
import org.oasisOpen.docs.wsn.b2.UnsubscribeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnsubscribeDecoder implements Decoder<AbstractServiceRequest, String> {

    private static final Logger LOG = LoggerFactory.getLogger(UnsubscribeDecoder.class);
    private static final DecoderKey KEY = new XmlNamespaceOperationDecoderKey(SubverseConstants.WS_N_NAMESPACE,
            SubverseConstants.OPERATION_UNSUBSCRIBE);

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
        return Collections.singleton(KEY);
    }

}
