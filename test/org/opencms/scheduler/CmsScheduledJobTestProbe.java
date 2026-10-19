/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** Per-test observations of scheduled job invocations. */
final class CmsScheduledJobTestProbe implements AutoCloseable {

    /** One fully published invocation result. */
    private static final class Result {

        private final int m_instanceCount;
        private final String m_failure;

        Result(int instanceCount, String failure) {

            m_instanceCount = instanceCount;
            m_failure = failure;
        }
    }

    /** Job parameter identifying the observer, independent of scheduler job IDs. */
    private static final String PROBE_PARAMETER = "testProbe";

    /** Observers belonging to tests which have not yet cleaned up their jobs. */
    private static final Map<String, CmsScheduledJobTestProbe> PROBES = new ConcurrentHashMap<>();

    private final String m_token = UUID.randomUUID().toString();
    private final BlockingQueue<Result> m_results = new LinkedBlockingQueue<>();

    CmsScheduledJobTestProbe() {

        PROBES.put(m_token, this);
    }

    /** Resolves only this invocation's observer; never falls back to another test. */
    static CmsScheduledJobTestProbe lookup(Map<String, String> parameters) {

        if (parameters == null) {
            throw new IllegalArgumentException("Scheduled job parameters are null");
        }
        CmsScheduledJobTestProbe probe = PROBES.get(parameters.get(PROBE_PARAMETER));
        if (probe == null) {
            throw new IllegalStateException("Scheduled job test probe is no longer registered");
        }
        return probe;
    }

    @Override
    public void close() {

        PROBES.remove(m_token, this);
    }

    /** Observes the entire interval unless an unexpected invocation arrives. */
    void assertNoExecution(int seconds) throws InterruptedException {

        assertNull(m_results.poll(seconds, TimeUnit.SECONDS), "Inactive scheduled job was executed");
    }

    /** Adds the token before the job configuration is frozen by scheduling. */
    void attach(CmsScheduledJobInfo jobInfo) {

        TreeMap<String, String> parameters = new TreeMap<>(jobInfo.getParameters());
        parameters.put(PROBE_PARAMETER, m_token);
        jobInfo.setParameters(parameters);
    }

    /** Waits for the whole sequence with a single deadline, checking each instance count. */
    void awaitExecutions(int... instanceCounts) throws InterruptedException {

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TestCmsScheduler.SECONDS_TO_WAIT);
        for (int i = 0; i < instanceCounts.length; i++) {
            Result result = m_results.poll(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
            assertNotNull(result, "Timed out waiting for scheduled invocation " + (i + 1));
            assertNull(result.m_failure, "Scheduled invocation failed: " + result.m_failure);
            assertEquals(instanceCounts[i], result.m_instanceCount, "Instance count for invocation " + (i + 1));
        }
    }

    /** Publishes only after the invocation has computed all its observations. */
    void record(int instanceCount, String failure) {

        m_results.add(new Result(instanceCount, failure));
    }
}
