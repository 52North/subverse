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
package org.n52.subverse.coding.subscribe;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.subverse.coding.capabilities.publications.PublicationsProducer;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.request.SubscribeRequest;
import org.n52.subverse.subscription.SubscribeOptions;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class SubscribeDecoderTest {

    @Test
    public void testDecoding() throws OwsExceptionReport, IOException {
        SubscribeDecoder dec = new SubscribeDecoder();
        dec.setPublicationsProducer(new PublicationsProducer().setPublicationsString("testdata|testdata"));

        URL res = getClass().getResource("subscribe.xml");

        SubscribeRequest subscribe = (SubscribeRequest) dec.decode(Resources.toString(res,
                Charset.forName("UTF-8")));

        Assert.assertThat(subscribe, CoreMatchers.notNullValue());

        SubscribeOptions options = subscribe.getOptions();

        Assert.assertThat(options.getFilterLanguageId().get(), CoreMatchers.is("http://www.opengis.net/fes/2.0"));
        Assert.assertThat(options.getPublicationIdentifier(), CoreMatchers.is("testdata"));

        DeliveryDefinition deliveryDef = options.getDeliveryDefinition().get();
        Assert.assertThat(deliveryDef.getIdentifier(), CoreMatchers.is("http://docs.oasis-open.org/wsn/b-2/NotificationConsumer"));
        Assert.assertThat(deliveryDef.getLocation(), CoreMatchers.is("http://receiver.org/consumer"));
    }

    @Test
    public void testDurationDecoding() throws OwsExceptionReport, IOException {
        SubscribeDecoder dec = new SubscribeDecoder();
        dec.setPublicationsProducer(new PublicationsProducer().setPublicationsString("testdata|testdata"));

        URL res = getClass().getResource("subscribe_duration.xml");
        SubscribeRequest subscribe = (SubscribeRequest) dec.decode(Resources.toString(res,
                Charset.forName("UTF-8")));

        Assert.assertThat(subscribe, CoreMatchers.notNullValue());

        DateTime termTime = subscribe.getOptions().getTerminationTime().get();
        Assert.assertThat(termTime.isBefore(DateTime.now().plusYears(1).plusMonths(2)), CoreMatchers.is(true));
        Assert.assertThat(termTime.isAfter(DateTime.now().plusYears(1).plusMonths(1)), CoreMatchers.is(true));

        res = getClass().getResource("subscribe_duration2.xml");
        subscribe = (SubscribeRequest) dec.decode(Resources.toString(res,
                Charset.forName("UTF-8")));

        Assert.assertThat(subscribe, CoreMatchers.notNullValue());

        termTime = subscribe.getOptions().getTerminationTime().get();
        Assert.assertThat(termTime.isBefore(DateTime.now().plusHours(2)), CoreMatchers.is(true));
        Assert.assertThat(termTime.isAfter(DateTime.now().plusHours(1).minusMinutes(1)), CoreMatchers.is(true));
    }
}
