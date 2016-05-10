/*
 * Copyright (C) 2016-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
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
import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.subverse.delivery.DeliveryDefinition;

public class SubscribeOptions implements Serializable {

    private final String publicationIdentifier;
    private final DateTime terminationTime;
    private final XmlObject filter;
    private final String filterLanguageId;
    private final DeliveryDefinition deliveryDefinition;
    private final Map<String, String> deliveryParameters;
    private final String contentType;

    public SubscribeOptions(String publicationIdentifier,
            DateTime terminationTime,
            XmlObject filter,
            String filterLanguageId,
            DeliveryDefinition deliveryDef,
            Map<String, String> deliveryParameters,
            String contentType) {
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
        return Optional.ofNullable(terminationTime);
    }

    public Optional<XmlObject> getFilter() {
        return Optional.ofNullable(filter);
    }

    public Optional<String> getFilterLanguageId() {
        return Optional.ofNullable(filterLanguageId);
    }

    public Optional<DeliveryDefinition> getDeliveryDefinition() {
        return Optional.ofNullable(deliveryDefinition);
    }

    public Map<String, String> getDeliveryParameters() {
        return deliveryParameters;
    }

    public Optional<String> getContentType() {
        return Optional.ofNullable(contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.publicationIdentifier,
                this.terminationTime,
                this.filter,
                this.filterLanguageId,
                this.deliveryDefinition,
                this.deliveryParameters,
                this.contentType);
    }



    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SubscribeOptions other = (SubscribeOptions) obj;
        return Objects.equal(this.publicationIdentifier, other.publicationIdentifier)
            && Objects.equal(this.terminationTime, other.terminationTime)
            && Objects.equal(this.filter != null ? this.filter.toString() : null,
                    other.filter != null ? other.filter.toString() : null)
            && Objects.equal(this.filterLanguageId, other.filterLanguageId)
            && Objects.equal(this.deliveryDefinition, other.deliveryDefinition)
            && Objects.equal(this.deliveryParameters, other.deliveryParameters)
            && Objects.equal(this.contentType, other.contentType);
    }

}
