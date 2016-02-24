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

import javax.inject.Inject;

import org.n52.iceland.coding.encode.EncoderRepository;
import org.n52.iceland.coding.encode.ResponseWriter;
import org.n52.iceland.coding.encode.ResponseWriterFactory;
import org.n52.iceland.coding.encode.ResponseWriterKey;
import org.n52.iceland.coding.encode.ResponseWriterRepository;
import org.n52.iceland.component.SingleTypeComponentFactory;

/**
 * @author Christian Autermann
 */
public class ServiceResponseWriterFactory
        implements ResponseWriterFactory,
                   SingleTypeComponentFactory<ResponseWriterKey, ResponseWriter<?>> {

    private ResponseWriterRepository responseWriterRepository;

    private EncoderRepository encoderRepository;

    @Inject
    public void setEncoderRepository(EncoderRepository encoderRepository) {
        this.encoderRepository = encoderRepository;
    }

    @Inject
    public void setResponseWriterRepository(
            ResponseWriterRepository responseWriterRepository) {
        this.responseWriterRepository = responseWriterRepository;
    }

    @Override
    public ResponseWriterKey getKey() {
        return ServiceResponseWriter.KEY;
    }

    @Override
    public ResponseWriter<?> create() {
        return new ServiceResponseWriter(this.responseWriterRepository,
                                         this.encoderRepository);
    }

}
