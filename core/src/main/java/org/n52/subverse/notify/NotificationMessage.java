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
package org.n52.subverse.notify;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import org.apache.xmlbeans.XmlObject;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class NotificationMessage {

    private final XmlObject message;
    private final Optional<String> topic;
    private final Optional<String> producerReference;
    private final Optional<String> subscriptionReference;

    public NotificationMessage(XmlObject message, Optional<String> topic,
            Optional<String> producerReference, Optional<String> subscriptionReference) {
        this.message = message;
        this.topic = topic;
        this.producerReference = producerReference;
        this.subscriptionReference = subscriptionReference;
    }

    public XmlObject getMessage() {
        return message;
    }

    public Optional<String> getTopic() {
        return topic;
    }

    public Optional<String> getProducerReference() {
        return producerReference;
    }

    public Optional<String> getSubscriptionReference() {
        return subscriptionReference;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).
                add("message", message).
                add("topic", topic).
                add("producerReference", producerReference).
                add("subscriptionReference", subscriptionReference).toString();
    }



}
