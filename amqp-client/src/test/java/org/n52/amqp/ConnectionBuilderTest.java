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
package org.n52.amqp;

import java.net.URI;
import java.net.URISyntaxException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.n52.amqp.jms.JmsOverAmqpConnection;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class ConnectionBuilderTest {

    @Test
    public void testCreation() throws AmqpConnectionCreationFailedException, URISyntaxException {
        try {
            ConnectionBuilder.create(new URI("amqp://localhost")).build();
        }
        catch (AmqpConnectionCreationFailedException e) {
            if (e.getCause() instanceof java.net.ConnectException) {
                //probably no broker running
            }
            else {
                throw e;
            }
        }
    }

    @Test (expected = AmqpConnectionCreationFailedException.class)
    public void testCreationWithUserWithoutPassword() throws AmqpConnectionCreationFailedException, URISyntaxException {
        try {
            ConnectionBuilder.create(new URI("amqp://localhost")).user("test").build();
        }
        catch (AmqpConnectionCreationFailedException e) {
            if (e.getCause() instanceof java.net.ConnectException) {
                //probably no broker running
            }
            else {
                throw e;
            }
        }
    }

    @Test (expected = AmqpConnectionCreationFailedException.class)
    public void testCreationWrongScheme() throws AmqpConnectionCreationFailedException, URISyntaxException {
        try {
            ConnectionBuilder.create(new URI("http://localhost")).build();
        }
        catch (AmqpConnectionCreationFailedException e) {
            if (e.getCause() instanceof java.net.ConnectException) {
                //probably no broker running
            }
            else {
                throw e;
            }
        }
    }

    @Test
    public void testUserPasswordParsing() throws AmqpConnectionCreationFailedException, URISyntaxException {
        try {
            Connection conn = ConnectionBuilder.create(new URI("amqp://tester:test123@localhost")).build();
            Assert.assertThat(conn.getUsername(), CoreMatchers.is("tester"));
            Assert.assertThat(conn.getPassword(), CoreMatchers.is("test123"));

            conn = ConnectionBuilder.create(new URI("amqp://localhost")).user("worker").password("s3cret").build();
            Assert.assertThat(conn.getUsername(), CoreMatchers.is("worker"));
            Assert.assertThat(conn.getPassword(), CoreMatchers.is("s3cret"));
            Assert.assertThat(conn.getRemoteURI().toString(), CoreMatchers.is("amqp://worker:s3cret@localhost"));
        }
        catch (AmqpConnectionCreationFailedException e) {
            if (e.getCause() instanceof java.net.ConnectException) {
                //probably no broker running
            }
            else {
                throw e;
            }
        }
    }

    @Test
    public void testJmsFlavor() throws URISyntaxException, AmqpConnectionCreationFailedException {
        Connection conn = ConnectionBuilder.create(new URI("amqp://tester:test123@localhost/queue://hahahaha")).jmsFlavor().build();

        Assert.assertThat(conn, CoreMatchers.instanceOf(JmsOverAmqpConnection.class));
        Assert.assertThat(((JmsOverAmqpConnection) conn).getDestination(), CoreMatchers.is("queue://hahahaha"));
    }
}
