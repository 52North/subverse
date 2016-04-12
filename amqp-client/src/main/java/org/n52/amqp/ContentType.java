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

package org.n52.amqp;

import java.util.Optional;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class ContentType {

    public static ContentType APPLICATION_XML = new ContentType("application/xml");
    public static ContentType APPLICATION_JSON = new ContentType("application/json");
    public static ContentType APPLICATION_SOAP_XML = new ContentType("application/soap+xml");
    public static ContentType TEXT_PLAIN = new ContentType("text/plain");

    private final String name;
    private final String encoding;

    public ContentType(String name) {
        this(name, null);
    }

    public ContentType(String name, String encoding) {
        this.name = name;
        this.encoding = encoding;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getEncoding() {
        return Optional.ofNullable(encoding);
    }

    @Override
    public String toString() {
        return String.format("ContentType {%s, %s}", name, getEncoding());
    }



}
