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
package org.n52.subverse.delivery.streamable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;
import org.n52.subverse.delivery.Streamable;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class StringStreamable implements Streamable {

    private final String payload;

    public StringStreamable(String payload) {
        Objects.requireNonNull(payload);
        this.payload = payload;
    }

    @Override
    public InputStream asStream() {
        byte[] bytes = payload.getBytes();
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public int getContentLength() {
        return payload.length();
    }

    @Override
    public Object originalObject() {
        return this.payload;
    }

}
