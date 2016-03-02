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
package org.n52.subverse.subscription;

public class Subscription {

    private final String id;
    private final SubscribeOptions options;
    private final SubscriptionEndpoint endpoint;

    public Subscription(String id, SubscribeOptions options, SubscriptionEndpoint endpoint) {
        this.id = id;
        this.options = options;
        this.endpoint = endpoint;
    }

    public String getId() {
        return id;
    }

    public SubscribeOptions getOptions() {
        return options;
    }

    public SubscriptionEndpoint getEndpoint() {
        return endpoint;
    }

}
