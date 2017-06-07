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

import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "readResources" method of the CmsObject to test reading resource lists within a subtree.<p>
 *
 */
public class TestReadResourceTree extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestReadResourceTree(String arg0) {

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
        suite.setName(TestReadResourceTree.class.getName());

        suite.addTest(new TestReadResourceTree("testReadSubtree"));
        suite.addTest(new TestReadResourceTree("testReadChildren"));
        suite.addTest(new TestReadResourceTree("testReadFolders"));
        suite.addTest(new TestReadResourceTree("testReadFiles"));
        suite.addTest(new TestReadResourceTree("testReadResources"));
        suite.addTest(new TestReadResourceTree("testReadModifiedResources"));
        suite.addTest(new TestReadResourceTree("testReadResourcesInTimerange"));

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
     * Test readResources for reading immediate child resources below a given path.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadChildren() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readResources: reading child resources");

        cms.getRequestContext().setSiteRoot("/");

        String path = "/sites/default/folder1/subfolder12";

        // store all resources of the expected result
        // storeResources(cms, path, false);
        storeResources(cms, path + "/subsubfolder121", false);
        storeResources(cms, path + "/index.html", false);
        storeResources(cms, path + "/page1.html", false);
        storeResources(cms, path + "/page2.html", false);

        // read each resource below folder1/subfolder12
        List result = cms.readResources(path, CmsResourceFilter.ALL, false);

        // check each resource in the result
        int i;
        for (i = 0; i < result.size(); i++) {
            assertFilter(cms, (CmsResource)result.get(i), OpenCmsTestResourceFilter.FILTER_EQUAL);
        }

