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
package org.n52.subverse.handler;

import java.util.Collections;
import java.util.Set;
import javax.inject.Inject;
import org.n52.iceland.ds.OperationHandler;
import org.n52.iceland.ds.OperationHandlerKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.ogc.ows.OwsOperation;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.notify.NotificationConsumer;
import org.n52.subverse.request.NotifyRequest;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class NotifyHandler implements OperationHandler {

    private static final OperationHandlerKey KEY
            = new OperationHandlerKey(SubverseConstants.SERVICE,
                    SubverseConstants.OPERATION_NOTIFY);

    private NotificationConsumer consumer;

    public NotificationConsumer getConsumer() {
        return consumer;
    }

    @Inject
    public void setConsumer(NotificationConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public String getOperationName() {
        return SubverseConstants.OPERATION_NOTIFY;
    }

    @Override
    public OwsOperation getOperationsMetadata(String service, String version) throws OwsExceptionReport {
        OwsOperation op = new OwsOperation();
        op.setOperationName(getOperationName());
        return op;
    }

    @Override
    public Set<OperationHandlerKey> getKeys() {
        return Collections.singleton(KEY);
    }

    public void receive(NotifyRequest notifyRequest) {
        notifyRequest.messages().forEach(m -> this.consumer.receive(m));
    }

}
