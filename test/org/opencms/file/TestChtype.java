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

import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.security.CmsSecurityException;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "chtype" method of the CmsObject.<p>
 *
 */
public class TestChtype extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestChtype(String arg0) {

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
        suite.setName(TestChtype.class.getName());

        suite.addTest(new TestChtype("testChtypeNewFile"));
        suite.addTest(new TestChtype("testChtypeJspFile"));

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
     * Test the chtype method on a new file.<p>
     *
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to change permissions
     * @param originalResType the original resource type
     * @param newResType the new resource tpye
     * @throws Throwable if something goes wrong
     */
    public static void chtypeNewFile(
        OpenCmsTestCase tc,
        CmsObject cms,
        String resource1,
        int originalResType,
        int newResType) throws Throwable {

        // create a new resource
        cms.createResource(resource1, originalResType);
        tc.storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();

        cms.chtype(resource1, newResType);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_CHTYPE);
        // date lastmodified must be new
        tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
        // the type must be the new type
    }

    /**
     * Test the chtype method on a new file.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testChtypeNewFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing chtype on a new file");
        chtypeNewFile(
            this,
            cms,
            "/chtype.txt",
            CmsResourceTypePlain.getStaticTypeId(),
            CmsResourceTypeBinary.getStaticTypeId());
    }

    /**
    * Test the chtype method on a file to jsp without permissions.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testChtypeJspFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing chtype on a new file");
        CmsProject offlineProject = cms.getRequestContext().getCurrentProject();

        // this should work since we are admin
        cms.chtype("/chtype.txt", CmsResourceTypeJsp.getJSPTypeId());

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(offlineProject);

        try {
            chtypeNewFile(
                this,
                cms,
                "/chtype2.txt",
                CmsResourceTypePlain.getStaticTypeId(),
                CmsResourceTypeJsp.getJSPTypeId());
            fail("chtype to jsp without permissions should fail");
        } catch (CmsSecurityException e) {
            // ok
        }
    }
}