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
* Copyright 2015-2016 52°North Initiative for Geospatial Open Source
* Software GmbH
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.n52.subverse.wsdl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Scanner;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.n52.subverse.util.RequestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The servlet of the Service which receives the incoming HttpPost and HttpGet
 * requests and sends the operation result documents to the client TODO review
 * exception handling
 *
 * @since 1.0.0
 */
@Controller
@RequestMapping(value = WsdlService.RESOURCE, consumes = "*/*", produces = WsdlService.WSDL_MIME_TYPE)
public class WsdlService extends HttpServlet {
    public static final String WSDL_MIME_TYPE = "application/wsdl+xml";
    public static final String RESOURCE = "/wsdl";
    private static final Logger LOGGER = LoggerFactory.getLogger(WsdlService.class);

    @RequestMapping(method = RequestMethod.GET)
    public void get(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(name = "f", required = false) String format) throws IOException, URISyntaxException {
        String serviceUrl = RequestUtils.resolveFullRequestUrl(request);
        serviceUrl = serviceUrl.substring(0, serviceUrl.indexOf(RESOURCE));

        InputStream res = getClass().getResourceAsStream("SubscriptionManager-template.xml");
        Scanner sc = new Scanner(res);
        StringBuilder sb = new StringBuilder();
        String sep = System.getProperty("line.separator");
        while (sc.hasNext()) {
            sb.append(sc.nextLine().replace("${serviceUrl}", serviceUrl));
            sb.append(sep);
        }

        try (ServletOutputStream os = response.getOutputStream()) {
            response.setStatus(HttpStatus.OK.value());

            if (format != null && !format.isEmpty()) {
                response.setContentType(determineContentType(format));
            }
            else {
                response.setContentType(WSDL_MIME_TYPE);
            }
            response.setContentLength(sb.length());

            os.write(sb.toString().getBytes(Charset.forName("utf8")));
            os.flush();
        } catch (IOException ex) {
            LOGGER.warn(ex.getMessage(), ex);
        }
    }

    private String determineContentType(String format) {
        switch (format) {
            case "xml":
                return "application/xml";
            default:
                return WSDL_MIME_TYPE;
        }
    }

}
