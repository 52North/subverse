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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.iceland.coding.encode.AbstractResponseWriter;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.EncoderRepository;
import org.n52.iceland.coding.encode.ResponseProxy;
import org.n52.iceland.coding.encode.ResponseWriterKey;
import org.n52.iceland.coding.encode.XmlEncoderKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.NoEncoderForKeyException;
import org.n52.iceland.response.NoContentResponse;
import org.n52.iceland.util.http.HTTPStatus;
import org.n52.iceland.util.http.NoContent;
import org.n52.iceland.w3c.soap.SoapChain;
import org.n52.iceland.w3c.soap.SoapResponse;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class SoapChainResponseWriter extends AbstractResponseWriter<SoapChain> {

    public static final ResponseWriterKey KEY = new ResponseWriterKey(SoapChain.class);
    private final EncoderRepository encoderRepository;

    SoapChainResponseWriter(EncoderRepository encoderRepository) {
        this.encoderRepository = encoderRepository;
    }

    @Override
    public void write(SoapChain chain, OutputStream out, ResponseProxy responseProxy) throws IOException, OwsExceptionReport {
        Object o = encodeSoapResponse(chain);
        if (o != null) {
            if (o instanceof XmlObject) {
                ((XmlObject) o).save(out, new XmlOptions().setSavePrettyPrint());
            }
        }
    }

    private Object encodeSoapResponse(SoapChain chain) throws OwsExceptionReport {
        EncoderKey key = new XmlEncoderKey(chain.getSoapResponse().getSoapNamespace(), chain.getSoapResponse().getClass());
        Encoder<?, SoapResponse> encoder = this.encoderRepository.getEncoder(key);
        if (encoder != null) {
            return encoder.encode(chain.getSoapResponse());
        } else {
            throw new NoEncoderForKeyException(key);
        }
    }

    @Override
    public boolean supportsGZip(SoapChain t) {
        return false;
    }

    @Override
    public Set<ResponseWriterKey> getKeys() {
        return Collections.singleton(KEY);
    }

    @Override
    public boolean hasForcedHttpStatus(SoapChain t) {
        return t.getBodyResponse() instanceof NoContentResponse;
    }

    @Override
    public HTTPStatus getForcedHttpStatus(SoapChain t) {
        return t.getBodyResponse() instanceof NoContentResponse ? HTTPStatus.NO_CONTENT : HTTPStatus.OK;
    }

}
