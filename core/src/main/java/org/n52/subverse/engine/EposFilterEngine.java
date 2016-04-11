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
package org.n52.subverse.engine;

import org.n52.subverse.subscription.UnknownSubscriptionException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import net.opengis.fes.x20.FilterDocument;
import net.opengis.fes.x20.FilterType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.epos.engine.EposEngine;
import org.n52.epos.engine.rules.RuleInstance;
import org.n52.epos.event.EposEvent;
import org.n52.epos.filter.EposFilter;
import org.n52.epos.filter.FilterInstantiationException;
import org.n52.epos.filter.FilterInstantiationRepository;
import org.n52.epos.filter.PassiveFilter;
import org.n52.epos.rules.PassiveFilterAlreadyPresentException;
import org.n52.epos.rules.Rule;
import org.n52.epos.rules.RuleListener;
import org.n52.epos.transform.TransformationException;
import org.n52.epos.transform.TransformationRepository;
import org.n52.subverse.delivery.DeliveryEndpoint;
import org.n52.subverse.delivery.Streamable;
import org.n52.subverse.delivery.streamable.GenericStreamable;
import org.n52.subverse.delivery.streamable.StringStreamable;
import org.n52.subverse.subscription.Subscription;
import org.n52.svalbard.xml.XmlOptionsHelper;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class EposFilterEngine implements FilterEngine {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EposFilterEngine.class);

    private final EposEngine engine = EposEngine.getInstance();

    private Map<String, Rule> rules = new HashMap<>();

    private XmlOptionsHelper xmlOptions;

    @Inject
    public void setXmlOptions(XmlOptionsHelper xmlOptions) {
        this.xmlOptions = xmlOptions;
    }

    @Override
    public void filterMessage(Object message) {
        EposEvent event = null;
        try {
            event = TransformationRepository.Instance.transform(message, EposEvent.class);
        } catch (TransformationException ex) {
            LOG.warn("could not transform to EposEvent: {}", ex.getMessage());
        }

        if (event == null) {
            event = new GenericEposEvent(message);
        }

        this.engine.filterEvent(event);
    }

    @Override
    public synchronized void register(Subscription result, DeliveryEndpoint deliveryEndpoint)
            throws SubscriptionRegistrationException {
        try {
            Optional<XmlObject> filter = result.getOptions().getFilter();
            Rule rule = createRule(filter, deliveryEndpoint);
            this.engine.registerRule(rule);
            this.rules.put(result.getId(), rule);
        } catch (FilterInstantiationException ex) {
            LOG.warn("Could not instantiate rule", ex);
            throw new SubscriptionRegistrationException("Could not instantiate rule", ex);
        }
    }

    @Override
    public synchronized void removeSubscription(String subscriptionId) throws UnknownSubscriptionException {
        if (!this.rules.containsKey(subscriptionId)) {
            throw new UnknownSubscriptionException("Subscription unknown: "+subscriptionId);
        }
        this.engine.unregisterRule(this.rules.get(subscriptionId));
    }

    private Rule createRule(Optional<XmlObject> filter, DeliveryEndpoint endpoint)
            throws FilterInstantiationException {
        Rule rule = new RuleInstance(new LocalRuleListener(endpoint));

        if (filter.isPresent()) {
            try {
                EposFilter instantiate = FilterInstantiationRepository.Instance
                    .instantiate(prepare(filter.get()));

                rule.setPassiveFilter((PassiveFilter) instantiate);
            } catch (PassiveFilterAlreadyPresentException ex) {
                // this should not happen as we just created the rule
                throw new FilterInstantiationException(ex);
            }
        }

        return rule;
    }

    private XmlObject prepare(XmlObject obj) {
        if (obj instanceof FilterType) {
            FilterDocument doc = FilterDocument.Factory.newInstance();
            doc.setFilter((FilterType) obj);
            return doc;
        }

        return obj;
    }

    private Streamable createStreamable(Object o) {
        /*
         * TODO outsource to module
         */
        if (o instanceof String) {
            return new StringStreamable((String) o);
        }
        else if (o instanceof XmlObject) {
            return new GenericStreamable("application/xml", o) {
                private String xml;

                @Override
                public InputStream asStream() {
                    return new StringStreamable(getXml()).asStream();
                }

                public synchronized String getXml() {
                    if (this.xml == null) {
                        this.xml = ((XmlObject) o).xmlText(xmlOptions.create().setSaveOuter());
                    }
                    return xml;
                }

                @Override
                public int getContentLength() {
                    return getXml().length();
                }
            };
        }

        return null;
    }


    private class LocalRuleListener implements RuleListener {

        private final DeliveryEndpoint endpoint;

        public LocalRuleListener(DeliveryEndpoint endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void onMatchingEvent(EposEvent event) {
            this.endpoint.deliver(Optional.ofNullable(createStreamable(event.getOriginalObject())));
        }

        @Override
        public void onMatchingEvent(EposEvent event, Object desiredOutputToConsumer) {
            this.endpoint.deliver(Optional.ofNullable(createStreamable(desiredOutputToConsumer)));
        }

        @Override
        public Object getEndpointReference() {
            return this.endpoint;
        }

    }

}
