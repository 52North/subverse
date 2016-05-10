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

import com.google.common.base.Objects;
import java.io.Serializable;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DeliveryParameter implements Serializable {

    private final String type;
    private final String namespace;
    private final String elementName;
    private final String value;

    public DeliveryParameter(String type, String namespace, String elementName, String value) {
        this.type = type;
        this.namespace = namespace;
        this.elementName = elementName;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getElementName() {
        return elementName;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.namespace,
                this.type,
                this.elementName,
                this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeliveryParameter other = (DeliveryParameter) obj;
        return Objects.equal(this.namespace, other.namespace)
            && Objects.equal(this.type, other.type)
            && Objects.equal(this.elementName, other.elementName)
            && Objects.equal(this.value, other.value);
    }

}
