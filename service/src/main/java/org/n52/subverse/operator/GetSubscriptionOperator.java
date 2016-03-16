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
import java.util.List;
import java.util.Set;
import org.n52.iceland.exception.ows.InvalidParameterValueException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.request.operator.RequestOperatorKey;
import org.n52.iceland.response.AbstractServiceResponse;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.handler.GetSubscriptionHandler;
import org.n52.subverse.request.GetSubscriptionRequest;
import org.n52.subverse.response.GetSubscriptionResponse;
import org.n52.subverse.subscription.Subscription;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class GetSubscriptionOperator extends AbstractOperator {

    private static final RequestOperatorKey KEY
            = new RequestOperatorKey(SubverseConstants.SERVICE,
                    SubverseConstants.VERSION,
                    SubverseConstants.OPERATION_GET_SUBSCRIPTION);

    @Override
    public AbstractServiceResponse receiveRequest(AbstractServiceRequest<?> request) throws OwsExceptionReport {
        if (request instanceof GetSubscriptionRequest) {
            GetSubscriptionHandler handler = (GetSubscriptionHandler) getHandler(request);

            List<Subscription> subs =  handler.getSubscriptions(((GetSubscriptionRequest) request).getIdentifiers());
            GetSubscriptionResponse result = new GetSubscriptionResponse().setSubscriptions(subs);
            result.setService(request.getService());
            result.setVersion(request.getVersion());
            return result;
        }

        throw new InvalidParameterValueException().withMessage("Invalid GetSubscription request received");
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
