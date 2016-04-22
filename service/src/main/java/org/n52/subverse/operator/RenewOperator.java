/*
 * Copyright (C) 2016-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.subverse.operator;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.n52.iceland.exception.ows.InvalidParameterValueException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.request.operator.RequestOperatorKey;
import org.n52.iceland.response.AbstractServiceResponse;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.coding.unsubscribe.ResourceUnknownFault;
import org.n52.subverse.handler.RenewHandler;
import org.n52.subverse.request.RenewRequest;
import org.n52.subverse.response.RenewResponse;
import org.n52.subverse.subscription.UnknownSubscriptionException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class RenewOperator extends AbstractOperator {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RenewOperator.class);
    private static final RequestOperatorKey KEY
            = new RequestOperatorKey(SubverseConstants.SERVICE,
                    SubverseConstants.VERSION,
                    SubverseConstants.OPERATION_RENEW);

    @Override
    public AbstractServiceResponse receiveRequest(AbstractServiceRequest<?> request) throws OwsExceptionReport {
        if (request instanceof RenewRequest) {
            RenewHandler handler = (RenewHandler) getHandler(request);
            DateTime termTime;
            try {
                termTime = handler.renewSubscription((RenewRequest) request);
            } catch (UnknownSubscriptionException ex) {
                LOG.warn("Unknown subscription", ex);
                throw new ResourceUnknownFault(ex.getMessage());
            }

            RenewResponse result = new RenewResponse(termTime);
            result.setService(request.getService());
            result.setVersion(request.getVersion());
            return result;
        }

        throw new InvalidParameterValueException().withMessage("Invalid Renew request received");
    }

    @Override
    public Set<RequestOperatorKey> getKeys() {
        return Collections.singleton(KEY);
    }

    @Override
    protected String getPrimaryOperationName() {
        return KEY.getOperationName();
    }

}
