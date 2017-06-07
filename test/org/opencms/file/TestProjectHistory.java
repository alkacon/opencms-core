/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.file.history.CmsHistoryProject;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the project history function of the CmsObject.<p>
 *
 * @since 6.0 alpha 2
 */
public class TestProjectHistory extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestProjectHistory(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestProjectHistory.class.getName());

        suite.addTest(new TestProjectHistory("testProjectHistory"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests the project history function of the CmsObject.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testProjectHistory() throws Throwable {

        CmsObject cms = getCmsObject();

        echo("Testing the project history function");
        projectHistory(cms);
    }

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
        assertEquals(projectHistory.size(), 1);
        historyProject = (CmsHistoryProject)projectHistory.get(0);
        assertEquals(historyProject.getName(), "_setupProject");
    }

}