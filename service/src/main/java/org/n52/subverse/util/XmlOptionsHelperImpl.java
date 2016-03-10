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
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package org.n52.subverse.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.xmlbeans.XmlOptions;
import org.n52.iceland.coding.CodingRepository;
import org.n52.subverse.SubverseConstants;
import org.n52.svalbard.xml.XmlOptionsHelper;
import org.springframework.stereotype.Component;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
@Component
public class XmlOptionsHelperImpl implements XmlOptionsHelper {

    private static final String CHARACTER_ENCODING = "UTF-8";

    private final ReentrantLock lock = new ReentrantLock();
    private XmlOptions options;

    @Override
    public XmlOptions create() {
        if (options == null) {
            lock.lock();
            try {
                if (options == null) {
                    options = new XmlOptions();
                    final Map<String, String> prefixes = getPrefixMap();
                    options.setSaveSuggestedPrefixes(prefixes);
                    options.setSaveImplicitNamespaces(prefixes);
                    options.setSaveAggressiveNamespaces();
                    options.setSavePrettyPrint();
                    options.setSaveNamespacesFirst();
                    options.setCharacterEncoding(CHARACTER_ENCODING);
                }
            } finally {
                lock.unlock();
            }
        }
        return options;
    }

    private Map<String, String> getPrefixMap() {
        final Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put(SubverseConstants.PUB_SUB_NAMESPACE, "pubsub");
        prefixMap.put(SubverseConstants.WS_N_NAMESPACE, "wsn");
        CodingRepository.getInstance().getEncoders().stream().forEach((encoder) -> {
            encoder.addNamespacePrefixToMap(prefixMap);
        });
        return prefixMap;
    }

}
