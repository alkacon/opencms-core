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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.apps;

import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the content service for generating serializable XML content entities and type definitions and persisting those entities.<p>
 */
public class TestCmsAppManager extends OpenCmsTestCase {

    /** The schema id. */
    private static final String SCHEMA_SYSTEM_ID_1 = "http://www.opencms.org/test1.xsd";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsAppManager(String arg0) {

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
        suite.setName(TestCmsAppManager.class.getName());

        suite.addTest(new TestCmsAppManager("testCollectAppConfigurations"));

        //        TestSetup wrapper = new TestSetup(suite) {
        //
        //            @Override
        //            protected void setUp() {
        //
        //                setupOpenCms("simpletest", "/");
        //            }
        //
        //            @Override
        //            protected void tearDown() {
        //
        //                removeOpenCms();
        //            }
        //        };

        return suite;
    }

    /**
     * Tests the collect plugin configurations.<p>
     *
     * @throws Exception if something fails
     */
    public void testCollectAppConfigurations() throws Exception {

        CmsWorkplaceAppManager manager = new CmsWorkplaceAppManager();
        Collection<I_CmsWorkplaceAppConfiguration> configs = manager.getWorkplaceApps();

        assertTrue("Should find apps", !configs.isEmpty());
    }
}
