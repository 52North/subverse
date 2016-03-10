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

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class FilterLanguage {

    private final String theAbstract;
    private final String identifier;
    private final Object supportedCapabilities;

    public FilterLanguage(String theAbstract, String identifier, Object supportedCapabilities) {
        this.theAbstract = theAbstract;
        this.identifier = identifier;
        this.supportedCapabilities = supportedCapabilities;
    }

    public String getTheAbstract() {
        return theAbstract;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Object getSupportedCapabilities() {
        return supportedCapabilities;
    }

}