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
package org.n52.svalbard.soap;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionType;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.exception.ows.OwsExceptionReport;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class OwsExceptionReportEncoder {

    XmlObject encode(OwsExceptionReport targetException) {
        ExceptionReportDocument excRepDoc = ExceptionReportDocument.Factory.newInstance();
        ExceptionReportDocument.ExceptionReport excRep = excRepDoc.addNewExceptionReport();

        ExceptionType exception = excRep.addNewException();
        exception.addExceptionText(targetException.getMessage());

        exception.setExceptionCode(exception.getExceptionCode());

        return excRepDoc;
    }

}
