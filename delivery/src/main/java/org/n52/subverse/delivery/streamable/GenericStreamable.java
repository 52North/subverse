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

import java.io.InputStream;
import org.n52.subverse.delivery.Streamable;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class GenericStreamable implements Streamable {

    private final InputStream stream;
    private final String contentType;
    private final int contentLength;
    private final Object originalObject;


    public GenericStreamable(InputStream s, String ct, int length, Object o) {
        this.stream = s;
        this.contentType = ct;
        this.contentLength = length;
        this.originalObject = o;
    }

    @Override
    public InputStream asStream() {
        return this.stream;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public int getContentLength() {
        return this.contentLength;
    }

    @Override
    public Object originalObject() {
        return this.originalObject;
    }

}
