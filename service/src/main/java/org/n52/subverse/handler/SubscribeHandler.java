package org.n52.subverse.handler;

import org.n52.iceland.ds.OperationHandler;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.subverse.request.SubscribeRequest;
import org.n52.subverse.response.SubscribeResponse;

/**
 * Handlers are a second level layer under the operations and allow to combine business logic across several operators
 *
*/
public interface SubscribeHandler extends OperationHandler {

    SubscribeResponse demo(SubscribeRequest request) throws OwsExceptionReport;
}
