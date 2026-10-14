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

package org.opencms.publish;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/** Tests publish completion without relying on the duration of a publish job. */
public class TestPublishCoordination extends OpenCmsTestRunner {

    private final BlockingQueue<CmsPublishJobRunning> m_finishing = new LinkedBlockingQueue<>();
    private final Semaphore m_continue = new Semaphore(0);
    private final AtomicReference<Throwable> m_listenerFailure = new AtomicReference<>();
    private ExecutorService m_waiters;
    private CmsPublishManager m_manager;
    private I_CmsPublishEventListener m_listener;

    /** Creates an isolated engine and a listener which holds jobs just before completion. */
    @BeforeEach
    public void setUp(TestInfo testInfo) throws Exception {

        setupOpenCms(testInfo, "simpletest", "/");
        m_finishing.clear();
        m_continue.drainPermits();
        m_listenerFailure.set(null);
        m_waiters = Executors.newFixedThreadPool(2);
        m_manager = OpenCms.getPublishManager();
        m_listener = new CmsTestPublishEventListener(getCmsObject().readResource("/folder2/subfolder21/image1.gif")) {

            @Override
            public void onFinish(CmsPublishJobRunning job) {

                m_finishing.add(job);
                try {
                    if (!m_continue.tryAcquire(10, TimeUnit.SECONDS)) {
                        m_listenerFailure.set(new AssertionError("Test did not release the publish listener"));
                    }
                } catch (InterruptedException e) {
                    m_listenerFailure.set(e);
                    Thread.currentThread().interrupt();
                }
            }
        };
        m_manager.addPublishListener(m_listener);
    }

    /** Releases blocked jobs even when an assertion fails. */
    @AfterEach
    public void tearDown(TestInfo testInfo) throws Exception {

        m_continue.release(100);
        try {
            m_manager.waitWhileRunning(5000);
            m_manager.removePublishListener(m_listener);
            m_waiters.shutdownNow();
            assertTrue(m_waiters.awaitTermination(5, TimeUnit.SECONDS));
            assertFalse(m_manager.isRunning());
            assertNull(m_listenerFailure.get());
        } finally {
            removeOpenCms(testInfo);
        }
    }

    /** Aborting queued work must not leave a completion waiter waiting for that job. */
    @Test
    public void testAbortQueuedJob() throws Exception {

        publish("abort-active");
        awaitFinishing();
        publish("abort-queued");
        CmsPublishJobEnqueued queued = m_manager.getPublishQueue().get(0);
        Future<?> waiter = m_waiters.submit(() -> m_manager.waitWhileRunning());
        m_manager.abortPublishJob(getCmsObject(), queued, true);
        assertWaiting(waiter);
        m_continue.release();
        waiter.get(5, TimeUnit.SECONDS);
        assertFalse(m_manager.isRunning());
        assertTrue(m_manager.getPublishQueue().isEmpty());
        assertTrue(m_finishing.isEmpty());
        assertNull(m_manager.getJobByPublishHistoryId(queued.getPublishHistoryId()));
    }

    /** Interrupting a waiter must not allow it to see an unfinished publish as complete. */
    @Test
    public void testInterruptedWaiter() throws Exception {

        publish("interrupt");
        awaitFinishing();
        Future<Boolean> waiter = m_waiters.submit(() -> {
            Thread.currentThread().interrupt();
            m_manager.waitWhileRunning();
            return Boolean.valueOf(Thread.currentThread().isInterrupted());
        });
        assertWaiting(waiter);
        m_continue.release();
        assertTrue(waiter.get(5, TimeUnit.SECONDS).booleanValue());
        assertFalse(m_manager.isRunning());
    }

