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

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import org.n52.subverse.IdProvider;
import javax.inject.Inject;
import org.joda.time.DateTime;
import org.n52.iceland.config.annotation.Configurable;
import org.n52.iceland.config.annotation.Setting;
import org.n52.iceland.lifecycle.Constructable;
import org.n52.iceland.lifecycle.Destroyable;
import org.n52.subverse.SubverseSettings;
import org.n52.subverse.dao.SubscriptionDao;
import org.n52.subverse.delivery.DeliveryDefinition;
import org.n52.subverse.delivery.DeliveryProvider;
import org.n52.subverse.delivery.DeliveryProviderRepository;
import org.n52.subverse.delivery.UnsupportedDeliveryDefinitionException;
import org.n52.subverse.engine.FilterEngine;
import org.n52.subverse.engine.SubscriptionRegistrationException;
import org.n52.subverse.termination.Terminatable;
import org.n52.subverse.termination.TerminationScheduler;
import org.n52.subverse.termination.UnknownTerminatableException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Configurable
public class SubscriptionManagerImpl implements SubscriptionManager, Constructable, Destroyable {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SubscriptionManagerImpl.class);
    private SubscriptionDao dao;
    private IdProvider idProvider;
    private DeliveryProviderRepository deliveryProviderRepository;
    private FilterEngine filterEngine;
    private String rootPublicationIdentifier;

    @Autowired
    private TerminationScheduler terminationScheduler;

    public FilterEngine getFilterEngine() {
        return filterEngine;
    }

    @Inject
    public void setFilterEngine(FilterEngine filterEngine) {
        this.filterEngine = filterEngine;
    }


    @Setting(SubverseSettings.ROOT_PUBLICATION)
    public void setRootPublicationIdentifier(String pubId) {
        this.rootPublicationIdentifier = pubId;
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
        Subscription result = internalSubscribe(options, this.idProvider.generateId());
        this.dao.storeSubscription(result);
        return result;
    }

    private Subscription internalSubscribe(SubscribeOptions options, String id) throws UnsupportedDeliveryDefinitionException,
            SubscriptionRegistrationException {
        SubscriptionEndpoint endpoint = createEndpoint(options);

        SubscribeOptions finalOptions;
        if (rootPublicationIdentifier.equals(options.getPublicationIdentifier())) {
            finalOptions = new SubscribeOptions(null, options);
        }
        else {
            finalOptions = options;
        }

        Subscription result = new Subscription(id, finalOptions, endpoint);

        this.filterEngine.register(result, endpoint.getDeliveryEndpoint());

        LOG.info("Registered subscription '{}'", result.getId());

        if (options.getTerminationTime().isPresent()) {
            this.terminationScheduler.scheduleTermination(new SubscriptionTerminatable(result.getId(),
                options.getTerminationTime().get()));
        }

        return result;
    }

    @Override
    public void unsubscribe(String subscriptionId) throws UnsubscribeFailedException {
        try {
            LOG.debug("Invoking removal of subscription '{}'", subscriptionId);
            Subscription toBeRemoved = this.dao.deleteSubscription(subscriptionId);
            this.filterEngine.removeSubscription(subscriptionId);
            if (toBeRemoved != null) {
                if (toBeRemoved.getEndpoint() != null) {
                    LOG.debug("Destroying endpoint: {}", toBeRemoved.getEndpoint().getDeliveryEndpoint());
                    toBeRemoved.getEndpoint().destroy();
                }
            }
            else {
                LOG.warn("DAO did not return subscription {}", subscriptionId);
            }

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

        DeliveryDefinition delDef = options.getDeliveryDefinition().get();
        return new SubscriptionEndpoint(provider.createDeliveryEndpoint(delDef), delDef);
    }

    @Override
    public void init() {
        this.dao.getAllSubscriptions().forEach(sub -> {
            try {
                this.internalSubscribe(sub.getOptions(), sub.getId());
                LOG.info("Re-subscribed subscription '{}'", sub.getId());
            } catch (UnsupportedDeliveryDefinitionException | SubscriptionRegistrationException ex) {
                LOG.warn("Could not re-subscribe subscription '{}'", sub.getId(), ex);
            }
        });
    }



    @Override
    public void destroy() {
        this.dao.getAllSubscriptions().forEach(sub -> {
            sub.getEndpoint().getDeliveryEndpoint().destroy();
        });
    }

    @Override
    public void renew(String subscriptionId, DateTime terminationTime) throws UnknownSubscriptionException {
        Optional<Subscription> sub = this.dao.getSubscription(subscriptionId);

        if (!sub.isPresent()) {
            throw new UnknownSubscriptionException("Subscription unknown: "+subscriptionId);
        }

        this.dao.updateTerminationTime(sub.get(), terminationTime);

        SubscriptionTerminatable term = new SubscriptionTerminatable(subscriptionId, terminationTime);
        try {
            this.terminationScheduler.cancelTermination(term);
        } catch (UnknownTerminatableException ex) {
            LOG.warn("Subscription termination did not exist. This might be ok. "+ex.getMessage());
            LOG.debug(ex.getMessage(), ex);
        }
        this.terminationScheduler.scheduleTermination(term);
    }


    private class SubscriptionTerminatable implements Terminatable {

        private final String subscription;
        private final DateTime endOfLife;

        private SubscriptionTerminatable(String subId, DateTime eol) {
            this.subscription = subId;
            this.endOfLife = eol;
        }

        @Override
        public void terminate() {
            try {
                unsubscribe(subscription);
            } catch (UnsubscribeFailedException ex) {
                LOG.warn("Could not unsubscribe!", ex);
            }
        }

        @Override
        public DateTime getEndOfLife() {
            return this.endOfLife;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("subscription", subscription).toString();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 41 * hash + Objects.hashCode(this.subscription);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SubscriptionTerminatable other = (SubscriptionTerminatable) obj;
            return Objects.equals(this.subscription, other.subscription);
        }


    }

}
