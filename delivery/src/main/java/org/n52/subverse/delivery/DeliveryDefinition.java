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
package org.n52.subverse.delivery;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DeliveryDefinition {

    private final String identifier;
    private final String location;
    private final String publicationId;
    private final List<DeliveryParameter> parameters = new ArrayList<>();

    public DeliveryDefinition(String identifier, String location, String publicationId) {
        this.identifier = identifier;
        this.location = location;
        this.publicationId = publicationId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getLocation() {
        return location;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void addParameter(DeliveryParameter param) {
        this.parameters.add(param);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
               .add("identifier", identifier)
               .add("location", location)
               .toString();
    }

    public List<DeliveryParameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }



}
