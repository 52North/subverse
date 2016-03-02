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
package org.n52.svalbard.soap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.EncoderRepository;
import org.n52.iceland.coding.encode.OperationResponseEncoderKey;
import org.n52.iceland.coding.encode.XmlEncoderKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.NoEncoderForResponseException;
import org.n52.iceland.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.iceland.ogc.ows.OWSConstants;
import org.n52.iceland.response.AbstractServiceResponse;
import org.n52.iceland.util.http.MediaType;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.iceland.w3c.soap.SoapResponse;
import org.w3.x2003.x05.soapEnvelope.Body;
import org.w3.x2003.x05.soapEnvelope.Envelope;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class SoapEnvelopeEncoder implements Encoder<XmlObject, SoapResponse> {

    private static final EncoderKey KEY = new XmlEncoderKey(Envelope.type.getName().getNamespaceURI(),
            SoapResponse.class);

    private EncoderRepository encoderRepository;

    public EncoderRepository getEncoderRepository() {
        return encoderRepository;
    }

    @Inject
    public void setEncoderRepository(EncoderRepository encoderRepository) {
        this.encoderRepository = encoderRepository;
    }

    @Override
    public XmlObject encode(SoapResponse objectToEncode) throws OwsExceptionReport, UnsupportedEncoderInputException {
        return encode(objectToEncode, Collections.emptyMap());
    }

    @Override
    public XmlObject encode(SoapResponse objectToEncode, Map<OWSConstants.HelperValues, String> additionalValues) throws OwsExceptionReport, UnsupportedEncoderInputException {
        AbstractServiceResponse bodyContent = objectToEncode.getBodyContent();

        EnvelopeDocument envDoc = EnvelopeDocument.Factory.newInstance();
        Envelope env = envDoc.addNewEnvelope();

        Body body = env.addNewBody();

        body.set(encodeBody(bodyContent));

        return envDoc;
    }

    @Override
    public MediaType getContentType() {
        return MediaTypes.APPLICATION_SOAP_XML;
    }

    @Override
    public Set<EncoderKey> getKeys() {
        return Collections.singleton(KEY);
    }

    private XmlObject encodeBody(AbstractServiceResponse bodyContent) throws OwsExceptionReport {
        EncoderKey key = new OperationResponseEncoderKey(bodyContent.getService(), bodyContent.getVersion(),
                bodyContent.getOperationKey().getOperation(), MediaTypes.APPLICATION_XML);
        Encoder<Object, Object> encoder = this.encoderRepository.getEncoder(key);

        if (encoder != null) {
            return (XmlObject) encoder.encode(bodyContent);
        }

        throw new NoEncoderForResponseException().withMessage("No encoder found for key: "+key);
    }

}
