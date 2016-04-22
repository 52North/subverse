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
package org.n52.subverse.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.n52.iceland.ds.OperationHandler;
import org.n52.iceland.ds.OperationHandlerKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.ogc.ows.OwsOperation;
import org.n52.subverse.SubverseConstants;
import org.n52.subverse.dao.SubscriptionDao;
import org.n52.subverse.subscription.Subscription;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class GetSubscriptionHandler implements OperationHandler {

    private static final OperationHandlerKey KEY
            = new OperationHandlerKey(SubverseConstants.SERVICE,
                    SubverseConstants.OPERATION_GET_SUBSCRIPTION);


    private SubscriptionDao dao;

    @Inject
    public void setDao(SubscriptionDao dao) {
        this.dao = dao;
    }

    @Override
    public String getOperationName() {
        return KEY.getOperationName();
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

    public List<Subscription> getSubscriptions(String[] identifiers) throws InvalidSubscriptionIdentifierFault {
        Objects.requireNonNull(identifiers);
        if (identifiers.length == 0) {
            return this.dao.getAllSubscriptions().collect(Collectors.toList());
        }

        List<Subscription> result = new ArrayList<>(identifiers.length);
        List<String> invalids = new ArrayList<>(identifiers.length);
        Arrays.asList(identifiers).stream().distinct().sorted().forEach(id -> {
            Optional<Subscription> subscription = this.dao.getSubscription(id);
            if (subscription.isPresent()) {
                result.add(subscription.get());
            }
            else {
                invalids.add(id);
            }
        });

        if (!invalids.isEmpty()) {
            throw new InvalidSubscriptionIdentifierFault(invalids.toArray(new String[invalids.size()]));
        }

        return result;
    }

}
