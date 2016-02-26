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
package org.n52.subverse.coding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.oasisOpen.docs.wsn.b2.SubscribeDocument;

/**
 *
 * @author matthes
 */
public class XmlBeansHelper {

    public static Optional<XmlObject> findFirstChild(QName qn, XmlObject obj) {
        return findChildren(qn, obj).findFirst();
    }

    public static Stream<XmlObject> findChildren(QName qn, XmlObject obj) {
        if (obj != null) {
            XmlObject[] list = obj.selectChildren(qn);
            return Arrays.asList(list).stream();
        }

        return new ArrayList<XmlObject>(0).stream();
    }

    public static String extractStringContent(XmlObject subDoc) {
        XmlCursor cur = subDoc.newCursor();
        cur.toFirstContentToken();
        return cur.getTextValue();
    }

}
