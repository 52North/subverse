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
package org.n52.subverse.delivery.wsn;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.n52.subverse.delivery.streamable.StringStreamable;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class WsnConsumerEndpointTest {

    @Test
    public void testDelivery() throws MalformedURLException {
        WsnConsumerEndpoint c = new WsnConsumerEndpoint("http://test.test");
        WsnConsumerEndpoint spied = Mockito.spy(c);

        List<String> result = new ArrayList<>(1);

        Mockito.doAnswer((Answer<Void>) (InvocationOnMock invocation) -> {
            Object[] args = invocation.getArguments();
            result.add(new String((byte[]) args[1]));
            return null;
        }).when(spied).sendPayload(Matchers.any(), Matchers.any());

        spied.deliver(Optional.of(new StringStreamable("hahaha")));

        Assert.assertThat(result.size(), CoreMatchers.is(1));
        Assert.assertThat(result.get(0), CoreMatchers.containsString("<![CDATA[hahaha]]>"));
    }

}
