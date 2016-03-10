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
package org.n52.svalbard.soap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.EncoderRepository;
import org.n52.iceland.coding.encode.OperationResponseEncoderKey;
import org.n52.iceland.coding.encode.XmlEncoderKey;
import org.n52.iceland.exception.ows.NoApplicableCodeException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.NoEncoderForResponseException;
import org.n52.iceland.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.iceland.ogc.ows.OWSConstants;
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
    public Object encode(SoapResponse objectToEncode) throws OwsExceptionReport, UnsupportedEncoderInputException {
        return encode(objectToEncode, Collections.emptyMap());
    }

    @Override
    public Object encode(SoapResponse objectToEncode, Map<OWSConstants.HelperValues, String> additionalValues) throws OwsExceptionReport, UnsupportedEncoderInputException {
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

        if (bodyContent != null) {
            body.set(encodeBody(bodyContent));
        } else {
            OwsExceptionReport exception = objectToEncode.getException();
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
            return (XmlObject) encoder.encode(bodyContent);
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
            detail.set(new OwsExceptionReportEncoder().encode(targetException));
        } catch (OwsExceptionReport ex) {
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

}
