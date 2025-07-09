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

package org.opencms.util;

import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Test cases for org.opencms.util.CmsPriorityLock.
 */
public class TestCmsPriorityLock extends OpenCmsTestCase {

    public void testFairness() throws Exception {

        CmsPriorityLock lock = new CmsPriorityLock();
        ExecutorService ex = Executors.newCachedThreadPool();
        CountDownLatch[] signals = new CountDownLatch[] {new CountDownLatch(1), new CountDownLatch(1)};
        List<String> order = new CopyOnWriteArrayList<>();
        lock.lock(false);
        for (int i = 0; i <= 1; i++) {
            final int j = i;
            final String name = "H" + i;
            ex.submit(() -> {
                try {
                    signals[j].await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.lock(true);
                try {
                    order.add(name);
                } finally {
                    lock.unlock();
                }
            });
        }
        signals[0].countDown();
        Thread.sleep(10);
        signals[1].countDown();
        Thread.sleep(10);
        lock.unlock();
        ex.shutdown();
        ex.awaitTermination(1, TimeUnit.SECONDS);
        assertEquals(List.of("H0", "H1"), order);
    }

    public void testPriority() throws Exception {

        CmsPriorityLock lock = new CmsPriorityLock();
        StringBuffer buffer = new StringBuffer();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (i == 1) {
                Thread.sleep(1);
            }
            final boolean high = i > 4;
            Thread t = new Thread() {

                public void run() {

                    try {
                        lock.lock(high);

                        try {
                            buffer.append((high ? "H" : "L"));
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            lock.unlock();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
            threads.add(t);
        }
        for (Thread t : threads) {
            t.join();
        }
        // aside from the first thread, the high priority threads get to acquire the lock first
        assertEquals("LHHHHHLLLL", buffer.toString());

    }

    public void testReentrantLocking() throws Exception {

        CmsPriorityLock lock = new CmsPriorityLock();
        lock.lock(true);
        lock.lock(true);
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<Boolean> blocked = ex.submit(() -> {
            lock.lock(false);
            lock.unlock();
            return true;
        });
        lock.unlock();
        Thread.sleep(50);
        assertFalse("Should still be blocked", blocked.isDone());
        lock.unlock();
        assertTrue(blocked.get(100, TimeUnit.MILLISECONDS));
        ex.shutdownNow();
    }

}