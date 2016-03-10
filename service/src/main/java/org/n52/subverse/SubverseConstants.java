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

import org.n52.iceland.exception.CodedException;
import org.n52.iceland.exception.ows.InvalidParameterValueException;

public interface SubverseConstants {

    String WS_N_NAMESPACE = "http://docs.oasis-open.org/wsn/b-2";

    String PUB_SUB_NAMESPACE = "http://www.opengis.net/pubsub/1.0";

    String SERVICE = "PubSub";

    String VERSION = "1.0.0";

    String OPERATION_GET_CAPABILITIES = "GetCapabilities";

    String OPERATION_SUBSCRIBE = "Subscribe";

    String OPERATION_RENEW = "Renew";

    String OPERATION_UNSUBSCRIBE = "Unsubscribe";

    String OPERATION_GET_SUBSCRIPTION = "GetSubscription";

    String OPERATION_NOTIFY = "Notify";

    interface DemoParam {

        String OUTPUT_FORMAT = "outputFormat";

    }

    interface Param {

        String SERVICE = "service";
        String REQUEST = "request";
        String VERSION = "version";

    }

    interface GetCapabilitiesParam extends Param {

        String SECTIONS = "Sections";
        String SECTIONS_LOWERCASE = "sections";
        String UPDATE_SEQUENCE = "updateSequence";
        String UPDATE_SEQUENCE_LOWERCASE = "updatesequence";
        String ACCCEPTVERSIONS = "AcceptVersions";
        String ACCCEPTVERSIONS_LOWERCASE = "acceptversions";
        String ACCEPT_FORMATS = "acceptformats";
        String ACCEPT_FORMATS_LOWERCASE = "AcceptFormats";
        String LANGUAGE = "LANGUAGE";
        String LANGUAGE_LOWERCASE = "language";

        enum ServiceMetadataSections {

            All,
            ServiceIdentification,
            ServiceProvider,
            OperationsMetadata,
            FilterCapabilities,
            DeliveryCapabilities,
            Publications;

            public static ServiceMetadataSections lookup(String value) throws CodedException {
                for (ServiceMetadataSections e : values()) {
                    if (e.toString().equalsIgnoreCase(value)) {
                        return e;
                    }
                }
                throw new InvalidParameterValueException().at(GetCapabilitiesParam.SECTIONS)
                        .withMessage("The requested section '%s' does not exist or is not supported!", value);
            }

        }

    }

}
