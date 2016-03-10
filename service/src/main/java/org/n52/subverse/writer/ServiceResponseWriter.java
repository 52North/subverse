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
package org.n52.subverse.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;

import org.n52.iceland.coding.encode.AbstractResponseWriter;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.EncoderRepository;
import org.n52.iceland.coding.encode.OperationRequestEncoderKey;
import org.n52.iceland.coding.encode.ResponseProxy;
import org.n52.iceland.coding.encode.ResponseWriter;
import org.n52.iceland.coding.encode.ResponseWriterKey;
import org.n52.iceland.coding.encode.ResponseWriterRepository;
import org.n52.iceland.exception.ows.NoApplicableCodeException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.NoEncoderForKeyException;
import org.n52.iceland.request.ResponseFormat;
import org.n52.iceland.response.AbstractServiceResponse;
import org.n52.iceland.util.http.MediaType;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann, Daniel Nüst
 */
public class ServiceResponseWriter extends AbstractResponseWriter<AbstractServiceResponse> {

    public static final ResponseWriterKey KEY
            = new ResponseWriterKey(AbstractServiceResponse.class);

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ServiceResponseWriter.class);

    private final ResponseWriterRepository responseWriterRepository;

    private final EncoderRepository encoderRepository;

    public ServiceResponseWriter(ResponseWriterRepository responseWriterRepository, EncoderRepository encoderRepository) {
        this.responseWriterRepository = responseWriterRepository;
        this.encoderRepository = encoderRepository;
    }

    @Override
    public void write(AbstractServiceResponse asr, OutputStream out,
            ResponseProxy responseProxy)
            throws OwsExceptionReport {
        Encoder<Object, AbstractServiceResponse> encoder = getEncoder(asr);
        if (encoder == null) {
            log.debug("No encoder found for response {}", asr);
            return;
        }
        // use encoded Object specific writer, e.g. JaxbWriter
        Object encode = null;
        try {
            encode = encoder.encode(asr);
        } catch (RuntimeException e) {
            log.warn("Unexpected error", e);
            throw new NoApplicableCodeException().withMessage("Internal server error during encoding.").causedBy(e);
        }
        if (encode == null) {
            log.warn("Encoding returned null!");
            return;
        }

        ResponseWriter<Object> writer = this.responseWriterRepository
                .getWriter(encode.getClass());
        if (writer == null) {
            throw new NoApplicableCodeException().withMessage("No writer for %s found!", encode.getClass());
        }

        try {
            writer.write(encode, out, responseProxy);
        } catch (IOException e) {
            throw new NoApplicableCodeException().withMessage("Internal server error.").causedBy(e);
        }
    }

    @Override
    public boolean supportsGZip(AbstractServiceResponse asr) {
        return true;
    }

    /**
     * Get the {@link Encoder} for the {@link AbstractServiceResponse} and the
     * requested contentType
     *
     * @param asr
     *            {@link AbstractServiceResponse} to get {@link Encoder} for
     *
     * @return {@link Encoder} for the {@link AbstractServiceResponse}
     */
    private Encoder<Object, AbstractServiceResponse> getEncoder(AbstractServiceResponse asr) {
        MediaType contentType = getEncodedContentType(asr);
        OperationRequestEncoderKey key = new OperationRequestEncoderKey(asr.getOperationKey(), contentType);
        Encoder<Object, AbstractServiceResponse> encoder = getEncoder(key);
        if (encoder == null) {
            throw new RuntimeException(new NoEncoderForKeyException(key));
        }
        return encoder;
    }

    /**
     * Getter for encoder, encapsulates the instance call
     *
     * @param key
     *            Encoder key
     * @param <D> destination of the encoder
     * @param <S> source for the encoder
     * @return Matching encoder
     */
    protected <D, S> Encoder<D, S> getEncoder(EncoderKey key) {
        return this.encoderRepository.getEncoder(key);
    }

    private MediaType getEncodedContentType(AbstractServiceResponse response) {
        if (response instanceof ResponseFormat) {
            return getEncodedContentType((ResponseFormat) response);
        }
        return getContentType();
    }

    @Override
    public Set<ResponseWriterKey> getKeys() {
        return Collections.singleton(KEY);
    }
}