    /** All waiters must observe history updates and completion of every queued job. */
    @Test
    public void testMultipleWaitersAndQueuedJobs() throws Exception {

        publish("first");
        CmsPublishJobRunning first = awaitFinishing();
        publish("second");
        Future<?> waiter1 = m_waiters.submit(() -> m_manager.waitWhileRunning());
        Future<?> waiter2 = m_waiters.submit(() -> m_manager.waitWhileRunning());
        assertWaiting(waiter1);
        assertWaiting(waiter2);
        m_continue.release();
        CmsPublishJobRunning second = awaitFinishing();
        assertWaiting(waiter1);
        assertWaiting(waiter2);
        m_continue.release();
        waiter1.get(5, TimeUnit.SECONDS);
        waiter2.get(5, TimeUnit.SECONDS);
        assertFalse(m_manager.isRunning());
        assertTrue(m_manager.getPublishQueue().isEmpty());
        assertTrue(
            m_manager.getEngine().getJobByPublishHistoryId(
                first.getPublishHistoryId()) instanceof CmsPublishJobFinished);
        assertTrue(
            m_manager.getEngine().getJobByPublishHistoryId(
                second.getPublishHistoryId()) instanceof CmsPublishJobFinished);
    }

    /** Shutdown must let completion acquire the engine lock and leave queued jobs pending. */
    @Test
    public void testShutdownDuringCompletion() throws Exception {

        m_manager.getEngine().initialize(getCmsObject(), m_manager.isPublishQueuePersistanceEnabled(), 5);
        m_manager.startPublishing();
        publish("shutdown-active");
        CmsPublishJobRunning job = awaitFinishing();
        publish("shutdown-queued");
        Future<?> shutdown = m_waiters.submit(() -> m_manager.getEngine().shutDown());
        assertWaiting(shutdown);
        m_continue.release();
        shutdown.get(2, TimeUnit.SECONDS);
        assertFalse(m_manager.isRunning());
        assertEquals(1, m_manager.getPublishQueue().size());
        assertTrue(
            m_manager.getEngine().getJobByPublishHistoryId(job.getPublishHistoryId()) instanceof CmsPublishJobFinished);
        assertTrue(m_finishing.isEmpty(), "Shutdown must not start another queued job");
    }

    /** A stopped queue must not prevent waiters from returning after the active job finishes. */
    @Test
    public void testStopWithQueuedJob() throws Exception {

        publish("active");
        awaitFinishing();
        publish("queued");
        Future<?> waiter = m_waiters.submit(() -> m_manager.waitWhileRunning());
        m_manager.stopPublishing();
        assertWaiting(waiter);
        m_continue.release();
        waiter.get(5, TimeUnit.SECONDS);
        assertFalse(m_manager.isRunning());
        assertEquals(1, m_manager.getPublishQueue().size());
        m_manager.waitWhileRunning();
        m_manager.startPublishing();
        awaitFinishing();
        m_continue.release();
        m_manager.waitWhileRunning(5000);
        assertTrue(m_manager.getPublishQueue().isEmpty());
    }

    /** A short timeout must not be rounded up to a one-second polling interval. */
    @Test
    public void testTimeout() throws Exception {

        publish("timeout");
        awaitFinishing();
        Future<?> waiter = m_waiters.submit(() -> m_manager.waitWhileRunning(25));
        waiter.get(500, TimeUnit.MILLISECONDS);
        assertTrue(m_manager.isRunning());
        m_manager.waitWhileRunning(0);
        m_manager.waitWhileRunning(-1);
        assertTrue(m_manager.isRunning());
    }

    /** Confirms that a caller cannot return while the publish listener is blocked. */
    private void assertWaiting(Future<?> waiter) {

        assertThrows(TimeoutException.class, () -> waiter.get(100, TimeUnit.MILLISECONDS));
    }

    /** Waits for the publisher to reach the test-controlled completion barrier. */
    private CmsPublishJobRunning awaitFinishing() throws Exception {

        CmsPublishJobRunning job = m_finishing.poll(5, TimeUnit.SECONDS);
        assertNotNull(job, "Publisher did not reach the completion listener");
        return job;
    }

    /** Queues a distinct resource for publication. */
    private void publish(String name) throws Exception {

        CmsObject cms = getCmsObject();
        String path = "/" + name + ".gif";
        cms.copyResource("/folder2/subfolder21/image1.gif", path, CmsResource.COPY_AS_NEW);
        m_manager.publishResource(cms, path);
    }
}
