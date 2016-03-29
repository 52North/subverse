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
package org.n52.subverse.coding;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.DecoderRepository;
import org.n52.iceland.coding.decode.DecodingException;
import org.n52.iceland.coding.decode.XmlNamespaceDecoderKey;
import org.n52.iceland.exception.ows.NoApplicableCodeException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.NoDecoderForKeyException;
import org.n52.iceland.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.iceland.service.AbstractServiceCommunicationObject;
import org.n52.subverse.subscription.SubscribeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class DelegatingStringDecoder
        implements Decoder<AbstractServiceCommunicationObject, String> {

    private static final Logger log = LoggerFactory.getLogger(DelegatingStringDecoder.class);

    private DecoderRepository decoderRepository;

    private final Set<DecoderKey> keys;

    private JAXBContext context;

    public DelegatingStringDecoder(Set<DecoderKey> keys) throws JAXBException {
        this.keys = Objects.requireNonNull(keys);
        this.context = JAXBContext.newInstance(SubscribeOptions.class);
    }

    @Inject
    public void setDecoderRepository(DecoderRepository decoderRepository) {
        this.decoderRepository = decoderRepository;
    }

    @Override
    public Set<DecoderKey> getKeys() {
        return Collections.unmodifiableSet(this.keys);
    }

    @Override
    public AbstractServiceCommunicationObject decode(String string)
            throws DecodingException {
        try {
            JAXBElement<Object> xmlObject = asXmlElement(string);
            DecoderKey key = new XmlNamespaceDecoderKey(getDocumentNamespace(string), xmlObject.getDeclaredType());
            Decoder<AbstractServiceCommunicationObject, JAXBElement> delegate = getDelegate(key);

            log.trace("Delegated decoding to {} based on key {}", delegate, key);
            return delegate.decode(xmlObject);
        } catch (JAXBException | IOException | ParserConfigurationException | SAXException | NoDecoderForKeyException ex) {
            throw new DecodingException(String.format("Error while decoding request string: \n%s", string), ex);
        }
    }

    private JAXBElement asXmlElement(String string) throws JAXBException {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        JAXBElement<Object> elem = unmarshaller.unmarshal(new StreamSource(new StringReader(string)), Object.class);
        return elem;
    }

    private Decoder<AbstractServiceCommunicationObject, JAXBElement> getDelegate(DecoderKey decoderKey)
            throws NoDecoderForKeyException {
        Decoder<AbstractServiceCommunicationObject, JAXBElement> decoder = this.decoderRepository.getDecoder(decoderKey);
        if (decoder == null) {
            throw new NoDecoderForKeyException(decoderKey);
        }
        return decoder;
    }

    // FIXME overkill to parse to document just to get the namespace...
    private String getDocumentNamespace(String string) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.isIgnoringComments();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        InputSource source = new InputSource(new StringReader(string));
        Document document = documentBuilder.parse(source);
        return document.getDocumentElement().getNamespaceURI();
    }

}
