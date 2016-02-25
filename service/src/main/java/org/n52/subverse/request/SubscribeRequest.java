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
package org.n52.subverse.request;

import javax.inject.Inject;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.response.SubscribeResponse;
import org.n52.subverse.subscription.SubscribeOptions;
import org.n52.subverse.subscription.SubscriptionManager;
import org.n52.subverse.subscription.SubscriptionReference;

public class SubscribeRequest extends AbstractServiceRequest<SubscribeResponse> {

    private final SubscribeOptions options;

    private SubscriptionManager manager;

    public SubscribeRequest(SubscribeOptions options) {
        this.options = options;
    }

    public SubscriptionManager getManager() {
        return manager;
    }

    @Inject
    public void setManager(SubscriptionManager manager) {
        this.manager = manager;
    }

    @Override
    public SubscribeResponse getResponse() throws OwsExceptionReport {
        SubscriptionReference ref = this.manager.subscribe(options);
        return new SubscribeResponse(ref);
    }

    @Override
    public String getOperationName() {
        return SubverseConstants.OPERATION_SUBSCRIBE;
    }

}
