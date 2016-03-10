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
package org.n52.subverse.coding.capabilities;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.exception.ows.InvalidParameterValueException;
import org.n52.iceland.exception.ows.MissingParameterValueException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.ogc.ows.OWSConstants;
import org.n52.iceland.request.GetCapabilitiesRequest;
import org.n52.iceland.util.KvpHelper;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.coding.KvpDecoder;

public class GetCapabilitiesKvpDecoder extends KvpDecoder<GetCapabilitiesRequest> {

    private static final Set<DecoderKey> KEYS = Sets.newHashSet(createKey(SubverseConstants.SERVICE, null, OWSConstants.Operations.GetCapabilities.toString()),
            createKey(SubverseConstants.SERVICE, SubverseConstants.VERSION, OWSConstants.Operations.GetCapabilities.toString()));

    @Override
    public Set<DecoderKey> getKeys() {
        return Collections.unmodifiableSet(KEYS);
    }

    @Override
    protected GetCapabilitiesRequest createRequest() {
        return new GetCapabilitiesRequest(SubverseConstants.SERVICE);
    }

    @Override
    protected void decodeParameter(GetCapabilitiesRequest request, String name, String values) throws OwsExceptionReport {
        switch (name.toLowerCase()) {
            case SubverseConstants.Param.SERVICE:
                request.setService(KvpHelper.checkParameterSingleValue(values, name));
                break;
            case SubverseConstants.Param.REQUEST:
                KvpHelper.checkParameterSingleValue(values, name);
                break;
            case SubverseConstants.GetCapabilitiesParam.ACCCEPTVERSIONS:
                if (values.isEmpty()) {
                    throw new MissingParameterValueException(name);
                }
                request.setAcceptVersions(Arrays.asList(values.split(",")));
                break;
//            case ACCEPT_FORMATS:
//                request.setAcceptFormats(KvpHelper.checkParameterMultipleValues(values, name));
//                break;
//            case UPDATE_SEQUENCE:
//                request.setUpdateSequence(KvpHelper.checkParameterSingleValue(values, name));
//                break;
//            case SECTIONS:
//                request.setSections(KvpHelper.checkParameterMultipleValues(values, name));
//                break;
//            case LANGUAGE:
//                Extension<String> le = new LanguageExtension(KvpHelper.checkParameterSingleValue(values, name));
//                request.addExtension(le);
//                break;
            default:
                throw new InvalidParameterValueException(name, values).withMessage("The parameter '%s' is not supported.", name); // OptionNotSupportedException (and thereby ParameterNotSupportedException) are OWS 1.1.0
        }
    }

}
