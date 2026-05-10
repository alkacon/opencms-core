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

package org.opencms.file;

import org.opencms.file.history.CmsHistoryProject;
import org.opencms.test.OpenCmsTestRunner;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Unit test for the project history function of the CmsObject.<p>
 *
 * @since 6.0 alpha 2
 */
public class TestProjectHistory extends OpenCmsTestRunner {

    /**
     * Tests the project history function of the CmsObject.<p>
     *
     * @param cms the CmsObject
     * @throws Throwable if something goes wrong
     */
    public static void projectHistory(CmsObject cms) throws Throwable {

        List projectHistory = null;
        CmsHistoryProject historyProject = null;

        projectHistory = cms.getAllHistoricalProjects();

        // the project history should contain just the setup project here
        assertEquals(1, projectHistory.size());
        historyProject = (CmsHistoryProject)projectHistory.get(0);
        assertEquals("_setupProject", historyProject.getName());
    }

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests the project history function of the CmsObject.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testProjectHistory() throws Throwable {

        CmsObject cms = getCmsObject();

        echo("Testing the project history function");
        projectHistory(cms);
    }

}
