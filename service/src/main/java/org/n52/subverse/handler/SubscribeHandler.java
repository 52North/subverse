package org.n52.subverse.handler;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.n52.iceland.ds.OperationHandler;
import org.n52.iceland.ds.OperationHandlerKey;
import org.n52.iceland.exception.ows.InvalidParameterValueException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.ogc.ows.OwsOperation;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.delivery.UnsupportedDeliveryDefinitionException;
import org.n52.subverse.engine.SubscriptionRegistrationException;
import org.n52.subverse.subscription.SubscribeOptions;
import org.n52.subverse.response.SubscribeResponse;
import org.n52.subverse.subscription.SubscriptionManager;
import org.slf4j.LoggerFactory;

/**
 * Handlers are a second level layer under the operations and allow to combine business logic across several operators
 *
*/
public class SubscribeHandler implements OperationHandler {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SubscribeHandler.class);

    private static final OperationHandlerKey KEY
            = new OperationHandlerKey(SubverseConstants.SERVICE,
                    SubverseConstants.OPERATION_SUBSCRIBE);
    private SubscriptionManager manager;


    public SubscriptionManager getManager() {
        return manager;
    }

    @Inject
    public void setManager(SubscriptionManager manager) {
        this.manager = manager;
    }

    public SubscribeResponse subscribe(SubscribeOptions request) throws OwsExceptionReport {
        try {
            return new SubscribeResponse(this.manager.subscribe(request));
        } catch (UnsupportedDeliveryDefinitionException ex) {
            LOG.warn("DeliveryDefinition denied", ex);
            throw new InvalidParameterValueException().causedBy(ex);
        } catch (SubscriptionRegistrationException ex) {
            LOG.warn("Registration of subscription failed", ex);
            throw new InvalidParameterValueException().causedBy(ex);
        }
    }

    @Override
    public String getOperationName() {
        return KEY.getOperationName();
    }

    @Override
    public OwsOperation getOperationsMetadata(String service, String version) throws OwsExceptionReport {
        return new OwsOperation();
    }

    @Override
    public Set<OperationHandlerKey> getKeys() {
        return Collections.singleton(KEY);
    }
}
