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

    public FilterCapabilities getFilterCapabilities() {
        return filterCapabilities;
    }

    public DeliveryCapabilities getDeliveryCapabilities() {
        return deliveryCapabilities;
    }

    public Publications getPublications() {
        return publications;
    }

    public boolean isSetFilterCapabilities() {
        return filterCapabilities != null;
    }

    public boolean isSetDeliveryCapabilities() {
        return deliveryCapabilities != null;
    }

    public boolean isSetPublications() {
        return publications != null;
    }

}
