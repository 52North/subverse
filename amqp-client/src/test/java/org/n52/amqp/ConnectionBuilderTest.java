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
import org.junit.Test;

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
}
