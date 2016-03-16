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
package org.n52.subverse.response;

import java.util.List;
import org.n52.iceland.response.AbstractServiceResponse;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.subscription.Subscription;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class GetSubscriptionResponse extends AbstractServiceResponse {

    private List<Subscription> subscriptions;

    @Override
    public String getOperationName() {
        return SubverseConstants.OPERATION_GET_SUBSCRIPTION;
    }

    public List<Subscription> getSubscriptions() {
        return this.subscriptions;
    }

    public GetSubscriptionResponse setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
        return this;
    }

}
