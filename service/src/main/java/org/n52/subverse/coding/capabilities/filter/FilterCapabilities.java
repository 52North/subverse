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



}
