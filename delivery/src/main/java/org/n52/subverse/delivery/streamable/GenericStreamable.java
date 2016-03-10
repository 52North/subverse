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
package org.n52.subverse.delivery.streamable;

import java.io.InputStream;
import org.n52.subverse.delivery.Streamable;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class GenericStreamable implements Streamable {

    private final InputStream stream;
    private final String contentType;
    private final int contentLength;
    private final Object originalObject;


    public GenericStreamable(InputStream s, String ct, int length, Object o) {
        this.stream = s;
        this.contentType = ct;
        this.contentLength = length;
        this.originalObject = o;
    }

    @Override
    public InputStream asStream() {
        return this.stream;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public int getContentLength() {
        return this.contentLength;
    }

    @Override
    public Object originalObject() {
        return this.originalObject;
    }

}
