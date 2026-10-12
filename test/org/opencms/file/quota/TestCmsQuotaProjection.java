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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.gwt.shared.CmsQuotaProjection;
import org.opencms.gwt.shared.CmsQuotaStatus;
import org.opencms.gwt.shared.CmsQuotaStatus.State;

import org.junit.jupiter.api.Test;

/**
 * Budget boundaries for a changing upload selection, including multi-gigabyte files.<p>
 */
public class TestCmsQuotaProjection {

    @Test
    public void testAbsentUnlimitedAndUnavailable() {

        assertFalse(new CmsQuotaProjection(null, 100).isUploadBlocked());
        for (State state : new State[] {State.UNCONFIGURED, State.UNLIMITED, State.UNAVAILABLE}) {
            CmsQuotaStatus original = new CmsQuotaStatus("/test/", 1, 1, state == State.UNLIMITED ? 0 : -1, state);
            CmsQuotaProjection projection = new CmsQuotaProjection(original, Long.MAX_VALUE);
            assertFalse(projection.isUploadBlocked());
            assertSame(original, projection.getProjectedStatus());
        }
        assertFalse(
            new CmsQuotaProjection(
                new CmsQuotaStatus("/test/", -1, -1, 100, State.UNAVAILABLE),
                200).isUploadBlocked());
    }

    @Test
    public void testAlreadyFull() {

        assertTrue(project(100, 100, 1).isAlreadyFull());
        assertTrue(project(100, 100, 1).isUploadBlocked());
        assertTrue(project(101, 100, 1).isUploadBlocked());
    }

    @Test
    public void testLargeSelectionsAndOverflow() {

        assertFalse(project(1000000000L, 5000000000L, 4000000000L).isUploadBlocked());
        assertTrue(project(1000000000L, 5000000000L, 4000000001L).isUploadBlocked());
        CmsQuotaProjection overflow = new CmsQuotaProjection(
            new CmsQuotaStatus("/test/", Long.MAX_VALUE - 2, -1, Long.MAX_VALUE, State.WARNING),
            3);
        assertTrue(overflow.isUploadBlocked());
        assertEquals(Long.MAX_VALUE, overflow.getProjectedStatus().getUsedBytes());
        assertEquals(-1, overflow.getProjectedStatus().getTotalBytes());
    }

    @Test
    public void testSelectionTransitionsAndExactFit() {

        assertEquals(State.NORMAL, project(60, 100, 19).getProjectedStatus().getState());
        assertEquals(State.WARNING, project(60, 100, 20).getProjectedStatus().getState());
        assertFalse(project(60, 100, 39).isUploadBlocked());
        assertEquals(State.EXCEEDED, project(60, 100, 40).getProjectedStatus().getState());
        assertFalse(project(60, 100, 40).isUploadBlocked());
        assertTrue(project(60, 100, 41).isUploadBlocked());
        assertFalse(project(60, 100, 20).isUploadBlocked());
        assertEquals(180, project(60, 100, 20).getProjectedStatus().getTotalBytes());
    }

    @Test
    public void testStoredUsageIsUnchanged() {

        CmsQuotaStatus current = new CmsQuotaStatus("/test/", 60, 80, 100, State.NORMAL);
        CmsQuotaProjection projection = new CmsQuotaProjection(current, 25);
        assertEquals(85, projection.getProjectedStatus().getUsedBytes());
        assertEquals(60, current.getUsedBytes());
        assertEquals(State.NORMAL, current.getState());
    }

    private CmsQuotaProjection project(long used, long quota, long upload) {

        return new CmsQuotaProjection(
            new CmsQuotaStatus("/test/", used, used + 100, quota, CmsQuotaStatus.getUsageState(used, quota)),
            upload);
    }
}
