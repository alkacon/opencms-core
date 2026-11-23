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

package org.opencms.main;

import org.opencms.file.quota.CmsFolderSizeTracker;
import org.opencms.test.OpenCmsTestRunner;

import java.lang.reflect.Field;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests when the folder size trackers are started.<p>
 *
 * Loading a tracker reads the folder sizes of the whole VFS, which is expensive on a large database.
 * The unit tests run OpenCms at the shell run level, like the OpenCms shell ({@link CmsShell}).
 */
public class TestFolderSizeTrackerStartup extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests that the shell does not load the folder sizes on startup, but on first use.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testShellLoadsFolderSizesOnFirstUse() throws Exception {

        assertEquals(OpenCms.RUNLEVEL_3_SHELL_ACCESS, OpenCms.getRunLevel());
        assertNull(getTrackerFuture("m_folderSizeTrackerFuture"), "Offline tracker started on shell startup");
        assertNull(getTrackerFuture("m_onlineFolderSizeTrackerFuture"), "Online tracker started on shell startup");

        CmsFolderSizeTracker offline = OpenCms.getFolderSizeTracker(false);
        CmsFolderSizeTracker online = OpenCms.getFolderSizeTracker(true);
        assertNotNull(offline);
        assertNotNull(online);
        assertSame(offline, OpenCms.getFolderSizeTracker(false));
        assertSame(online, OpenCms.getFolderSizeTracker(true));
    }

    /**
     * Reads one of the private tracker futures of the running OpenCms instance.<p>
     *
     * @param fieldName the name of the field
     * @return the future, or null if the tracker has not been started
     *
     * @throws Exception if the field cannot be read
     */
    private Future<?> getTrackerFuture(String fieldName) throws Exception {

        Field field = OpenCmsCore.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (Future<?>)field.get(OpenCmsCore.getInstance());
    }
}
