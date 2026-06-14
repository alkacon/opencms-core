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

package org.opencms.test.performance;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestSnapRunner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Verifies the snapshot based OpenCms setup: every restore is fast, contains the imported
 * fixture data and is fully isolated from changes made by the previous test.<p>
 */
public class TestSnapshotSetup extends OpenCmsTestSnapRunner {

    /** Marker resource used to prove that each restored instance is fresh. */
    private static final String MARKER = "/isolation-marker.txt";

    /**
     * Runs several snapshot setups, asserting fixture data presence and test isolation,
     * and prints the per-setup timings for tuning.<p>
     *
     * @param testInfo the JUnit test info object
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void benchmarkAndIsolation(TestInfo testInfo) throws Exception {

        int iterations = 4;
        long[] timings = new long[iterations];
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            CmsObject cms = setupOpenCms(testInfo, "simpletest", "/");
            timings[i] = System.currentTimeMillis() - start;

            // the imported "simpletest" data must be present in every restored instance
            assertTrue(cms.existsResource("/index.html"), "fixture data missing in restored instance");
            // isolation: a resource created in the previous iteration must not survive into this one
            assertFalse(cms.existsResource(MARKER), "snapshot not fresh - marker from previous test leaked");

            // mutate this instance so the next iteration can prove it was reset
            cms.createResource(MARKER, OpenCms.getResourceManager().getResourceType("plain"));
            assertTrue(cms.existsResource(MARKER));
        }

        System.out.println("----- Snapshot setup timings -----");
        System.out.println("iteration 0 (build template + restore): " + timings[0] + " ms");
        for (int i = 1; i < iterations; i++) {
            System.out.println("iteration " + i + " (restore only)        : " + timings[i] + " ms");
        }
    }
}
