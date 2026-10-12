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

package org.opencms.file.quota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.gwt.shared.CmsQuotaStatus;
import org.opencms.gwt.shared.CmsQuotaStatus.State;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for quota inheritance, exclusive usage and warning boundaries.<p>
 */
public class TestCmsQuotaManager {

    @Test
    public void testConfiguredSystemDefaultAndTenantPrecedence() {

        java.util.List<CmsQuotaDefinition> definitions = Arrays.asList(
            new CmsQuotaDefinition("/a", null, null),
            new CmsQuotaDefinition("/a/child", null, null),
            new CmsQuotaDefinition("/b", 2000L, 10000L),
            new CmsQuotaDefinition("/b/child", null, null),
            new CmsQuotaDefinition("/unlimited", CmsQuotaDefinition.UNLIMITED, null));
        Map<String, CmsFolderReportEntry> sizes = Collections.singletonMap("/a/", new CmsFolderReportEntry(900, 900));
        Map<String, CmsQuotaStatus> result = CmsQuotaManager.calculate(definitions, sizes, 1000L);
        assertEquals(1000, result.get("/a/").getQuotaBytes());
        assertEquals(1000, result.get("/a/child/").getQuotaBytes());
        assertEquals(State.WARNING, result.get("/a/").getState());
        assertEquals(10000, result.get("/b/").getQuotaBytes());
        assertEquals(2000, result.get("/b/child/").getQuotaBytes());
        assertEquals(0, result.get("/unlimited/").getQuotaBytes());
        // A changed configuration is effective on the next snapshot, without changing the usage report.
        assertEquals(State.NORMAL, CmsQuotaManager.calculate(definitions, sizes, 2000L).get("/a/").getState());
    }

    @Test
    public void testExclusiveUsageAndMissingSizes() {

        CmsQuotaDefinition scope = new CmsQuotaDefinition("/a", 1000L, null);
        CmsQuotaStatus result = CmsQuotaManager.calculate(
            Collections.singletonList(scope),
            Collections.singletonMap("/a/", new CmsFolderReportEntry(5000, 700))).get("/a/");
        assertEquals(State.NORMAL, result.getState());
        assertEquals(70.0, result.getPercentage(), 0.001);
        assertEquals(5000, result.getTotalBytes());
        assertEquals(
            State.UNAVAILABLE,
            CmsQuotaManager.calculate(Collections.singletonList(scope), Collections.emptyMap()).get("/a/").getState());
    }

    @Test
    public void testIndividualBudgetSourceIsIndependentOfValue() {

        for (long quota : new long[] {0, 1000}) {
            Map<String, CmsQuotaStatus> result = CmsQuotaManager.calculate(
                Arrays.asList(
                    new CmsQuotaDefinition("/inherited", null, null),
                    new CmsQuotaDefinition("/individual", null, quota)),
                Collections.emptyMap(),
                quota);
            assertEquals(result.get("/inherited/").getQuotaBytes(), result.get("/individual/").getQuotaBytes());
            assertFalse(result.get("/inherited/").isIndividualQuota());
            assertTrue(result.get("/individual/").isIndividualQuota());
        }
    }

    @Test
    public void testInheritanceAndIndividualIsolation() {

        Map<String, CmsQuotaStatus> result = CmsQuotaManager.calculate(
            Arrays.asList(
                new CmsQuotaDefinition("/a", 2000L, 10000L),
                new CmsQuotaDefinition("/a/b", null, null),
                new CmsQuotaDefinition("/a/c", 3000L, 7000L),
                new CmsQuotaDefinition("/a/c/d", null, null),
                new CmsQuotaDefinition("/ab", null, null)),
            Collections.emptyMap());
        assertEquals(10000, result.get("/a/").getQuotaBytes());
        assertEquals(2000, result.get("/a/b/").getQuotaBytes());
        assertEquals(7000, result.get("/a/c/").getQuotaBytes());
        assertEquals(3000, result.get("/a/c/d/").getQuotaBytes());
        assertEquals(-1, result.get("/ab/").getQuotaBytes());
    }

    @Test
    public void testInvalidDefaultBlocksSilentFallback() {

        Map<String, CmsQuotaStatus> result = CmsQuotaManager.calculate(
            Arrays.asList(
                new CmsQuotaDefinition("/a", 2000L, null),
                new CmsQuotaDefinition("/a/b", CmsQuotaDefinition.UNAVAILABLE, null),
                new CmsQuotaDefinition("/a/b/c", null, null)),
            Collections.singletonMap("/a/b/c/", new CmsFolderReportEntry(10, 10)));
        assertEquals(State.UNAVAILABLE, result.get("/a/b/c/").getState());
    }

