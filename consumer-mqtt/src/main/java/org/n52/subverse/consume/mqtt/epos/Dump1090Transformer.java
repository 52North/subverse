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
package org.n52.subverse.consume.mqtt.epos;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.n52.epos.event.EposEvent;
import org.n52.epos.event.MapEposEvent;
import org.n52.epos.transform.EposTransformer;
import org.n52.epos.transform.TransformationException;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class Dump1090Transformer implements EposTransformer {

    private final JsonFactory factory;
    private final ObjectMapper mapper;
    private final TypeReference<HashMap<String, Object>> typeRef;

    public Dump1090Transformer() {
        this.factory = new JsonFactory();
        this.mapper = new ObjectMapper(factory);
        this.typeRef = new TypeReference<HashMap<String,Object>>() {};
    }



    @Override
    public EposEvent transform(Object input) throws TransformationException {
        try {
            Map<String,Object> o = mapper.readValue((String) input, typeRef);
            Object tsObj = o.get("timestamp");
            long ts = 0;
            if (tsObj instanceof Integer) {
                ts = (int) tsObj;
            }
            else if (tsObj instanceof String) {
                ts = Long.parseLong((String) tsObj);
            }

            MapEposEvent event = new MapEposEvent(ts, ts);

            o.keySet().stream().forEach((k) -> {
                event.put(k, o.get(k));
            });

            event.setOriginalObject(input);
            event.setContentType("application/json");
            return event;
        } catch (IOException ex) {
            throw new TransformationException("Could not parse JSON", ex);
        }
    }

    @Override
    public boolean supportsInput(Object input) {
        if (input instanceof String) {
            String str = (String) input;
            if (str.contains("\"seen_pos\"") && str.contains("\"timestamp\"")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public short getPriority() {
        return 0;
    }

}
