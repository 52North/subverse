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
package org.n52.subverse.delivery.wsn;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.xmlbeans.XmlOptions;
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
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class WsnConsumerEndpointTest {

    @Test
    public void testDelivery() throws MalformedURLException {
        WsnConsumerEndpoint c = new WsnConsumerEndpoint("http://test.test", new XmlOptions());
        WsnConsumerEndpoint spied = Mockito.spy(c);

        List<String> result = new ArrayList<>(1);

        Mockito.doAnswer((Answer<Void>) (InvocationOnMock invocation) -> {
            Object[] args = invocation.getArguments();
            result.add(new String((byte[]) args[1]));
            return null;
        }).when(spied).sendPayload(Matchers.any(), Matchers.any());

        spied.deliver(Optional.of(new StringStreamable("hahaha")), false);

        Assert.assertThat(result.size(), CoreMatchers.is(1));
        Assert.assertThat(result.get(0), CoreMatchers.containsString("<![CDATA[hahaha]]>"));
    }

}
