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
package org.n52.subverse;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.config.annotation.Setting;
import org.n52.iceland.exception.ConfigurationError;
import static org.n52.iceland.service.ServiceSettings.SERVICE_URL;
import org.n52.iceland.util.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author <a href="mailto:d.nuest@52north.org">Daniel Nüst</a>
 */
@Controller
@RequestMapping(value = "/endpoint", produces = MediaType.APPLICATION_JSON_VALUE)
@Configurable
public class JsonEndpoint {

    private static final Logger log = LoggerFactory.getLogger(JsonEndpoint.class);

    private static Properties gitProps;

    private static Properties versionProps;

    private String serviceURL;

    @Setting(SERVICE_URL)
    public void setServiceURL(final URI serviceURL) throws ConfigurationError {
        Validation.notNull("Service URL", serviceURL);
        String url = serviceURL.toString();
        if (url.contains("?")) {
            url = url.split("[?]")[0];
        }
        this.serviceURL = url;
    }

    public JsonEndpoint() {
        gitProps = new Properties();
        gitProps.put("git.branch", "wwowowowo");
        gitProps.put("git.commit.id", "wwowowowo1");
        gitProps.put("git.build.time", "wwowowowo2");

        URL verPropUrl = Resources.getResource("version.properties");
        ByteSource verSource = Resources.asByteSource(verPropUrl);
        versionProps = new Properties();
        try (InputStream in = verSource.openStream()) {
            log.info("Loading version properties from {} [via {}]", verPropUrl, in);
            versionProps.load(in);
        } catch (IOException e) {
            log.error("Could not load version properties", e);
        }

        log.info("NEW {}", this);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> info() {
        Map<String, Object> infoMap = Maps.newHashMap();

        infoMap.put("endpoint", serviceURL);
        try {
            infoMap.put("version",
                    ImmutableMap.of("branch", gitProps.get("git.branch"),
                            "commitid", gitProps.get("git.commit.id"),
                            "buildtime", gitProps.get("git.build.time")));
        } catch (RuntimeException e) {
            log.error("Error retrieving git information from {}", gitProps, e);
        }
        try {
            infoMap.put("build",
                    ImmutableMap.of("version", versionProps.get("build.version"),
                            "date", versionProps.get("build.date")));
        } catch (RuntimeException e) {
            log.error("Error retrieving version information from {}", versionProps, e);
        }

        return infoMap;
    }

}
