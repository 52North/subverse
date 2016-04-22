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
package org.n52.subverse.util;

import java.util.List;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.n52.subverse.delivery.DeliveryParameter;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DeliveryParameterXmlHelper {

    public static XmlObject createDeliveryParameters(List<DeliveryParameter> parameters) {
        if (parameters.isEmpty()) {
            return null;
        }

        XmlObject xo = XmlObject.Factory.newInstance();
        XmlCursor cur = xo.newCursor();
        cur.toNextToken();

        parameters.forEach(param -> {
            createElement(cur, param);
        });

        cur.dispose();

        return xo;
    }

    private static void createElement(XmlCursor cur, DeliveryParameter param) {
        cur.beginElement(new QName(param.getNamespace(), param.getElementName()));

        if (!param.hasChildren()) {
            cur.insertChars(param.getValue());
        }
        else {
            param.getChildren().forEach(child -> {
                createElement(cur, child);
            });
        }

        cur.toEndDoc();
    }


}
