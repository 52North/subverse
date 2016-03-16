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
package org.n52.subverse.util;

import org.apache.xmlbeans.SchemaType;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.oasisOpen.docs.wsn.b2.AbsoluteOrRelativeTimeType;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class TerminationTimeHelper {

   public static DateTime parseDateTime(AbsoluteOrRelativeTimeType time) throws InvalidTerminationTimeException {
        SchemaType type = time.instanceType();

        String val = null;

        if (type == null || type.getName() == null) {
            val = time.getStringValue();
            try {
                return org.n52.iceland.util.DateTimeHelper.makeDateTime(val);
            }
            catch (Exception e) {
                throw new InvalidTerminationTimeException(
                        "Cannot parse date time object", e);
            }
        }

        if (type.getName().getLocalPart().equals("dateTime")) {
            val = time.getStringValue();
            return org.n52.iceland.util.DateTimeHelper.makeDateTime(val);
        }
        else if (type.getName().getLocalPart().equals("duration")) {
            val = time.getStringValue().trim();

            if (val.startsWith("-")) {
                throw new InvalidTerminationTimeException(
                        "Negative durations are not allowed");
            }

            if (val.startsWith("P")) {
                PeriodFormatter formatter = ISOPeriodFormat.standard();
                Period period = formatter.parsePeriod(val);
                return DateTime.now().plus(period);
            }
        }

        throw new InvalidTerminationTimeException("Could not parse termination time");
    }

}
