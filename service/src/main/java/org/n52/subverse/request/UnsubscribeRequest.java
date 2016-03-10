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

import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.response.UnsubscribeResponse;

public class UnsubscribeRequest extends AbstractServiceRequest<UnsubscribeResponse> {

    private final String subscriptionId;
    private UnsubscribeResponse response;

    public UnsubscribeRequest(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void setResponse(UnsubscribeResponse response) {
        this.response = response;
    }

    @Override
    public UnsubscribeResponse getResponse() throws OwsExceptionReport {
        return this.response;
    }

    @Override
    public String getOperationName() {
        return SubverseConstants.OPERATION_UNSUBSCRIBE;
    }


    @Override
    public boolean isSetVersion() {
        return true;
    }

    @Override
    public boolean isSetService() {
        return true;
    }

    @Override
    public String getVersion() {
        return SubverseConstants.VERSION;
    }

    @Override
    public String getService() {
        return SubverseConstants.SERVICE;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

}
