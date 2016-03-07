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
package org.n52.subverse.coding.capabilities.delivery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.n52.subverse.delivery.DeliveryProvider;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class DeliveryCapabilities {

    private List<DeliveryMethod> methods;

    DeliveryCapabilities(Collection<DeliveryProvider> providers) {
        Objects.requireNonNull(providers);
        this.methods = new ArrayList<>(providers.size());

        for (DeliveryProvider provider : providers) {
            this.methods.add(new DeliveryMethod(provider.getIdentifier(),
            provider.getAbstract()));
        }
    }

    public List<DeliveryMethod> getMethods() {
        return methods;
    }

}
