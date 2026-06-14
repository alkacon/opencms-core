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

import org.opencms.test.OpenCmsTestSnapRunner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Manual benchmark comparing three OpenCms setup strategies:<p>
 *
 * <ol>
 * <li>traditional import based setup,
 * <li>snapshot setup restoring only the database,
 * <li>snapshot setup restoring the database plus the RFS config and search index state.
 * </ol>
 *
 * The class name deliberately does <i>not</i> start with "Test", so it is excluded from the
 * regular test suite (which runs only <code>Test*</code> classes) and the (slow) repeated
 * imports do not run on every build. Run it explicitly:<p>
 *
 * <pre>
 * ./gradlew testSingle --tests "org.opencms.test.performance.SnapshotSetupBenchmark"
 * </pre>
 */
public class SnapshotSetupBenchmark extends OpenCmsTestSnapRunner {

    /** Number of setups to run for each variant. */
    private static final int N = 5;

    /** The import fixture folder. */
    private static final String IMPORT_FOLDER = "simpletest";

    /** The import target folder. */
    private static final String TARGET_FOLDER = "/";

    /**
     * Measures the one-time cold build (first setup, which runs the full import) against N warm
     * restores (subsequent setups served from the template cache) and reports the speedup.<p>
     *
     * @param testInfo the JUnit test info object
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void benchmarkColdVsWarm(TestInfo testInfo) throws Exception {

        // cold: the first setup builds the snapshot template (full import) then restores once
        long coldStart = System.currentTimeMillis();
        setupOpenCms(testInfo, IMPORT_FOLDER, TARGET_FOLDER);
        long coldPlusFirstRestore = System.currentTimeMillis() - coldStart;

        // warm: subsequent setups are served from the template cache
        long restoreTotal = 0;
        for (int i = 0; i < N; i++) {
            long start = System.currentTimeMillis();
            setupOpenCms(testInfo, IMPORT_FOLDER, TARGET_FOLDER);
            restoreTotal += System.currentTimeMillis() - start;
        }
        double restoreAvg = (double)restoreTotal / N;
        long buildOnly = Math.max(0, coldPlusFirstRestore - Math.round(restoreAvg));

        StringBuilder report = new StringBuilder();
        report.append("\n========== SETUP BENCHMARK (N=").append(N).append(", fixture '").append(IMPORT_FOLDER).append(
            "') ==========\n");
        report.append(String.format("Cold build (full import)     : ~%d ms%n", Long.valueOf(buildOnly)));
        report.append(String.format("  (cold build + first restore): %d ms%n", Long.valueOf(coldPlusFirstRestore)));
        report.append(
            String.format(
                "Warm restore                 : avg %.0f ms/setup over %d restores%n",
                Double.valueOf(restoreAvg),
                Integer.valueOf(N)));
        report.append("--------------------------------------------------------\n");
        report.append(
            String.format(
                "Warm vs cold build: %d -> %.0f ms (%.1fx faster)%n",
                Long.valueOf(buildOnly),
                Double.valueOf(restoreAvg),
                Double.valueOf(buildOnly / restoreAvg)));
        report.append("========================================================\n");
        System.out.println(report);
    }
}
