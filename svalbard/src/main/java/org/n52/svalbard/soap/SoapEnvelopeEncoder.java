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
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.HelperValues;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.EncoderRepository;
import org.n52.iceland.coding.encode.EncodingException;
import org.n52.iceland.coding.encode.ExceptionEncoderKey;
import org.n52.iceland.coding.encode.OperationResponseEncoderKey;
import org.n52.iceland.coding.encode.XmlEncoderKey;
import org.n52.iceland.exception.ows.NoApplicableCodeException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.NoEncoderForResponseException;
import org.n52.iceland.response.AbstractServiceResponse;
import org.n52.iceland.response.NoContentResponse;
import org.n52.iceland.util.http.HTTPStatus;
import org.n52.iceland.util.http.MediaType;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.iceland.util.http.NoContent;
import org.n52.iceland.w3c.soap.SoapResponse;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.Body;
import org.w3.x2003.x05.soapEnvelope.Detail;
import org.w3.x2003.x05.soapEnvelope.Envelope;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2003.x05.soapEnvelope.Fault;
import org.w3.x2003.x05.soapEnvelope.FaultDocument;
import org.w3.x2003.x05.soapEnvelope.Faultcode;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class SoapEnvelopeEncoder implements Encoder<Object, SoapResponse> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SoapEnvelopeEncoder.class);

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
    public Object encode(SoapResponse objectToEncode) throws EncodingException {
        return encode(objectToEncode, Collections.emptyMap());
    }

    @Override
    public Object encode(SoapResponse objectToEncode, Map<HelperValues, String> additionalValues) throws EncodingException {
        AbstractServiceResponse bodyContent = objectToEncode.getBodyContent();

        /*
        * Special case: NoContent
         */
        if (bodyContent instanceof NoContentResponse) {
            return new NoContent();
        }

        EnvelopeDocument envDoc = EnvelopeDocument.Factory.newInstance();
        Envelope env = envDoc.addNewEnvelope();

        Body body = env.addNewBody();

        OwsExceptionReport exception = null;
        if (bodyContent != null) {
            try {
                body.set(encodeBody(bodyContent));
            }
            catch (OwsExceptionReport e) {
                exception = e;
                LOG.warn(exception.getMessage(), exception);
            }
        } else {
            exception = objectToEncode.getException();
        }

        if (exception != null) {
            body.set(encodeException(exception));
        }

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
            try {
                return (XmlObject) encoder.encode(bodyContent);
            } catch (EncodingException ex) {
                throw new NoApplicableCodeException().withMessage(ex.getMessage()).causedBy(ex);
            }
        }

        throw new NoEncoderForResponseException().withMessage("No encoder found for key: " + key);
    }

    private XmlObject encodeException(OwsExceptionReport exception) {
        OwsExceptionReport targetException;
        if (exception == null) {
            targetException = new NoApplicableCodeException().withMessage("Unexpected error");
        } else {
            targetException = exception;
        }

        FaultDocument faultDoc = FaultDocument.Factory.newInstance();
        Fault fault = faultDoc.addNewFault();
        Faultcode code = fault.addNewCode();
        code.setValue(new QName(EnvelopeDocument.type.getDocumentElementName().getNamespaceURI(),
                determineCauser(targetException.getStatus())));

        if (targetException instanceof SoapFault) {
            fault.addNewReason().addNewText().setStringValue(
                    ((SoapFault) targetException).getReason());
        }

        Detail detail = fault.addNewDetail();
        try {
            ExceptionEncoderKey excKey = new ExceptionEncoderKey(MediaTypes.APPLICATION_SOAP_XML);
            Encoder<XmlObject, OwsExceptionReport> encoder = this.encoderRepository.getEncoder(excKey);
            if (encoder == null) {
                encoder = new OwsExceptionReportEncoder();
            }
            XmlObject owsXml = encoder.encode(targetException);
            detail.set(owsXml);
        } catch (EncodingException ex) {
            LOG.warn("Error encoding OwsExceptionReport", ex);
        }

        return faultDoc;
    }

    private String determineCauser(HTTPStatus status) {
        if (status == null) {
            return "Sender";
        }

        return status.isClientError() ? "Sender" : "Receiver";
    }

    @Override
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(EnvelopeDocument.type.getDocumentElementName().getNamespaceURI(), "soap12");
    }



}
