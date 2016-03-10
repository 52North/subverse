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
package org.n52.subverse.coding.capabilities.filter;

import java.util.ArrayList;
import java.util.List;
import net.opengis.fes.x20.ConformanceType;
import net.opengis.fes.x20.FilterCapabilitiesDocument;
import net.opengis.ows.x11.DomainType;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class FilterCapabilities {

    private List<FilterLanguage> languages;

    public FilterCapabilities() {
        //TODO make configurable, or dynamic from components
        this.languages = new ArrayList<>();
        this.languages.add(new FilterLanguage("OGC Filter Encoding Spec 2.0",
                "http://www.opengis.net/fes/2.0", createFilterCapDocument()));
    }

    private Object createFilterCapDocument() {
        FilterCapabilitiesDocument doc = FilterCapabilitiesDocument.Factory.newInstance();

        FilterCapabilitiesDocument.FilterCapabilities caps = doc.addNewFilterCapabilities();

        ConformanceType conf = caps.addNewConformance();

        DomainType c1 = conf.addNewConstraint();
        c1.setName("ImplementsMinSpatialFilter");
        c1.addNewNoValues();
        c1.addNewDefaultValue().setStringValue("true");

        DomainType c2 = conf.addNewConstraint();
        c2.setName("ImplementsTemporalFilter");
        c2.addNewNoValues();
        c2.addNewDefaultValue().setStringValue("true");

        return doc;
    }

    public List<FilterLanguage> getLanguages() {
        return languages;
    }

}
