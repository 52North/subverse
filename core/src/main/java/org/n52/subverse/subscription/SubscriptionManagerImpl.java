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
package org.n52.subverse.subscription;

import org.n52.subverse.IdProvider;
import javax.inject.Inject;
import org.n52.subverse.dao.SubscriptionDao;
import org.n52.subverse.delivery.DeliveryProvider;
import org.n52.subverse.delivery.DeliveryProviderRepository;
import org.n52.subverse.delivery.UnsupportedDeliveryDefinitionException;
import org.n52.subverse.engine.FilterEngine;
import org.n52.subverse.engine.SubscriptionRegistrationException;
import org.n52.subverse.engine.UnknownSubscriptionException;
import org.slf4j.LoggerFactory;

public class SubscriptionManagerImpl implements SubscriptionManager {

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

        return result;
    }

    @Override
    public void unsubscribe(String subscriptionId) throws UnsubscribeFailedException {
        try {
            this.filterEngine.removeSubscription(subscriptionId);
        } catch (UnknownSubscriptionException ex) {
            throw new UnsubscribeFailedException("Could not remove subscription", ex);
        }
        this.dao.deleteSubscription(subscriptionId);
    }

    private SubscriptionEndpoint createEndpoint(SubscribeOptions options) throws UnsupportedDeliveryDefinitionException {
        DeliveryProvider provider = this.deliveryProviderRepository.getProvider(options.getDeliveryDefinition());

        if (provider == null) {
            throw new UnsupportedDeliveryDefinitionException("No provider for the delivery definition is available: "
                    +options.getDeliveryDefinition());
        }

        return new SubscriptionEndpoint(provider.createDeliveryEndpoint(options.getDeliveryDefinition().get()));
    }



}
