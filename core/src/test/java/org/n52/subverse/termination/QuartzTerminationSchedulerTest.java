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

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class QuartzTerminationSchedulerTest {

    private static final Logger LOG = LoggerFactory.getLogger(QuartzTerminationSchedulerTest.class);

    @Test
    public void testTermination() throws InterruptedException, BrokenBarrierException, TimeoutException {
        QuartzTerminationScheduler qts = new QuartzTerminationScheduler();
        qts.init();

        CyclicBarrier barrier = new CyclicBarrier(2);
        DummyTerminatable term = new DummyTerminatable(barrier, 0);

        qts.scheduleTermination(term);

        barrier.await(10, TimeUnit.SECONDS);

        Assert.assertThat(term.terminated, CoreMatchers.is(true));
    }

    @Test
    public void testCancel() throws InterruptedException, BrokenBarrierException, TimeoutException, UnknownTerminatableException {
        QuartzTerminationScheduler qts = new QuartzTerminationScheduler();
        qts.init();

        DummyTerminatable term = new DummyTerminatable(new CyclicBarrier(2), 2);

        qts.scheduleTermination(term);
        qts.cancelTermination(term);

        Thread.sleep(4000);

        Assert.assertThat(term.terminated, CoreMatchers.is(false));
    }
    
    @Test
    public void testHistoric() throws InterruptedException, BrokenBarrierException, TimeoutException, UnknownTerminatableException {
        QuartzTerminationScheduler qts = new QuartzTerminationScheduler();
        qts.init();

        CyclicBarrier barrier = new CyclicBarrier(2);
        DummyTerminatable term = new DummyTerminatable(barrier, -20);

        qts.scheduleTermination(term);

        barrier.await(10, TimeUnit.SECONDS);

        Assert.assertThat(term.terminated, CoreMatchers.is(true));
    }

    private static class DummyTerminatable implements Terminatable {

        private final String id;
        private boolean terminated;
        private final DateTime endOfLife;
        private final CyclicBarrier barrier;

        public DummyTerminatable(CyclicBarrier barrier, int plusSeconds) {
            this.id = UUID.randomUUID().toString();
            this.endOfLife = new DateTime().plusSeconds(plusSeconds);
            this.barrier = barrier;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("id", id).toString();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + Objects.hashCode(this.id);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DummyTerminatable other = (DummyTerminatable) obj;
            return Objects.equals(this.id, other.id);
        }


        @Override
        public void terminate() {
            LOG.info("Terminating me! "+this);
            this.terminated = true;
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException ex) {
                this.terminated = false;
                LOG.warn(ex.getMessage(), ex);
            }
        }

        @Override
        public DateTime getEndOfLife() {
            return endOfLife;
        }

    }

}
