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

package org.opencms.repository;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test for Webdav repositories.<p>
 */
public class TestRepository extends OpenCmsTestCase {

    /**
     * Create test instance.<p>
     *
     * @param name the test name
     */
    public TestRepository(String name) {

        super(name);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestRepository.class.getName());
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
        suite.addTest(new TestRepository("testPropertyCachingBug"));

        return wrapper;
    }

    /**
     * Test for a bug with property caching caused by CmsResourceWrapperSystemFolder.<p>
     *
     * @throws Exception
     */
    public void testPropertyCachingBug() throws Exception {

        OpenCms.getEventManager().fireEvent(I_CmsEventListener.EVENT_CLEAR_CACHES);
        OpenCms.getMemoryMonitor().clearCache();
        OpenCms.getRepositoryManager().getRepository("standard", CmsRepository.class).login("Admin", "admin").getItem(
            "/");
        CmsProperty templateElements = getCmsObject().readPropertyObject(
            "/",
            CmsPropertyDefinition.PROPERTY_TEMPLATE,
            true);
        System.out.println(templateElements);
        assertTrue(
            "template-elements property should not be empty",
            !CmsStringUtil.isEmptyOrWhitespaceOnly(templateElements.getValue()));
    }
}
