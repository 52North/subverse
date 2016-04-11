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
import java.util.Objects;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class ConnectionBuilder {

    private final URI address;
    private String user;
    private String password;


    private ConnectionBuilder(URI address) {
        this.address = address;
    }

    public static ConnectionBuilder create(URI address) throws AmqpConnectionCreationFailedException {
        Objects.requireNonNull(address, "Please provide an AMQP broker address");

        String scheme = address.getScheme();
        if (!scheme.equals("amqp") && !scheme.equals("amqps") && !scheme.equals("tcp")) {
            throw new AmqpConnectionCreationFailedException(String.format("%s is not a valid amqp address", address));
        }
        return new ConnectionBuilder(address);
    }

    public ConnectionBuilder user(String user) {
        this.user = user;
        return this;
    }

    public ConnectionBuilder password(String pwd) {
        this.password = pwd;
        return this;
    }

    public Connection build() throws AmqpConnectionCreationFailedException {
        if (this.user != null) {
            if (this.password == null) {
                throw new AmqpConnectionCreationFailedException("If a user is provided, a password is required as well");
            }
        }

        try {
            return new Connection(this.address, this.user, this.password);
        } catch (Exception ex) {
            throw new AmqpConnectionCreationFailedException(ex);
        }
    }

}
