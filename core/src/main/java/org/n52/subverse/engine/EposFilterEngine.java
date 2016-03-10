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
package org.n52.subverse.engine;

import org.n52.subverse.subscription.UnknownSubscriptionException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class EposFilterEngine implements FilterEngine {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EposFilterEngine.class);

    private final EposEngine engine = EposEngine.getInstance();

    private Map<String, Rule> rules = new HashMap<>();

    @Override
    public void filterMessage(Object message) {
        try {
            EposEvent event = TransformationRepository.Instance.transform(message, EposEvent.class);
            this.engine.filterEvent(event);
        } catch (TransformationException ex) {
            LOG.warn("could not transform to EposEvent", ex);
        }
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
            String xml = ((XmlObject) o).xmlText(new XmlOptions().setSavePrettyPrint());
            InputStream s = new StringStreamable(xml).asStream();
            return new GenericStreamable(s, "application/xml", xml.length());
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
