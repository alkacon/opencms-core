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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search;

import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.test.OpenCmsTestRunner;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests that search shutdown finishes offline indexing before closing the indexes.<p>
 */
public class TestCmsSearchShutdown extends OpenCmsTestRunner {

    private volatile boolean m_blockUpdates;

    private CountDownLatch m_continueUpdate;

    private CmsSearchIndex m_index;

    private CmsSearchManager m_manager;

    private CountDownLatch m_updateStarted;

    /**
     * Creates a fresh offline index for each shutdown test.<p>
     *
     * @param testInfo the test information
     * @throws Exception if setup fails
     */
    @BeforeEach
    public void setUp(TestInfo testInfo) throws Exception {

        setupOpenCms(testInfo, "simpletest", "/");
        m_manager = OpenCms.getSearchManager();
        m_manager.setOfflineUpdateFrequency(3600000);
        m_blockUpdates = false;
        m_updateStarted = new CountDownLatch(1);
        m_continueUpdate = new CountDownLatch(1);
        m_index = new CmsSearchIndex("Shutdown test") {

            /**
             * @see org.opencms.search.CmsSearchIndex#onIndexChanged(boolean)
             */
            @Override
            public void onIndexChanged(boolean force) {

                if (!force && m_blockUpdates) {
                    m_updateStarted.countDown();
                    try {
                        m_continueUpdate.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                }
                super.onIndexChanged(force);
            }
        };
        m_index.setProject("Offline");
        m_index.setLocale(Locale.ENGLISH);
        m_index.setRebuildMode(I_CmsSearchIndex.REBUILD_MODE_OFFLINE);
        m_index.addSourceName("source1");
        m_index.initialize();
        m_manager.addSearchIndex(m_index);
        m_manager.rebuildIndex(m_index.getName(), null);
        awaitIdleIndexer();
    }

    /**
     * Checks that interrupting shutdown does not close an active index.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testInterruptedShutdown() throws Exception {

        checkRunningAndPendingChangesIndexedOnShutdown(true);
    }

    /**
     * Checks that the complete shell shutdown preserves pending changes across a restart.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testPendingChangesIndexedOnShellExit() throws Exception {

        final String path = createResource("shell-pending.txt");
        restartOpenCms();
        assertIndexed(path);
    }

    /**
     * Checks that changes queued while the indexer sleeps survive shutdown.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testPendingChangesIndexedOnShutdown() throws Exception {

        final String path = createResource("pending.txt");
        m_manager.shutDown();
        assertIndexed(path);
    }

    /**
     * Checks that a running pass and a queued pass both finish before shutdown.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testRunningAndPendingChangesIndexedOnShutdown() throws Exception {

        checkRunningAndPendingChangesIndexedOnShutdown(false);
    }

    /**
     * Checks the committed index directly after the search manager has closed it.<p>
     *
     * @param paths the resource root paths
     * @throws Exception if the index cannot be read
     */
    private void assertIndexed(String... paths) throws Exception {

        try (Directory directory = FSDirectory.open(Paths.get(m_index.getPath()));
        DirectoryReader reader = DirectoryReader.open(directory)) {
            final IndexSearcher searcher = new IndexSearcher(reader);
            for (String path : paths) {
                assertEquals(1, searcher.count(new TermQuery(new Term(CmsSearchField.FIELD_PATH, path))), path);
            }
        }
    }

    /**
     * Waits until the periodic indexer sleeps, so the next change is guaranteed to remain queued.<p>
     *
     * @throws Exception if the indexer does not become idle
     */
    private void awaitIdleIndexer() throws Exception {

        final Field field = CmsSearchManager.class.getDeclaredField("m_offlineIndexThread");
        field.setAccessible(true);
        final Thread indexer = (Thread)field.get(m_manager);
        final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            for (StackTraceElement frame : indexer.getStackTrace()) {
                if (frame.getClassName().equals(Thread.class.getName()) && frame.getMethodName().startsWith("sleep")) {
                    return;
                }
            }
            Thread.sleep(10);
        }
        fail("Offline indexer did not become idle");
    }

    /**
     * Checks that shutdown waits for both a running pass and changes queued behind it.<p>
     *
     * @param interrupt whether to interrupt the shutdown caller while it waits
     * @throws Exception if the test fails
     */
    private void checkRunningAndPendingChangesIndexedOnShutdown(boolean interrupt) throws Exception {

        // The old shutdown gives up after 2.5 times this frequency. Editor latency must not limit shutdown either.
        m_manager.setOfflineUpdateFrequency(20);
        m_manager.setMaxIndexWaitTime(1);
        m_blockUpdates = true;
        final String firstPath = createResource("running.txt");
        m_manager.updateOfflineIndexes(0);
        final CountDownLatch shutdownFinished = new CountDownLatch(1);
        final AtomicBoolean interrupted = new AtomicBoolean();
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        final Thread shutdown = new Thread(() -> {
            try {
                m_manager.shutDown();
                interrupted.set(Thread.currentThread().isInterrupted());
            } catch (Throwable e) {
                failure.set(e);
            } finally {
                shutdownFinished.countDown();
            }
        });
        try {
            assertTrue(m_updateStarted.await(10, TimeUnit.SECONDS), "Offline update did not start");
            final String secondPath = createResource("queued.txt");
            shutdown.start();
            assertFalse(shutdownFinished.await(250, TimeUnit.MILLISECONDS), "Shutdown closed an active index");
            if (interrupt) {
                shutdown.interrupt();
                assertFalse(
                    shutdownFinished.await(250, TimeUnit.MILLISECONDS),
                    "Interruption skipped pending indexing");
            }
            m_continueUpdate.countDown();
            assertTrue(shutdownFinished.await(10, TimeUnit.SECONDS), "Shutdown did not finish after indexing");
            assertNull(failure.get(), "Shutdown failed");
            assertEquals(interrupt, interrupted.get(), "Shutdown must preserve the caller's interrupt status");
            assertIndexed(firstPath, secondPath);
        } finally {
            m_continueUpdate.countDown();
            shutdown.join(10000);
            assertFalse(shutdown.isAlive(), "Shutdown thread did not terminate");
        }
    }

    /**
     * Creates a resource for indexing.<p>
     *
     * @param name the file name
     * @return the resource root path
     * @throws Exception if creation fails
     */
    private String createResource(String name) throws Exception {

        final CmsObject cms = getCmsObject();
        return cms.createResource(
            "/" + name,
            CmsResourceTypePlain.getStaticTypeId(),
            "shutdown test".getBytes(),
            null).getRootPath();
    }
}
