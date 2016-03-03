/*
* Copyright 2015 52°North Initiative for Geospatial Open Source Software GmbH.
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
package org.n52.subverse.subscription;

import com.google.common.base.MoreObjects;
import java.util.Map;
import java.util.Optional;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.subverse.delivery.DeliveryDefinition;

public class SubscribeOptions {

    private final String publicationIdentifier;
    private final Optional<DateTime> terminationTime;
    private final Optional<XmlObject> filter;
    private final Optional<String> filterLanguageId;
    private final Optional<DeliveryDefinition> deliveryDefinition;
    private final Map<String, String> deliveryParameters;
    private final Optional<String> contentType;

    public SubscribeOptions(String publicationIdentifier,
            Optional<DateTime> terminationTime,
            Optional<XmlObject> filter,
            Optional<String> filterLanguageId,
            Optional<DeliveryDefinition> deliveryDef,
            Map<String, String> deliveryParameters,
            Optional<String> contentType) {
        this.publicationIdentifier = publicationIdentifier;
        this.terminationTime = terminationTime;
        this.filter = filter;
        this.filterLanguageId = filterLanguageId;
        this.deliveryDefinition = deliveryDef;
        this.deliveryParameters = deliveryParameters;
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("publicationIdentifier", publicationIdentifier)
                .add("terminationTime", terminationTime)
                .add("filter", filter)
                .add("filterLanguageId", filterLanguageId)
                .add("deliveryLocation", deliveryDefinition)
                .add("deliveryParameters", deliveryParameters)
                .add("contentType", contentType)
                .toString();
    }

    public String getPublicationIdentifier() {
        return publicationIdentifier;
    }

    public Optional<DateTime> getTerminationTime() {
        return terminationTime;
    }

    public Optional<XmlObject> getFilter() {
        return filter;
    }

    public Optional<String> getFilterLanguageId() {
        return filterLanguageId;
    }

    public Optional<DeliveryDefinition> getDeliveryDefinition() {
        return deliveryDefinition;
    }

    public Map<String, String> getDeliveryParameters() {
        return deliveryParameters;
    }

    public Optional<String> getContentType() {
        return contentType;
    }

}
