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
package org.n52.subverse.coding.capabilities.publications;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class Publications {

    private final List<Publication> publicationList;

    Publications(String publicationsString) {
        String[] entries = publicationsString.split(",");

        publicationList = new ArrayList<>(entries.length);
        for (String entry : entries) {
            String[] idAbs = entry.split("\\|");
            if (idAbs != null && idAbs.length == 2) {
                publicationList.add(new Publication(idAbs[1], idAbs[0]));
            }
        }

        if (publicationList.isEmpty()) {
            publicationList.add(new Publication("All data", "all"));
        }
    }

    public List<Publication> getPublicationList() {
        return publicationList;
    }

}
