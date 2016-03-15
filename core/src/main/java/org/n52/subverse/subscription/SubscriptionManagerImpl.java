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
package org.n52.subverse.subscription;

import org.n52.subverse.IdProvider;
import javax.inject.Inject;
import org.n52.iceland.lifecycle.Destroyable;
import org.n52.subverse.dao.SubscriptionDao;
import org.n52.subverse.delivery.DeliveryProvider;
import org.n52.subverse.delivery.DeliveryProviderRepository;
import org.n52.subverse.delivery.UnsupportedDeliveryDefinitionException;
import org.n52.subverse.engine.FilterEngine;
import org.n52.subverse.engine.SubscriptionRegistrationException;
import org.slf4j.LoggerFactory;

public class SubscriptionManagerImpl implements SubscriptionManager, Destroyable {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SubscriptionManagerImpl.class);
    private SubscriptionDao dao;
    private IdProvider idProvider;
    private DeliveryProviderRepository deliveryProviderRepository;
    private FilterEngine filterEngine;

    public FilterEngine getFilterEngine() {
        return filterEngine;
    }

    @Inject
    public void setFilterEngine(FilterEngine filterEngine) {
        this.filterEngine = filterEngine;
    }

    public SubscriptionDao getDao() {
        return dao;
    }

    public DeliveryProviderRepository getDeliveryProviderRepository() {
        return deliveryProviderRepository;
    }

    @Inject
    public void setDeliveryProviderRepository(DeliveryProviderRepository deliveryProviderRepository) {
        this.deliveryProviderRepository = deliveryProviderRepository;
    }

    @Inject
    public void setDao(SubscriptionDao dao) {
        this.dao = dao;
    }

    public IdProvider getIdProvider() {
        return idProvider;
    }

    @Inject
    public void setIdProvider(IdProvider idProvider) {
        this.idProvider = idProvider;
    }

    @Override
    public Subscription subscribe(SubscribeOptions options) throws UnsupportedDeliveryDefinitionException,
            SubscriptionRegistrationException {
        SubscriptionEndpoint endpoint = createEndpoint(options);
        Subscription result = new Subscription(this.idProvider.generateId(), options, endpoint);

        this.dao.storeSubscription(result);

        this.filterEngine.register(result, endpoint.getDeliveryEndpoint());

        LOG.info("Registered subscription '{}'", result.getId());

        return result;
    }

    @Override
    public void unsubscribe(String subscriptionId) throws UnsubscribeFailedException {
        try {
            this.dao.deleteSubscription(subscriptionId);
            this.filterEngine.removeSubscription(subscriptionId);
            LOG.info("Removed subscription '{}'", subscriptionId);
        } catch (UnknownSubscriptionException ex) {
            throw new UnsubscribeFailedException("Unknown subscription id: "+subscriptionId, ex);
        }

    }

    private SubscriptionEndpoint createEndpoint(SubscribeOptions options) throws UnsupportedDeliveryDefinitionException {
        DeliveryProvider provider = this.deliveryProviderRepository.getProvider(options.getDeliveryDefinition());

        if (provider == null) {
            throw new UnsupportedDeliveryDefinitionException("No provider for the delivery definition is available: "
                    +options.getDeliveryDefinition());
        }

        return new SubscriptionEndpoint(provider.createDeliveryEndpoint(options.getDeliveryDefinition().get()));
    }

    @Override
    public void destroy() {
        this.dao.getAllSubscriptions().forEach(sub -> {
            sub.getEndpoint().getDeliveryEndpoint().destroy();
        });
    }


}
