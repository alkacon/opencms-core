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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Manual benchmark for a realistic fixture: the minimal "systemtest" base import plus the
 * <code>alkacon.mercury.test</code> module export imported on top (with the core
 * <code>org.opencms.base</code> module that it depends on).<p>
 *
 * It reports two numbers the snapshot optimization trades off:<p>
 *
 * <ol>
 * <li>the initial import time (the one-time cold build of the snapshot template, which runs
 * the full base import plus the modules, paid once per JVM run), and
 * <li>the cached setup time (a restore from the populated template cache, paid per test).
 * </ol>
 *
 * Only a single full import runs (the cold build); the warm restores reuse it, so the
 * benchmark does not pay the slow import more than once.<p>
 *
 * The class name deliberately does <i>not</i> start with "Test", so it is excluded from the
 * regular test suite (which runs only <code>Test*</code> classes). Run it explicitly:<p>
 *
 * <pre>
 * ./gradlew testSingle --tests "org.opencms.test.performance.MercurySnapshotBenchmark"
 * </pre>
 */
public class MercurySnapshotBenchmark extends OpenCmsTestSnapRunner {

    /** Number of cached restores to time. */
    private static final int RESTORES = 3;

    /** The minimal base import folder the module needs. */
    private static final String IMPORT_FOLDER = "systemtest";

    /** The module export to import on top of the base (file: WEB-INF/packages/<name>.zip). */
    private static final String MODULE_NAME = "alkacon.mercury.test";

    /** The import target folder. */
    private static final String TARGET_FOLDER = "/";

    /**
     * Times the one-time cold snapshot build (the initial import) and the warm cached
     * restores, then prints a report.<p>
     *
     * @param testInfo the JUnit test info object
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void benchmarkMercuryFixture(TestInfo testInfo) throws Exception {

        // org.opencms.base provides the ADE / formatter configuration the module relies on and
        // must be imported first; the snapshot runner handles its special import and the error
        // suppression internally
        List<String> modules = List.of(MODULE_OPENCMS_BASE, MODULE_NAME);

        // cold build: the first call runs the full base import plus the modules once
        long buildStart = System.currentTimeMillis();
        setupOpenCms(testInfo, IMPORT_FOLDER, TARGET_FOLDER, modules);
        long buildPlusFirstRestore = System.currentTimeMillis() - buildStart;

        // warm restores: subsequent calls reuse the cached template (the per-test cost)
        long restoreTotal = 0;
        for (int i = 0; i < RESTORES; i++) {
            long start = System.currentTimeMillis();
            setupOpenCms(testInfo, IMPORT_FOLDER, TARGET_FOLDER, modules);
            restoreTotal += System.currentTimeMillis() - start;
        }
        double restoreAvg = (double)restoreTotal / RESTORES;
        long buildOnly = Math.max(0, buildPlusFirstRestore - Math.round(restoreAvg));

        StringBuilder report = new StringBuilder();
        report.append("\n========== MERCURY FIXTURE BENCHMARK ==========\n");
        report.append(String.format("Fixture: '%s' + module '%s'%n", IMPORT_FOLDER, MODULE_NAME));
        report.append("-----------------------------------------------\n");
        report.append(String.format("Initial import (cold build)    : ~%d ms%n", Long.valueOf(buildOnly)));
        report.append(String.format("  (cold build + first restore) : %d ms%n", Long.valueOf(buildPlusFirstRestore)));
        report.append(
            String.format(
                "Cached setup (warm cache)      : avg %.0f ms over %d restores%n",
                Double.valueOf(restoreAvg),
                Integer.valueOf(RESTORES)));
        report.append("-----------------------------------------------\n");
        report.append(
            String.format(
                "Cached vs initial import       : %d -> %.0f ms (%.1fx faster)%n",
                Long.valueOf(buildOnly),
                Double.valueOf(restoreAvg),
                Double.valueOf(buildOnly / restoreAvg)));
        report.append("===============================================\n");
        System.out.println(report);
    }
}
