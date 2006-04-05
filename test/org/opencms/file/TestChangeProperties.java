/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestChangeProperties.java,v $
 * Date   : $Date: 2005/06/25 12:02:09 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the <code>{@link CmsObject#changeResourcesInFolderWithProperty(String, String, String, String, boolean)}</code>
 * method.<p>
 * 
 * @author Matthias Gafert
 * 
 * @version $Revision: 1.1 $
 */
public class TestChangeProperties extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestChangeProperties(String arg0) {

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
        suite.setName(TestChangeProperties.class.getName());

        suite.addTest(new TestChangeProperties("testChangeResourcesRelativePath"));
        suite.addTest(new TestChangeProperties("testChangeResourcesFullPath"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
            }

            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tries to change the "Description" property of the two files
     * "/folder1/index.html" and "/folder2/index.html" with the site-root
     * "/sites/default".
     * 
     * The test fails, if the <code>recursive</code> parameter of
     * <code>changeResourcesInFolderWithProperty()</code> changes the
     * semantics of the method call.<p> 
     * 
     * @throws Throwable if an error occurs while the test is running
     */
    public void testChangeResourcesRelativePath() throws Throwable {

        CmsObject cms = getCmsObject();

        // Init
        String resource1 = "/folder1/subfolder11/index.html";
        String resource2 = "/folder1/subfolder12/index.html";
        cms.lockResource(resource1);
        cms.lockResource(resource2);
        assertLock(cms, resource1);
        assertLock(cms, resource2);

        // Recursive semantics
        System.out.println("Changing property of \""
            + resource1
            + "\" in \""
            + cms.getRequestContext().getSiteRoot()
            + "\"");

        List l1 = cms.changeResourcesInFolderWithProperty(
            resource1,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            "This is the index page of subfolder11",
            "Changed Value",
            true);

        // Non-recursive semantics
        System.out.println("Changing property of \""
            + resource2
            + "\" in \""
            + cms.getRequestContext().getSiteRoot()
            + "\"");

        List l2 = cms.changeResourcesInFolderWithProperty(
            resource2,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            "This is the index in subfolder12",
            "Changed value",
            false);

        // One resource should have been changed with each call
        assertEquals(l1.size(), l2.size());
    }

    /**
     * Tries to change the "Description" property of the two files
     * "/sites/default/folder1/index.html" and
     * "/sites/default/folder2/index.html" with the site-root "".
     * 
     * The test fails, if the <code>recursive</code> parameter of
     * <code>changeResourcesInFolderWithProperty()</code> changes the
     * semantics of the method call.<p> 
     * 
     * @throws Throwable if an error occurs while the test is running
     */
    public void testChangeResourcesFullPath() throws Throwable {

        CmsObject cms = getCmsObject();

        // Init
        String resource1 = cms.getRequestContext().getSiteRoot() + "/folder2/subfolder21/index.html";
        String resource2 = cms.getRequestContext().getSiteRoot() + "/folder2/subfolder22/index.html";

        cms.getRequestContext().setSiteRoot("");

        cms.lockResource(resource1);
        cms.lockResource(resource2);
        assertLock(cms, resource1);
        assertLock(cms, resource2);

        // Recursive semantics
        System.out.println("Changing property of \""
            + resource1
            + "\" in \""
            + cms.getRequestContext().getSiteRoot()
            + "\"");

        List l1 = cms.changeResourcesInFolderWithProperty(
            resource1,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            "This is the index page in subfolder21",
            "Changed Value",
            true);

        // Non-recursive semantics
        System.out.println("Changing property of \""
            + resource2
            + "\" in \""
            + cms.getRequestContext().getSiteRoot()
            + "\"");

        List l2 = cms.changeResourcesInFolderWithProperty(
            resource2,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            "This is the index page in subfolder22",
            "Changed value",
            false);

        // One resource should have been changed with each call
        assertEquals(l1.size(), l2.size());
    }
}
