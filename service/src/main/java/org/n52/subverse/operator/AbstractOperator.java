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

import javax.inject.Inject;
import org.n52.iceland.ds.OperationHandler;
import org.n52.iceland.ds.OperationHandlerRepository;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.ogc.ows.OwsOperation;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.request.operator.RequestOperator;

/**
 *
 * @author matthes
 */
public abstract class AbstractOperator implements RequestOperator {

    private OperationHandlerRepository operationHandlerRepository;

    @Inject
    public void setOperationHandlerRepository(OperationHandlerRepository repo) {
        this.operationHandlerRepository = repo;
    }

    protected OperationHandler getHandler(AbstractServiceRequest<?> request) {
        String service = request.getService();
        String operationName = request.getOperationName();
        return getHandler(service, operationName);
    }

    protected OperationHandler getHandler(String service, String operationName) {
        return operationHandlerRepository.getOperationHandler(service, operationName);
    }

    @Override
    public OwsOperation getOperationMetadata(String service, String version) throws OwsExceptionReport {
        return getHandler(service, getPrimaryOperationName()).getOperationsMetadata(service, version);
    }

    protected abstract String getPrimaryOperationName();

}