    @Test
    public void testSystemDefaultIsUsedForEachScopeIndependently() {

        long gb = CmsQuotaDefinition.BYTES_PER_GB;
        Map<String, CmsQuotaStatus> result = CmsQuotaManager.calculate(
            Arrays.asList(new CmsQuotaDefinition("/a", null, 10 * gb), new CmsQuotaDefinition("/a/b", null, null)),
            Collections.singletonMap("/a/b/", new CmsFolderReportEntry(gb, gb)),
            2 * gb);
        assertEquals(10 * gb, result.get("/a/").getQuotaBytes());
        assertEquals(2 * gb, result.get("/a/b/").getQuotaBytes());
        assertEquals(50.0, result.get("/a/b/").getPercentage(), 0.001);
        assertEquals(State.UNCONFIGURED, status(2 * gb, null).getState());
    }

    @Test
    public void testThresholdsAndOverflow() {

        assertEquals(State.NORMAL, status(799, 1000L).getState());
        assertEquals(State.WARNING, status(800, 1000L).getState());
        assertEquals(State.WARNING, status(999, 1000L).getState());
        assertEquals(State.EXCEEDED, status(1000, 1000L).getState());
        assertEquals(State.EXCEEDED, status(1500, 1000L).getState());
        assertEquals(150.0, status(1500, 1000L).getPercentage(), 0.001);
        assertEquals(State.NORMAL, status(4, 6L).getState());
        assertEquals(State.WARNING, status(5, 6L).getState());
        long warning = (Long.MAX_VALUE / 5) * 4 + 2;
        assertEquals(State.NORMAL, status(warning - 1, Long.MAX_VALUE).getState());
        assertEquals(State.WARNING, status(warning, Long.MAX_VALUE).getState());
    }

    @Test
    public void testUnknownAndUnlimitedRemainDistinct() {

        assertEquals(State.UNCONFIGURED, status(2000, null).getState());
        assertEquals(-1, status(2000, null).getQuotaBytes());
        assertFalse(status(2000, null).isWarningVisible());
        assertEquals(State.UNLIMITED, status(2000, 0L).getState());
        assertEquals(State.UNAVAILABLE, status(2000, -1L).getState());
        assertEquals(State.UNAVAILABLE, status(-1, 1000L).getState());
        assertFalse(status(799, 1000L).isWarningVisible());
        assertTrue(status(800, 1000L).isWarningVisible());
        assertTrue(status(1000, 1000L).isWarningVisible());
        assertFalse(status(1000, 0L).isWarningVisible());
        assertTrue(status(-1, 1000L).isWarningVisible());
    }

    @Test
    public void testUnlimitedAndInvalidSystemDefaults() {

        java.util.List<CmsQuotaDefinition> definitions = Arrays.asList(
            new CmsQuotaDefinition("/a", null, null),
            new CmsQuotaDefinition("/b", null, 1000L));
        Map<String, CmsFolderReportEntry> sizes = new java.util.HashMap<>();
        sizes.put("/a/", new CmsFolderReportEntry(900, 900));
        sizes.put("/b/", new CmsFolderReportEntry(900, 900));
        Map<String, CmsQuotaStatus> unlimited = CmsQuotaManager.calculate(
            definitions,
            sizes,
            CmsQuotaDefinition.UNLIMITED);
        assertEquals(State.UNLIMITED, unlimited.get("/a/").getState());
        assertEquals(State.WARNING, unlimited.get("/b/").getState());
        Map<String, CmsQuotaStatus> invalid = CmsQuotaManager.calculate(
            definitions,
            sizes,
            CmsQuotaDefinition.UNAVAILABLE);
        assertEquals(State.UNAVAILABLE, invalid.get("/a/").getState());
        assertEquals(State.WARNING, invalid.get("/b/").getState());
    }

    @Test
    public void testUnlimitedDefaultCanBeOverridden() {

        Map<String, CmsQuotaStatus> result = CmsQuotaManager.calculate(
            Arrays.asList(
                new CmsQuotaDefinition("/a", 2000L, null),
                new CmsQuotaDefinition("/a/b", CmsQuotaDefinition.UNLIMITED, null),
                new CmsQuotaDefinition("/a/b/c", null, 500L),
                new CmsQuotaDefinition("/a/b/c/d", null, null)),
            Collections.emptyMap());
        assertEquals(0, result.get("/a/b/").getQuotaBytes());
        assertEquals(500, result.get("/a/b/c/").getQuotaBytes());
        assertEquals(0, result.get("/a/b/c/d/").getQuotaBytes());
    }

    private CmsQuotaStatus status(long used, Long quota) {

        return CmsQuotaManager.calculate(
            Collections.singletonList(new CmsQuotaDefinition("/a", null, quota)),
            Collections.singletonMap("/a/", new CmsFolderReportEntry(used, used))).get("/a/");
    }
}
