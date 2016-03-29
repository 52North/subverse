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
package org.n52.subverse.coding.renew;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.iceland.coding.HelperValues;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.EncodingException;
import org.n52.iceland.coding.encode.OperationResponseEncoderKey;
import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.util.http.MediaType;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.response.RenewResponse;
import org.oasisOpen.docs.wsn.b2.RenewResponseDocument;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
@Configurable
public class RenewResponseEncoder implements Encoder<XmlObject, RenewResponse> {

    private static final Set<EncoderKey> ENCODER_KEYS = Sets.<EncoderKey>newHashSet(
            new OperationResponseEncoderKey(SubverseConstants.SERVICE,
                    SubverseConstants.VERSION,
                    SubverseConstants.OPERATION_RENEW,
                    MediaTypes.TEXT_XML),
            new OperationResponseEncoderKey(SubverseConstants.SERVICE,
                    SubverseConstants.VERSION,
                    SubverseConstants.OPERATION_RENEW,
                    MediaTypes.APPLICATION_XML));

    @Override
    public XmlObject encode(RenewResponse objectToEncode) throws EncodingException {
        return encode(objectToEncode, Collections.emptyMap());
    }

    @Override
    public XmlObject encode(RenewResponse objectToEncode, Map<HelperValues, String> additionalValues) throws EncodingException {
        RenewResponseDocument renewDoc = RenewResponseDocument.Factory.newInstance();
        RenewResponseDocument.RenewResponse renew = renewDoc.addNewRenewResponse();

        /*
         * tiem stamps
         */
        renew.setCurrentTime(new DateTime(DateTimeZone.UTC).toCalendar(Locale.getDefault()));
        DateTime termTime = objectToEncode.getTerminationTime();
        renew.setTerminationTime(termTime.toCalendar(Locale.getDefault()));

        return renewDoc;
    }

    @Override
    public MediaType getContentType() {
        return MediaTypes.APPLICATION_XML;
    }

    @Override
    public Set<EncoderKey> getKeys() {
        return ENCODER_KEYS;
    }

}
