/*
 * Copyright 2016 52°North Initiative for Geospatial Open Source Software GmbH.
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

package org.n52.amqp.jms;

import java.net.URI;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class JmsOverAmqpConnectionTest {

    @Test
    public void testUrlAndDestination() {
        String target = "amqp://192.168.0.12:5672/topic://52NORTH.Topic01.OUT";
        JmsOverAmqpConnection conn = new JmsOverAmqpConnection(URI.create(target), null, null);

        Assert.assertThat(conn.getHost(), CoreMatchers.is("192.168.0.12"));
        Assert.assertThat(conn.getPort(), CoreMatchers.is(5672));
        Assert.assertThat(conn.getDestination(), CoreMatchers.is("topic://52NORTH.Topic01.OUT"));

        target = "amqp://192.168.0.13:5671/topic://52NORTH.Topic01.OUT";
        conn = new JmsOverAmqpConnection(URI.create(target), null, null);

        Assert.assertThat(conn.getHost(), CoreMatchers.is("192.168.0.13"));
        Assert.assertThat(conn.getPort(), CoreMatchers.is(5671));
        Assert.assertThat(conn.getDestination(), CoreMatchers.is("topic://52NORTH.Topic01.OUT"));

        target = "amqp://192.168.0.13/topic://52NORTH.Topic01.OUT";
        conn = new JmsOverAmqpConnection(URI.create(target), null, null);

        Assert.assertThat(conn.getHost(), CoreMatchers.is("192.168.0.13"));
        Assert.assertThat(conn.getPort(), CoreMatchers.is(5672));
        Assert.assertThat(conn.getDestination(), CoreMatchers.is("topic://52NORTH.Topic01.OUT"));
    }

}