        // check the number of resources
        assertEquals(m_currentResourceStrorage.size(), i);
    }

    /**
     * Test readResources for reading file resources.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadFiles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readResources: reading file resources");

        cms.getRequestContext().setSiteRoot("/");
        String path = "/sites/default/folder2";

        // store all resources of the expected result
        storeResources(cms, path + "/subfolder21/subsubfolder211/jsp1.jsp", false);
        storeResources(cms, path + "/subfolder21/subsubfolder211/jsp2.jsp", false);
        storeResources(cms, path + "/subfolder21/image1.gif", false);
        storeResources(cms, path + "/subfolder21/index.html", false);
        storeResources(cms, path + "/subfolder21/page1.html", false);
        storeResources(cms, path + "/subfolder22/subsubfolder221/index.html", false);
        storeResources(cms, path + "/subfolder22/subsubfolder221/jsp1.jsp", false);
        storeResources(cms, path + "/subfolder22/subsubfolder221/page1.html", false);
        storeResources(cms, path + "/subfolder22/index.html", false);
        storeResources(cms, path + "/subfolder22/page1.html", false);
        storeResources(cms, path + "/subfolder22/page2.html", false);
        storeResources(cms, path + "/subfolder22/page3.html", false);
        storeResources(cms, path + "/image1.gif", false);
        storeResources(cms, path + "/image2.gif", false);
        storeResources(cms, path + "/index.html", false);
        storeResources(cms, path + "/page1.html", false);
        storeResources(cms, path + "/page2.html", false);

        // read each non-folder resource below folder2
        List result = cms.readResources(path, CmsResourceFilter.DEFAULT_FILES);

        // check each resource in the result
        int i;
        for (i = 0; i < result.size(); i++) {
            assertFilter(cms, (CmsResource)result.get(i), OpenCmsTestResourceFilter.FILTER_EQUAL);
        }

        // check the number of resources
        assertEquals(m_currentResourceStrorage.size(), i);
    }

    /**
     * Test readResources for reading folder resources.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadFolders() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readResources: reading folder resources");

        cms.getRequestContext().setSiteRoot("/");

        String path = "/sites/default/folder2";

        // store all resources of the expected result
        // storeResources(cms, path, false);
        storeResources(cms, path + "/subfolder21", false);
        storeResources(cms, path + "/subfolder21/subsubfolder211", false);
        storeResources(cms, path + "/subfolder22", false);
        storeResources(cms, path + "/subfolder22/subsubfolder221", false);

        // read each resource below folder2
        List result = cms.readResources(path, CmsResourceFilter.DEFAULT_FOLDERS);

        // check each resource in the result
        int i;
        for (i = 0; i < result.size(); i++) {
            assertFilter(cms, (CmsResource)result.get(i), OpenCmsTestResourceFilter.FILTER_EQUAL);
        }

        // check the number of resources
        assertEquals(m_currentResourceStrorage.size(), i);
    }

    /**
     * Test readResources for reading modified resources.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadModifiedResources() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readResources: reading modified resources");

        cms.getRequestContext().setSiteRoot("/");

        String path = "/sites/default/folder2";
        String resourcename;

        // touch/create/delete some resources
        resourcename = path + "/subfolder21/subsubfolder211/jsp1.jsp";
        cms.lockResource(resourcename);
        cms.setDateLastModified(resourcename, System.currentTimeMillis(), false);

        resourcename = path + "/subfolder22/subsubfolder221/page1.html";
        cms.lockResource(resourcename);
        cms.deleteResource(resourcename, CmsResource.DELETE_PRESERVE_SIBLINGS);

        resourcename = path + "/newpage.html";
        cms.createResource(resourcename, CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        // store all resources of the expected result
        storeResources(cms, path + "/newpage.html", false);

        // read new resources
        List result = cms.readResources("/", CmsResourceFilter.ALL.addRequireState(CmsResource.STATE_NEW));

        // check each resource in the result
        int i;
        for (i = 0; i < result.size(); i++) {
            assertFilter(cms, (CmsResource)result.get(i), OpenCmsTestResourceFilter.FILTER_EQUAL);
        }

        // check the number of resources
        assertEquals(m_currentResourceStrorage.size(), i);

        // store all resources of the expected result
        storeResources(cms, path + "/subfolder21/subsubfolder211/jsp1.jsp", false);
        storeResources(cms, path + "/subfolder22/subsubfolder221/jsp1.jsp", false); // sibling
        storeResources(cms, path + "/subfolder22/subsubfolder221/page1.html", false);

        // read each resource with a modified state ("not unchanged")
        result = cms.readResources("/", CmsResourceFilter.ALL_MODIFIED);

        // check each resource in the result
        for (i = 0; i < result.size(); i++) {
            assertFilter(cms, (CmsResource)result.get(i), OpenCmsTestResourceFilter.FILTER_EQUAL);
        }

        // check the number of resources
        assertEquals(m_currentResourceStrorage.size(), i);
    }

    /**
     * Test the method that read the direct sub-resources of a folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadResources() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readResources: reading child resources");

        cms.getRequestContext().setSiteRoot("/");

        String path = "/sites/default/";

        List result;

        result = cms.getResourcesInFolder(path, CmsResourceFilter.ALL);
        assertEquals(5, result.size());

        result = cms.getSubFolders(path, CmsResourceFilter.ALL);
        assertEquals(4, result.size());

        result = cms.getFilesInFolder(path, CmsResourceFilter.ALL);
        assertEquals(1, result.size());
    }

    /**
     * Test readResources for reading resources modified within a timerange.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadResourcesInTimerange() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readResources: reading resources modified within a timerange");

        long timestamp1 = CmsResource.DATE_RELEASED_DEFAULT + 1;
        long timestamp2 = System.currentTimeMillis();
        List result = null;

        cms.getRequestContext().setSiteRoot("/");

        String path = "/sites/default/folder1";
        String resourcename;

        // ensure no resource was modified before timestamp1
        result = cms.readResources("/", CmsResourceFilter.ALL.addRequireLastModifiedBefore(timestamp1));

        if (result.size() > 0) {
            fail("Unexpected modification dates in resources found");
        }

        // now touch a resource with a timestamp before timestamp1 and check that it is now modified before timestamp1
        resourcename = path + "/subfolder12/index.html";
        cms.lockResource(resourcename);
        cms.setDateLastModified(resourcename, timestamp1 - 1, false);

        // store all resources of the expected result
        storeResources(cms, resourcename, false);

        // ensure this resource was modified before timestamp1
        result = cms.readResources("/", CmsResourceFilter.ALL.addRequireLastModifiedBefore(timestamp1));

        // check each resource in the result
        int i;
        for (i = 0; i < result.size(); i++) {
            assertFilter(cms, (CmsResource)result.get(i), OpenCmsTestResourceFilter.FILTER_EQUAL);
        }

        // check the number of resources
        assertEquals(m_currentResourceStrorage.size(), i);

        // ensure no resource was modified after timestamp2
        result = cms.readResources("/", CmsResourceFilter.ALL.addRequireLastModifiedAfter(timestamp2));

        if (result.size() > 0) {
            fail("Unexpected modification dates in resources found");
        }

        // now touch the resource with a timestamp after timestamp2 and check that it is now modified after timestamp2
        cms.lockResource(resourcename);
        cms.setDateLastModified(resourcename, timestamp2 + 1, false);

        // store all resources of the expected result
        storeResources(cms, resourcename, false);

        // ensure this resource was modified after timestamp2
        result = cms.readResources("/", CmsResourceFilter.ALL.addRequireLastModifiedAfter(timestamp2));

        // check each resource in the result
        for (i = 0; i < result.size(); i++) {
            assertFilter(cms, (CmsResource)result.get(i), OpenCmsTestResourceFilter.FILTER_EQUAL);
        }

        // check the number of resources
        assertEquals(m_currentResourceStrorage.size(), i);
    }

    /**
     * Test readResources for reading a subtree below a given path.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadSubtree() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing readResources: reading a subtree");

        cms.getRequestContext().setSiteRoot("/");

        String path = "/sites/default/folder1/subfolder12";

        // store all resources of the expected result
        // storeResources(cms, path, false);
        storeResources(cms, path + "/subsubfolder121", false);
        storeResources(cms, path + "/subsubfolder121/image1.gif", false);
        storeResources(cms, path + "/subsubfolder121/index.html", false);
        storeResources(cms, path + "/subsubfolder121/page1.html", false);
        storeResources(cms, path + "/index.html", false);
        storeResources(cms, path + "/page1.html", false);
        storeResources(cms, path + "/page2.html", false);

        // read each resource below folder1/subfolder12
        List result = cms.readResources(path, CmsResourceFilter.ALL);

        // check each resource in the result
        int i;
        for (i = 0; i < result.size(); i++) {
            assertFilter(cms, (CmsResource)result.get(i), OpenCmsTestResourceFilter.FILTER_EQUAL);
        }

        // check the number of resources
        assertEquals(m_currentResourceStrorage.size(), i);
    }
}