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
package org.n52.svalbard.soap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionType;
import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.coding.encode.Encoder;
import org.n52.iceland.coding.encode.EncoderKey;
import org.n52.iceland.coding.encode.ExceptionEncoderKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.iceland.ogc.ows.OWSConstants;
import org.n52.iceland.util.http.MediaType;
import org.n52.iceland.util.http.MediaTypes;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
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
    public XmlObject encode(OwsExceptionReport objectToEncode) throws OwsExceptionReport, UnsupportedEncoderInputException {
        return encode(objectToEncode, Collections.emptyMap());
    }

    @Override
    public XmlObject encode(OwsExceptionReport objectToEncode, Map<OWSConstants.HelperValues, String> additionalValues) throws OwsExceptionReport, UnsupportedEncoderInputException {
        ExceptionReportDocument excRepDoc = ExceptionReportDocument.Factory.newInstance();
        ExceptionReportDocument.ExceptionReport excRep = excRepDoc.addNewExceptionReport();

        ExceptionType exception = excRep.addNewException();
        exception.addExceptionText(createExceptionText(objectToEncode));

        exception.setExceptionCode(exception.getExceptionCode());

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
