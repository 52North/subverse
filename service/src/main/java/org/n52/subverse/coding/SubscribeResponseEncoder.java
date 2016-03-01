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
package org.n52.subverse.coding;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.OperationRequestEncoderKey;
import org.n52.iceland.coding.encode.OperationResponseEncoderKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.iceland.ogc.ows.OWSConstants;
import org.n52.iceland.util.http.MediaType;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.response.SubscribeResponse;
import org.oasisOpen.docs.wsn.b2.SubscribeResponseDocument;
import org.w3.x2005.x08.addressing.AttributedURIType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
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
    
    @Override
    public XmlObject encode(SubscribeResponse objectToEncode) throws OwsExceptionReport, UnsupportedEncoderInputException {
        return encode(objectToEncode, Collections.emptyMap());
    }

    @Override
    public XmlObject encode(SubscribeResponse objectToEncode, Map<OWSConstants.HelperValues, String> additionalValues) throws OwsExceptionReport, UnsupportedEncoderInputException {
        SubscribeResponseDocument result = SubscribeResponseDocument.Factory.newInstance();
        SubscribeResponseDocument.SubscribeResponse resp = result.addNewSubscribeResponse();
        
        EndpointReferenceType ref = resp.addNewSubscriptionReference();
        AttributedURIType add = ref.addNewAddress();
        
        XmlCursor cur = add.newCursor();
        cur.toFirstContentToken();
        cur.insertChars("test");
        
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
