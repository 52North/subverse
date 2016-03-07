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
