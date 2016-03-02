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
package org.n52.subverse.operator;

import java.util.Collections;
import java.util.Set;
import org.n52.iceland.ds.OperationHandler;
import org.n52.iceland.exception.ows.NoApplicableCodeException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.request.operator.RequestOperatorKey;
import org.n52.iceland.response.AbstractServiceResponse;
import org.n52.iceland.response.NoContentResponse;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.handler.NotifyHandler;
import org.n52.subverse.request.NotifyRequest;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class NotifyOperator extends AbstractOperator {

    private static final RequestOperatorKey KEY
            = new RequestOperatorKey(SubverseConstants.SERVICE,
                    SubverseConstants.VERSION,
                    SubverseConstants.OPERATION_NOTIFY);

    @Override
    protected String getPrimaryOperationName() {
        return SubverseConstants.OPERATION_NOTIFY;
    }

    @Override
    public AbstractServiceResponse receiveRequest(AbstractServiceRequest<?> request) throws OwsExceptionReport {
        OperationHandler handler = getHandler(request);
        if (handler != null) {
            ((NotifyHandler) handler).receive((NotifyRequest) request);
            NoContentResponse result = new NoContentResponse(getPrimaryOperationName());
            result.setService(request.getService());
            result.setVersion(request.getVersion());
            return result;
        }

        throw new NoApplicableCodeException().withMessage("No Notify Handler found");
    }

    @Override
    public Set<RequestOperatorKey> getKeys() {
        return Collections.singleton(KEY);
    }

}
