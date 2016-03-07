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
package org.n52.subverse;

import org.n52.iceland.ogc.ows.OwsCapabilities;
import org.n52.subverse.coding.capabilities.delivery.DeliveryCapabilities;
import org.n52.subverse.coding.capabilities.filter.FilterCapabilities;
import org.n52.subverse.coding.capabilities.publications.Publications;

public class SubverseCapabilities extends OwsCapabilities {

    private FilterCapabilities filterCapabilities;
    private DeliveryCapabilities deliveryCapabilities;
    private Publications publications;

    public SubverseCapabilities(String version) {
        super(SubverseConstants.SERVICE, version);
    }

    public void setFilterCapabilities(FilterCapabilities fc) {
        this.filterCapabilities = fc;
    }

    public void setDeliveryCapabilities(DeliveryCapabilities dc) {
        this.deliveryCapabilities = dc;
    }

    public void setPublications(Publications ps) {
        this.publications = ps;
    }

}
