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
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.DecoderRepository;
import org.n52.iceland.coding.decode.XmlNamespaceOperationDecoderKey;
import org.n52.iceland.exception.CodedException;
import org.n52.iceland.exception.ows.NoApplicableCodeException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.NoDecoderForKeyException;
import org.n52.iceland.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.w3c.soap.SoapRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.Envelope;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2003.x05.soapEnvelope.Header;

public class SoapEnvelopeDecoder implements Decoder<SoapRequest, String> {

    private static final Logger LOG = LoggerFactory.getLogger(SoapEnvelopeDecoder.class);
    private static final DecoderKey KEY = new XmlNamespaceOperationDecoderKey(Envelope.type.getName().getNamespaceURI(),
            Envelope.type.getName().getLocalPart());

    private DecoderRepository decoderRepository;

    @Override
    public SoapRequest decode(String xml) throws OwsExceptionReport, UnsupportedDecoderInputException {
        Objects.requireNonNull(xml);

        EnvelopeDocument envDoc;
        Envelope env;
        try {
            envDoc = EnvelopeDocument.Factory.parse(xml);
            env = envDoc.getEnvelope();
        } catch (XmlException ex) {
            LOG.warn("Could not decode XML", ex);
            throw new NoApplicableCodeException().causedBy(ex).withMessage(ex.getMessage());
        }

        Header header = null;
        if (env.isSetHeader()) {
            header = env.getHeader();
        }

        AbstractServiceRequest innerRequest = internalDecode(new SoapEnvelopeContainer<>(null, header, env.getBody()));
        SoapRequest soap = new SoapRequest(Envelope.type.getName().getNamespaceURI(), "1.2");
        soap.setSoapBodyContent(innerRequest);
        return soap;
    }

    @Override
    public Set<DecoderKey> getKeys() {
        return Collections.singleton(KEY);
    }

    @Inject
    public void setDecoderRepository(DecoderRepository decoderRepository) {
        this.decoderRepository = decoderRepository;
    }

    public DecoderRepository getDecoderRepository() {
        return decoderRepository;
    }


    private AbstractServiceRequest internalDecode(SoapEnvelopeContainer<XmlObject> env) throws OwsExceptionReport {
        XmlObject body = env.getBody();
        XmlNamespaceOperationDecoderKey targetKey = createKey(body);

        Decoder<Object, Object> decoder = this.decoderRepository.getDecoder(targetKey);

        if (decoder != null) {
            //TODO: a lot of XML de/serializing - find a better solution
            Object request = decoder.decode(body.xmlText());

            if (request instanceof AbstractServiceRequest) {
                return (AbstractServiceRequest) request;
            }

            throw new NoApplicableCodeException()
                    .withMessage("invalid decoder response."
                            + " Expected AbstractServiceRequest but got "
                            +request.getClass());
        }

        throw new NoDecoderForKeyException(targetKey);
    }

    private XmlNamespaceOperationDecoderKey createKey(XmlObject body) throws CodedException {
        XmlCursor cur = body.newCursor();

        XmlObject elem;
        if (cur.toFirstChild()) {
            elem = cur.getObject();
            cur.dispose();
        }
        else {
            throw new NoApplicableCodeException()
                    .withMessage("No body in this soap:Envelope");
        }

        QName qn;
        if (elem.schemaType() != null && elem.schemaType().getName() != null) {
            qn = elem.schemaType().getName();
        }
        else {
            qn = new QName(elem.getDomNode().getNamespaceURI(), elem.getDomNode().getLocalName());
        }
        return new XmlNamespaceOperationDecoderKey(qn.getNamespaceURI(), qn.getLocalPart());
    }
}
