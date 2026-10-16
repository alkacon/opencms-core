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

package org.opencms.ade.galleries;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.gwt.shared.CmsQuotaStatus;
import org.opencms.gwt.shared.CmsQuotaStatus.State;

import org.junit.jupiter.api.Test;

/**
 * Red-only gallery warnings, session and budget scope isolation, and explicit acknowledgement.<p>
 */
public class TestCmsGalleryQuotaWarnings {

    @Test
    public void testEarlyAcknowledgementCheckIsScopeAndSessionSpecific() {

        CmsGalleryQuotaWarnings warnings = new CmsGalleryQuotaWarnings();
        CmsQuotaStatus red = status("/sites/a/", State.EXCEEDED);
        assertFalse(warnings.isAcknowledged(red.getRootPath()));
        warnings.getWarning(red);
        assertFalse(warnings.isAcknowledged(red.getRootPath()));
        warnings.acknowledge(red.getRootPath(), State.WARNING);
        assertFalse(warnings.isAcknowledged(red.getRootPath()));
        warnings.acknowledge(red.getRootPath(), State.EXCEEDED);
        assertTrue(warnings.isAcknowledged(red.getRootPath()));
        assertFalse(warnings.isAcknowledged("/sites/a/sub/"));
        assertFalse(warnings.isAcknowledged("/sites/b/"));
        assertFalse(new CmsGalleryQuotaWarnings().isAcknowledged(red.getRootPath()));
    }

    @Test
    public void testOnlyOfferedWarningCanBeAcknowledged() {

        CmsGalleryQuotaWarnings warnings = new CmsGalleryQuotaWarnings();
        CmsQuotaStatus orange = status("/sites/a/", State.WARNING);
        CmsQuotaStatus red = status("/sites/a/", State.EXCEEDED);
        warnings.acknowledge(red.getRootPath(), red.getState());
        assertNull(warnings.getWarning(orange));
        warnings.acknowledge(red.getRootPath(), red.getState());
        assertSame(red, warnings.getWarning(red));
    }

    @Test
    public void testOnlyRedRequiresAcknowledgement() {

        CmsGalleryQuotaWarnings warnings = new CmsGalleryQuotaWarnings();
        CmsQuotaStatus orange = status("/sites/a/", State.WARNING);
        CmsQuotaStatus red = status("/sites/a/", State.EXCEEDED);
        assertNull(warnings.getWarning(orange));
        // Merely displaying or closing the red notice must not acknowledge it.
        assertSame(red, warnings.getWarning(red));
        assertSame(red, warnings.getWarning(red));
        warnings.acknowledge(orange.getRootPath(), orange.getState());
        assertNull(warnings.getWarning(orange));
        assertSame(red, warnings.getWarning(red));
        // An old orange dialog can be acknowledged after another tab has already seen red.
        warnings.acknowledge(orange.getRootPath(), orange.getState());
        assertSame(red, warnings.getWarning(red));
        warnings.acknowledge(red.getRootPath(), red.getState());
        warnings.acknowledge(orange.getRootPath(), orange.getState());
        assertNull(warnings.getWarning(red));
        assertNull(warnings.getWarning(orange));
        assertNull(warnings.getWarning(status("/sites/a/", State.NORMAL)));
        assertNull(warnings.getWarning(red));
    }

    @Test
    public void testQuietStatesDoNotResetAcknowledgements() {

        CmsGalleryQuotaWarnings warnings = new CmsGalleryQuotaWarnings();
        CmsQuotaStatus red = status("/sites/a/", State.EXCEEDED);
        warnings.getWarning(red);
        warnings.acknowledge(red.getRootPath(), red.getState());
        assertNull(warnings.getWarning(null));
        for (State state : new State[] {
            State.NORMAL,
            State.WARNING,
            State.UNLIMITED,
            State.UNCONFIGURED,
            State.UNAVAILABLE}) {
            assertNull(warnings.getWarning(status("/sites/a/", state)));
            assertNull(warnings.getWarning(red));
        }
        assertNull(warnings.getWarning(new CmsQuotaStatus("/sites/a/", -1, -1, 100, State.WARNING)));
        assertNull(warnings.getWarning(new CmsQuotaStatus("/sites/a/", 100, 100, 0, State.EXCEEDED)));
    }

    @Test
    public void testSeparateTenantsSubsitesAndSessions() {

        CmsGalleryQuotaWarnings first = new CmsGalleryQuotaWarnings();
        CmsGalleryQuotaWarnings second = new CmsGalleryQuotaWarnings();
        CmsQuotaStatus parent = status("/sites/a/", State.EXCEEDED);
        CmsQuotaStatus child = status("/sites/a/subsite/", State.EXCEEDED);
        CmsQuotaStatus other = status("/sites/b/", State.EXCEEDED);
        first.getWarning(parent);
        first.acknowledge(parent.getRootPath(), parent.getState());
        assertSame(child, first.getWarning(child));
        assertSame(other, first.getWarning(other));
        assertNull(first.getWarning(parent));
        assertSame(parent, second.getWarning(parent));
        // A new session after login starts with no acknowledgements.
        assertSame(parent, new CmsGalleryQuotaWarnings().getWarning(parent));
    }

    private CmsQuotaStatus status(String rootPath, State state) {

        return new CmsQuotaStatus(rootPath, state == State.EXCEEDED ? 110 : 85, 110, 100, state);
    }
}
