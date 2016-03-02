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
import net.opengis.fes.x20.FilterType;
import org.joda.time.DateTime;

public class SubscribeOptions {

    private final String publicationIdentifier;
    private final Optional<DateTime> terminationTime;
    private final Optional<FilterType> filter;
    private final Optional<String> filterLanguageId;
    private final Optional<String> deliveryLocation;
    private final Optional<String> deliveryMethod;
    private final Map<String, String> deliveryParameters;
    private final Optional<String> contentType;

    public SubscribeOptions(String publicationIdentifier,
            Optional<DateTime> terminationTime,
            Optional<FilterType> filter,
            Optional<String> filterLanguageId,
            Optional<String> deliveryLocation,
            Optional<String> deliveryMethod,
            Map<String, String> deliveryParameters,
            Optional<String> contentType) {
        this.publicationIdentifier = publicationIdentifier;
        this.terminationTime = terminationTime;
        this.filter = filter;
        this.filterLanguageId = filterLanguageId;
        this.deliveryLocation = deliveryLocation;
        this.deliveryMethod = deliveryMethod;
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
                .add("deliveryLocation", deliveryLocation)
                .add("deliveryMethod", deliveryMethod)
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

    public Optional<FilterType> getFilter() {
        return filter;
    }

    public Optional<String> getFilterLanguageId() {
        return filterLanguageId;
    }

    public Optional<String> getDeliveryLocation() {
        return deliveryLocation;
    }

    public Optional<String> getDeliveryMethod() {
        return deliveryMethod;
    }

    public Map<String, String> getDeliveryParameters() {
        return deliveryParameters;
    }

    public Optional<String> getContentType() {
        return contentType;
    }

}
