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
import org.n52.iceland.exception.CodedException;
import org.n52.subverse.coding.subscribe.UnacceptableInitialTerminationTimeFault;
import org.oasisOpen.docs.wsn.b2.AbsoluteOrRelativeTimeType;

/**
 *
 * @author Matthes Rieke <m.rieke@52north.org>
 */
public class TerminationTimeHelper {

   public static DateTime parseDateTime(AbsoluteOrRelativeTimeType time) throws CodedException {
        SchemaType type = time.instanceType();

        if (type == null || type.getName() == null) {
            try {
                return org.n52.iceland.util.DateTimeHelper.makeDateTime(time.getStringValue());
            }
            catch (Exception e) {
                throw new UnacceptableInitialTerminationTimeFault(
                        "Cannot parse date time object").causedBy(e);
            }
        }

        if (type.getName().getLocalPart().equals("dateTime")) {
            return org.n52.iceland.util.DateTimeHelper.makeDateTime(time.getStringValue());
        }
        else if (type.getName().getLocalPart().equals("duration")) {
            String string = time.getStringValue().trim();

            if (string.startsWith("-")) {
                throw new UnacceptableInitialTerminationTimeFault(
                        "Termination time cannot be in the past");
            }

            if (string.startsWith("P")) {
                PeriodFormatter formatter = ISOPeriodFormat.standard();
                Period period = formatter.parsePeriod(string);
                return DateTime.now().plus(period);
            }
        }

        throw new UnacceptableInitialTerminationTimeFault(
                        "Cannot determine type of date time object");
    }

}
