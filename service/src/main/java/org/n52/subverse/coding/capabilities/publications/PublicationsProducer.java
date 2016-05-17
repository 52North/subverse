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
package org.n52.subverse.coding.capabilities.publications;

import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.config.annotation.Setting;
import org.n52.iceland.util.Producer;
import org.n52.subverse.SubverseSettings;
import org.n52.subverse.publications.PublicationsProvider;
import org.n52.subverse.publications.PublicationsProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;


/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@Configurable
public class PublicationsProducer implements Producer<Publications>, PublicationsProvider {

    private String publicationsString;

    @Autowired
    private PublicationsProviderRepository providerRepository;
    private String rootPublicationIdentifier;

    @Setting(SubverseSettings.PUBLICATIONS)
    public PublicationsProducer setPublicationsString(String ps) {
        this.publicationsString = ps;
        return this;
    }

    @Setting(SubverseSettings.ROOT_PUBLICATION)
    public PublicationsProvider setRootPublicationIdentifier(String pubId) {
        this.rootPublicationIdentifier = pubId;
        return this;
    }

    @Override
    public Publications get() {
        if (providerRepository == null) {
            return new Publications(publicationsString);
        }
        else {
            return new Publications(providerRepository.getProviders());
        }
    }

    @Override
    public String getIdentifier() {
        return rootPublicationIdentifier;
    }

    @Override
    public String getAbstract() {
        return "root publication providing all data available";
    }

    @Override
    public String getContentType() {
        return null;
    }

}
