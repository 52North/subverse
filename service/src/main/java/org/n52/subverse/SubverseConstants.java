/*
 * Copyright 2015 52°North Initiative for Geospatial Open Source Software GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.subverse;

import org.n52.iceland.exception.CodedException;
import org.n52.iceland.exception.ows.InvalidParameterValueException;

public interface SubverseConstants {

    String WS_N_NAMESPACE = "http://docs.oasis-open.org/wsn/b-2";

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
