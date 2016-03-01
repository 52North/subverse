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
import java.util.Set;
import javax.inject.Inject;
import org.n52.iceland.coding.encode.EncoderRepository;
import org.n52.iceland.coding.encode.ResponseWriter;
import org.n52.iceland.coding.encode.ResponseWriterFactory;
import org.n52.iceland.coding.encode.ResponseWriterKey;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class SoapChainResponseWriterFactory implements ResponseWriterFactory {

    private EncoderRepository encoderRepository;

    public EncoderRepository getEncoderRepository() {
        return encoderRepository;
    }

    @Inject
    public void setEncoderRepository(EncoderRepository encoderRepository) {
        this.encoderRepository = encoderRepository;
    }
    
    @Override
    public Set<ResponseWriterKey> getKeys() {
        return Collections.singleton(SoapChainResponseWriter.KEY);
    }

    @Override
    public ResponseWriter<?> create(ResponseWriterKey key) {
        return new SoapChainResponseWriter(this.encoderRepository);
    }
    
}
