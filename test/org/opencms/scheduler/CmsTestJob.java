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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

/** Job used to exercise concurrent execution and completion in the scheduler thread pool. */
public class CmsTestJob implements Job {

    /** Synchronization and observations owned by a single pool test. */
    static final class Probe {

        /** The default pool can run ten jobs concurrently. */
        private final CountDownLatch m_firstBatchStarted = new CountDownLatch(10);
        private final CountDownLatch m_release = new CountDownLatch(1);
        private final CountDownLatch m_finished = new CountDownLatch(TestCmsScheduler.THREADS_TO_RUN);
        private final Queue<String> m_completed = new ConcurrentLinkedQueue<>();
        private final Queue<String> m_failures = new ConcurrentLinkedQueue<>();

        /** Called after scheduler shutdown, when no more results can be published. */
        void assertCompleted(Set<String> expectedJobs) {

            assertTrue(m_failures.isEmpty(), "Job failures: " + m_failures);
            assertEquals(expectedJobs.size(), m_completed.size(), "Each job must complete exactly once");
            assertEquals(expectedJobs, new HashSet<>(m_completed));
        }

        void awaitCompletion() throws InterruptedException {

            assertTrue(m_finished.await(TestCmsScheduler.SECONDS_TO_WAIT, TimeUnit.SECONDS), "Not all jobs completed");
        }

        void awaitFirstBatch() throws InterruptedException {

            assertTrue(
                m_firstBatchStarted.await(TestCmsScheduler.SECONDS_TO_WAIT, TimeUnit.SECONDS),
                "Ten jobs did not start concurrently");
        }

        void release() {

            m_release.countDown();
        }
    }

    /** Job-data key for this test's probe. */
    static final String PROBE_KEY = "testProbe";

    /**
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) {

        Probe probe = (Probe)context.getMergedJobDataMap().get(PROBE_KEY);
        String jobName = context.getJobDetail().getKey().getName();
        probe.m_firstBatchStarted.countDown();
        try {
            if (!probe.m_release.await(TestCmsScheduler.SECONDS_TO_WAIT, TimeUnit.SECONDS)) {
                probe.m_failures.add("Timed out waiting for release: " + jobName);
            }
        } catch (InterruptedException e) {
            probe.m_failures.add("Interrupted while waiting for release: " + jobName);
            Thread.currentThread().interrupt();
        } finally {
            probe.m_completed.add(jobName);
            probe.m_finished.countDown();
        }
    }
}
