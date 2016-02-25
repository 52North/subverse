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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.Envelope;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2003.x05.soapEnvelope.Header;

public class SoapEnvelopeDecoder implements Decoder<AbstractServiceRequest, String> {

    private static final Logger LOG = LoggerFactory.getLogger(SoapEnvelopeDecoder.class);
    private static final DecoderKey KEY = new XmlNamespaceOperationDecoderKey(Envelope.type.getName().getNamespaceURI(),
            Envelope.type.getName().getLocalPart());

    private DecoderRepository decoderRepository;

    @Override
    public AbstractServiceRequest decode(String xml) throws OwsExceptionReport, UnsupportedDecoderInputException {
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

        return internalDecode(new SoapEnvelopeContainer<>(null, header, env.getBody()));
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
