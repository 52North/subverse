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
package org.n52.subverse.coding.subscribe;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.request.AbstractServiceRequest;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class SubscribeDecoderTest {

    @Test
    public void testDecoding() throws OwsExceptionReport, IOException {
        SubscribeDecoder dec = new SubscribeDecoder();

        URL res = getClass().getResource("subscribe.xml");

        AbstractServiceRequest subscribe = dec.decode(Resources.toString(res,
                Charset.forName("UTF-8")));

        Assert.assertThat(subscribe, CoreMatchers.notNullValue());
    }
}
