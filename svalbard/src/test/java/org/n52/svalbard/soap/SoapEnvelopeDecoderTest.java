/*
 * Copyright 2016 52°North.
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

public class SoapEnvelopeDecoderTest {

    private static final String SOAP12_ENV_FILE = "/soap12_envelope.xml";
    private static final DecoderKey KEY = new XmlNamespaceOperationDecoderKey(
            "http://docs.oasis-open.org/wsn/b-2",
            "Subscribe");

    @Test
    public void testDecoding() throws OwsExceptionReport {
        SoapEnvelopeDecoder dec = new SoapEnvelopeDecoder();
        dec.setDecoderRepository(createMockRepo());
        AbstractServiceRequest result = dec.decode(readResource(SOAP12_ENV_FILE));

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
