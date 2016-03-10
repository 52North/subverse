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
package org.n52.subverse.coding;

import java.util.Map;
import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.OperationDecoderKey;
import org.n52.iceland.exception.ows.CompositeOwsException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.util.http.MediaTypes;

public abstract class KvpDecoder<T> implements Decoder<T, Map<String, String>> {

    @Override
    @SuppressWarnings("ThrowableResultIgnored")
    public T decode(Map<String, String> parameters)
            throws OwsExceptionReport {
        CompositeOwsException exceptions = new CompositeOwsException();
        T t = createRequest();
        parameters.forEach((name, value) -> {
            try {
                decodeParameter(t, name, value);
            } catch (OwsExceptionReport e) {
                exceptions.add(e);
            }
        });
        exceptions.throwIfNotEmpty();
        return t;
    }

    protected abstract T createRequest();

    protected abstract void decodeParameter(T request, String name, String values)
            throws OwsExceptionReport;

    protected static DecoderKey createKey(String service, String version, String operation) {
        return new OperationDecoderKey(service, version, operation, MediaTypes.APPLICATION_KVP);
    }
}
