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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class AmqpMessage {

    private final Object body;
    private final Optional<ContentType> contentType;
    private final Optional<String> subject;
    private final Map<String, String> deliveryAnnotations;
    private final Map<String, String> messageAnnotations;

    public AmqpMessage(Object body) {
        this(body, null, null);
    }

    public AmqpMessage(Object body, ContentType contentType) {
        this(body, contentType, null);
    }

    public AmqpMessage(Object body, String subject) {
        this(body, null, subject);
    }

    public AmqpMessage(Object body, ContentType contentType, String subject) {
        this(body, contentType, subject, null, null);
    }

    public AmqpMessage(Object body, ContentType contentType, String subject,
            Map<String, String> deliveryAnnotations, Map<String, String> messageAnnotations) {
        this.body = body;
        this.contentType = Optional.ofNullable(contentType);
        this.subject = Optional.ofNullable(subject);
        this.deliveryAnnotations = deliveryAnnotations != null ? deliveryAnnotations : Collections.emptyMap();
        this.messageAnnotations = messageAnnotations != null ? messageAnnotations : Collections.emptyMap();
    }

    public Object getBody() {
        return body;
    }

    public Optional<ContentType> getContentType() {
        return contentType;
    }

    public Optional<String> getSubject() {
        return subject;
    }

    public Map<String, String> getDeliveryAnnotations() {
        return deliveryAnnotations;
    }

    public Map<String, String> getMessageAnnotations() {
        return messageAnnotations;
    }

    @Override
    public String toString() {
        return String.format("AmqpMessage{%s, %s, %s}", body, getContentType(), getSubject());
    }



}
