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
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.n52.iceland.exception.ows.OwsExceptionReport;
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

        URL res = getClass().getResource("subscribe.xml");

        SubscribeRequest subscribe = (SubscribeRequest) dec.decode(Resources.toString(res,
                Charset.forName("UTF-8")));

        Assert.assertThat(subscribe, CoreMatchers.notNullValue());

        SubscribeOptions options = subscribe.getOptions();

        Assert.assertThat(options.getFilterLanguageId().get(), CoreMatchers.is("http://www.opengis.net/fes/2.0"));
        Assert.assertThat(options.getPublicationIdentifier(), CoreMatchers.is("http://host.org/pubsub/publication/aircraft"));

        DeliveryDefinition deliveryDef = options.getDeliveryDefinition().get();
        Assert.assertThat(deliveryDef.getIdentifier(), CoreMatchers.is("http://docs.oasis-open.org/wsn/b-2/NotificationConsumer"));
        Assert.assertThat(deliveryDef.getLocation(), CoreMatchers.is("http://receiver.org/consumer"));
    }

    @Test
    public void testDurationDecoding() throws OwsExceptionReport, IOException {
        SubscribeDecoder dec = new SubscribeDecoder();

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
