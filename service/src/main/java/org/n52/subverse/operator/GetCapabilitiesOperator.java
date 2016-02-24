package org.n52.subverse.operator;

import java.util.Collections;
import java.util.Set;

import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.ogc.ows.OWSConstants;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.request.GetCapabilitiesRequest;
import org.n52.iceland.request.operator.RequestOperatorKey;
import org.n52.iceland.response.AbstractServiceResponse;
import org.n52.subverse.handler.GetCapabilitiesHandler;
import org.n52.subverse.SubverseConstants;

/**
 * @author Christian Autermann
 */
public class GetCapabilitiesOperator extends AbstractOperator {

    private static final RequestOperatorKey KEY
            = new RequestOperatorKey(SubverseConstants.SERVICE,
                                     SubverseConstants.VERSION,
                                     OWSConstants.Operations.GetCapabilities);

    @Override
    public AbstractServiceResponse receiveRequest(AbstractServiceRequest<?> request) throws OwsExceptionReport {
        return getGetCapabilitiesHandler(request).getCapabilities((GetCapabilitiesRequest) request);
    }

    @Override
    public Set<RequestOperatorKey> getKeys() {
        return Collections.singleton(KEY);
    }

    private GetCapabilitiesHandler getGetCapabilitiesHandler(AbstractServiceRequest<?> request) {
        return (GetCapabilitiesHandler) getHandler(request);
    }

    @Override
    protected String getPrimaryOperationName() {
        return KEY.getOperationName();
    }

}
