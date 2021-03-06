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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionType;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.HelperValues;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.EncodingException;
import org.n52.iceland.coding.encode.ExceptionEncoderKey;
import org.n52.iceland.exception.ows.CodedOwsException;
import org.n52.iceland.exception.ows.OwsExceptionCode;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.ogc.ows.ExceptionCode;
import org.n52.iceland.util.http.MediaType;
import org.n52.iceland.util.http.MediaTypes;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class OwsExceptionReportEncoder implements Encoder<XmlObject, OwsExceptionReport> {

    private static final EncoderKey KEY = new ExceptionEncoderKey(MediaTypes.APPLICATION_XML);

    private final boolean encodeStackTraces;

    public OwsExceptionReportEncoder() {
        this(false);
    }

    public OwsExceptionReportEncoder(boolean encodeStackTraces) {
        this.encodeStackTraces = encodeStackTraces;
    }


    @Override
    public XmlObject encode(OwsExceptionReport objectToEncode) throws EncodingException {
        return encode(objectToEncode, Collections.emptyMap());
    }

    @Override
    public XmlObject encode(OwsExceptionReport objectToEncode, Map<HelperValues, String> additionalValues) throws EncodingException {
        ExceptionReportDocument excRepDoc = ExceptionReportDocument.Factory.newInstance();
        ExceptionReportDocument.ExceptionReport excRep = excRepDoc.addNewExceptionReport();

        ExceptionType exception = excRep.addNewException();
        exception.addExceptionText(createExceptionText(objectToEncode));

        if (objectToEncode instanceof CodedOwsException) {
            ExceptionCode code = ((CodedOwsException) objectToEncode).getCode();
            if (code instanceof OwsExceptionCode) {
                exception.setExceptionCode(((OwsExceptionCode) code).name());
            }
            else {
                exception.setExceptionCode(code.toString());
            }
        }

        return excRepDoc;
    }

    @Override
    public MediaType getContentType() {
        return MediaTypes.APPLICATION_XML;
    }

    @Override
    public Set<EncoderKey> getKeys() {
        return Collections.singleton(KEY);
    }

    private String createExceptionText(OwsExceptionReport ex) {
        String msg = ex.getMessage();

        StringBuilder sb = new StringBuilder();
        if (msg != null) {
            sb.append(msg);
        }

        Throwable cause = ex.getCause();
        if (cause != null) {
            sb.append(System.getProperty("line.separator"));
            sb.append(cause.getMessage());

            StackTraceElement[] stack = cause.getStackTrace();

            if (this.encodeStackTraces && stack != null) {
                for (StackTraceElement ste : stack) {
                    sb.append(System.getProperty("line.separator"));
                    sb.append(ste.toString());
                }
            }
        }

        return sb.toString();
    }

}
