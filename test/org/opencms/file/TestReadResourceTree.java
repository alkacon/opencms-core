/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestReadResourceTree.java,v $
 * Date   : $Date: 2004/11/12 17:44:21 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file;

import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "readResources" method of the CmsObject to test reading resource lists within a subtree.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.2 $
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
        assertEquals(this.m_currentResourceStrorage.size(), i);
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
        assertEquals(this.m_currentResourceStrorage.size(), i);
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
        assertEquals(this.m_currentResourceStrorage.size(), i);
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
        assertEquals(this.m_currentResourceStrorage.size(), i);
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
        assertEquals(4, result.size());
        
        result = cms.getSubFolders(path, CmsResourceFilter.ALL);
        assertEquals(3, result.size());     
        
        result = cms.getFilesInFolder(path, CmsResourceFilter.ALL);
        assertEquals(1, result.size());          
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
        cms.touch(resourcename, System.currentTimeMillis(), 0L, 0L, false);

        resourcename = path + "/subfolder22/subsubfolder221/page1.html";
        cms.lockResource(resourcename);
        cms.deleteResource(resourcename, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);

        resourcename = path + "/newpage.html";
        cms.createResource(resourcename, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);

        // store all resources of the expected result
        storeResources(cms, path + "/newpage.html", false);

        // read new resources
        List result = cms.readResources("/", CmsResourceFilter.ALL.addRequireState(I_CmsConstants.C_STATE_NEW));

        // check each resource in the result
        int i;
        for (i = 0; i < result.size(); i++) {
            assertFilter(cms, (CmsResource)result.get(i), OpenCmsTestResourceFilter.FILTER_EQUAL);
        }

        // check the number of resources
        assertEquals(this.m_currentResourceStrorage.size(), i);

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
        assertEquals(this.m_currentResourceStrorage.size(), i);
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
        cms.touch(resourcename, timestamp1 - 1, 0L, 0L, false);

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
        assertEquals(this.m_currentResourceStrorage.size(), i);

        // ensure no resource was modified after timestamp2
        result = cms.readResources("/", CmsResourceFilter.ALL.addRequireLastModifiedAfter(timestamp2));

        if (result.size() > 0) {
            fail("Unexpected modification dates in resources found");
        }

        // now touch the resource with a timestamp after timestamp2 and check that it is now modified after timestamp2
        cms.lockResource(resourcename);
        cms.touch(resourcename, timestamp2 + 1, 0L, 0L, false);

        // store all resources of the expected result
        storeResources(cms, resourcename, false);

        // ensure this resource was modified after timestamp2
        result = cms.readResources("/", CmsResourceFilter.ALL.addRequireLastModifiedAfter(timestamp2));

        // check each resource in the result
        for (i = 0; i < result.size(); i++) {
            assertFilter(cms, (CmsResource)result.get(i), OpenCmsTestResourceFilter.FILTER_EQUAL);
        }

        // check the number of resources
        assertEquals(this.m_currentResourceStrorage.size(), i);
    }
}