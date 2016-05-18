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

package org.n52.subverse.termination;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.joda.time.DateTime;
import org.n52.iceland.lifecycle.Constructable;
import org.n52.iceland.lifecycle.Destroyable;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class QuartzTerminationScheduler implements TerminationScheduler, Constructable, Destroyable {


    private static final Logger LOG = LoggerFactory.getLogger(QuartzTerminationScheduler.class);
    private Scheduler quartz;
    private final Map<Terminatable, JobDetail> jobs = new HashMap<>();

    public QuartzTerminationScheduler() {
    }

    @Override
    public void init() {
        try {
            this.quartz = new StdSchedulerFactory().getScheduler();
            this.quartz.start();
        } catch (SchedulerException ex) {
            LOG.warn("Could not initialize scheduling component. Subscriptions will not be removed at end of life!", ex);
        }
    }

    @Override
    public void destroy() {
        try {
            this.quartz.shutdown(true);
        } catch (SchedulerException ex) {
            LOG.warn("Could not stop scheduler", ex);
        }
    }

    @Override
    public void scheduleTermination(Terminatable term) {
        Objects.requireNonNull(term);

        DateTime endOfLife = term.getEndOfLife();
        String uuid = UUID.randomUUID().toString();

        JobDetail job = JobBuilder.newJob(TerminatableJob.class).withIdentity(uuid).build();
        job.getJobDataMap().put(TerminatableJob.KEY, term);
        Trigger trigger = TriggerBuilder.newTrigger().startAt(endOfLife.toDate()).build();

        try {
            Date confirmedDate = this.quartz.scheduleJob(job, trigger);
            LOG.info("Terminatable {} will terminate at {}", term, confirmedDate);
            this.jobs.put(term, job);
        } catch (SchedulerException ex) {
            LOG.warn("Could not schedule job for terminatable {}", term, ex);
        }
    }

    @Override
    public void cancelTermination(Terminatable term) throws UnknownTerminatableException {
        if (this.jobs.containsKey(term)) {
            JobDetail job = this.jobs.get(term);
            try {
                boolean deleted = this.quartz.deleteJob(job.getKey());
                LOG.info("Terminatable {} removed? {}", term, deleted);
            } catch (SchedulerException ex) {
                LOG.warn("Could not remove job", ex);
            }
        }
        else {
            throw new UnknownTerminatableException("Terminatable "+term+" does not exist");
        }
    }


    public static class TerminatableJob implements Job {

        static final String KEY = "terminatable";

        public TerminatableJob() {
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            Object o = context.getJobDetail().getJobDataMap().get(KEY);
            if (o instanceof Terminatable) {
                ((Terminatable) o).terminate();
            }
        }

    }
}
