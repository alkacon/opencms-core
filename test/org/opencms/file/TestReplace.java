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

import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.lock.CmsLockType;
import org.opencms.security.CmsSecurityException;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for replace operation.<p>
 */
public class TestReplace extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestReplace(String arg0) {

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
        suite.setName(TestReplace.class.getName());

        suite.addTest(new TestReplace("testReplaceResourceContent"));
        suite.addTest(new TestReplace("testReplaceResourceJsp"));

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
     * Tests the "replace resource" operation.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReplaceResourceContent() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing replacement of file content");

        String path = "/types/text.txt";
        String contentStr = "Hello this is the new content";

        long timestamp = System.currentTimeMillis();

        storeResources(cms, path);
        cms.lockResource(path);
        cms.replaceResource(path, CmsResourceTypePlain.getStaticTypeId(), contentStr.getBytes(), null);

        // project must be current project
        assertProject(cms, path, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, path, CmsResource.STATE_CHANGED);
        // date lastmodified must be new
        assertDateLastModifiedAfter(cms, path, timestamp);
        // user lastmodified must be current user
        assertUserLastModified(cms, path, cms.getRequestContext().getCurrentUser());
        // assert lock state
        assertLock(cms, path, CmsLockType.EXCLUSIVE);
        // assert new content
        assertContent(cms, path, contentStr.getBytes());
        // now check the rest of the attributes
        assertFilter(cms, path, OpenCmsTestResourceFilter.FILTER_REPLACERESOURCE);
    }

    /**
     * Tests the "replace resource" operation for jsp without permissions.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReplaceResourceJsp() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing replacement of file for jsp without permissions");
        CmsProject offlineProject = cms.getRequestContext().getCurrentProject();

        String path = "/types/text.txt";
        String contentStr = "Hello this is the new content";

        // this should work since we are admin
        cms.replaceResource(path, CmsResourceTypeJsp.getJSPTypeId(), contentStr.getBytes(), null);
        cms.unlockResource(path);

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(offlineProject);

        try {
            cms.lockResource(path);
            cms.replaceResource(path, CmsResourceTypePlain.getStaticTypeId(), contentStr.getBytes(), null);
            fail("replaceResource from jsp without permissions should fail");
        } catch (CmsSecurityException e) {
            // ok
        }

        cms = getCmsObject();
        cms.lockResource(path);
        cms.replaceResource(path, CmsResourceTypePlain.getStaticTypeId(), contentStr.getBytes(), null);
        cms.unlockResource(path);

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(offlineProject);

        try {
            cms.lockResource(path);
            cms.replaceResource(path, CmsResourceTypeJsp.getJSPTypeId(), contentStr.getBytes(), null);
            fail("replaceResource to jsp without permissions should fail");
        } catch (CmsSecurityException e) {
            // ok
        }
    }
}
