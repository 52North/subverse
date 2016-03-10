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
package org.n52.svalbard.soap;

import java.io.InputStream;
import java.util.Collections;
import java.util.Scanner;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.DecoderRepository;
import org.n52.iceland.coding.decode.XmlNamespaceOperationDecoderKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.w3c.soap.SoapRequest;

public class SoapEnvelopeDecoderTest {

    private static final String SOAP12_ENV_FILE = "/soap12_envelope.xml";
    private static final DecoderKey KEY = new XmlNamespaceOperationDecoderKey(
            "http://docs.oasis-open.org/wsn/b-2",
            "Subscribe");

    @Test
    public void testDecoding() throws OwsExceptionReport {
        SoapEnvelopeDecoder dec = new SoapEnvelopeDecoder();
        dec.setDecoderRepository(createMockRepo());
        SoapRequest result = dec.decode(readResource(SOAP12_ENV_FILE));

        Assert.assertThat(result, CoreMatchers.notNullValue());
    }

    private String readResource(String res) {
        InputStream stream = getClass().getResourceAsStream(res);

        StringBuilder sb;
        try (Scanner sc = new Scanner(stream)) {
            sb = new StringBuilder();
            while (sc.hasNext()) {
                sb.append(sc.nextLine());
                sb.append(System.getProperty("line.separator"));
            }
        }

        return sb.toString();
    }

    private DecoderRepository createMockRepo() throws OwsExceptionReport {
        DecoderRepository result = new DecoderRepository();
        DecoderRepository mockRepo = Mockito.spy(result);

        Decoder decoder = Mockito.mock(Decoder.class);
        Mockito.when(decoder.decode(Matchers.any())).thenReturn(Mockito.mock(AbstractServiceRequest.class));
        Mockito.when(decoder.getKeys()).thenReturn(Collections.singleton(KEY));

        Mockito.when(mockRepo.getDecoder(KEY)).thenReturn(decoder);
        return mockRepo;
    }

}
