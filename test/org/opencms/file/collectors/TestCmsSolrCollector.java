/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file.collectors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.fields.I_CmsSearchField;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the priority resource collectors.<p>
 */
public class TestCmsSolrCollector extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSolrCollector(String arg0) {

        super(arg0);
    }

    /**
     * Initializes the resources needed for the tests.<p>
     * 
     * @param cms the cms object
     * @throws CmsException if something goes wrong
     */
    public static void initResources(CmsObject cms) throws CmsException {

        cms.createResource("/file1", CmsResourceTypePlain.getStaticTypeId());
        cms.createResource("/folder1", CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource("/folder1/file1", CmsResourceTypePlain.getStaticTypeId());
        cms.createResource("/folder1/file2", CmsResourceTypePlain.getStaticTypeId());
        cms.createResource("/folder1/file3", CmsResourceTypePlain.getStaticTypeId());
        cms.createResource("/folder1/file4", CmsResourceTypePlain.getStaticTypeId());
        cms.createResource("/folder1/fileJsp", CmsResourceTypeJsp.getJSPTypeId());
        cms.createResource("/folder1/sub1", CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource("/folder1/sub1/file5", CmsResourceTypePlain.getStaticTypeId());
    }

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsSolrCollector.class.getName());

        suite.addTest(new TestCmsSolrCollector("testByQuery"));
        suite.addTest(new TestCmsSolrCollector("testByContext"));
        suite.addTest(new TestCmsSolrCollector("testByContextWithQuery"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                CmsObject cms = setupOpenCms(null, null, false);
                try {
                    initResources(cms);
                    I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
                    OpenCms.getSearchManager().rebuildIndex("Solr Offline", report);
                } catch (CmsException exc) {
                    exc.printStackTrace();
                    fail(exc.getMessage());
                }
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests the "allInFolderPriorityDesc" resource collector.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testByQuery() throws Throwable {

        CmsObject cms = getCmsObject();
        String resTypePlainName = CmsResourceTypePlain.getStaticTypeName();
        echo("Testing allInFolderPriorityDateDesc resource collector");

        I_CmsResourceCollector collector = new CmsSolrCollector();

        StringBuffer q = new StringBuffer(128);
        q.append("q=");
        q.append("+parent-folders:/sites/default/folder1/ -parent-folders:/sites/default/folder1/*/");
        q.append(" +type:" + resTypePlainName);
        q.append("&rows=" + 3);
        q.append("&start=0&type=dismax&fl=*,score");
        q.append("&sort=" + I_CmsSearchField.FIELD_DATE_LASTMODIFIED + " desc");
        List<CmsResource> resources = collector.getResults(cms, "byQuery", q.toString());

        // assert that 3 files are returned
        assertEquals(3, resources.size());

        CmsResource res;
        res = resources.get(0);
        assertEquals("/sites/default/folder1/file4", res.getRootPath());
        res = resources.get(1);
        assertEquals("/sites/default/folder1/file3", res.getRootPath());
        res = resources.get(2);
        assertEquals("/sites/default/folder1/file2", res.getRootPath());
    }

    /**
     * Tests the "allInFolderPriorityDesc" resource collector.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testByContext() throws Throwable {

        echo("Testing testByContext resource collector");

        I_CmsResourceCollector collector = new CmsSolrCollector();
        List<CmsResource> resources = collector.getResults(getCmsObject(), "byContext", null);

        // assert that 3 files are returned
        assertEquals(6, resources.size());
    }

    /**
     * Tests the "allInFolderPriorityDesc" resource collector.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testByContextWithQuery() throws Throwable {

        echo("Testing testByContextWithQuery resource collector");

        I_CmsResourceCollector collector = new CmsSolrCollector();
        StringBuffer q = new StringBuffer(128);
        q.append("q=");
        q.append("+type:" + CmsResourceTypePlain.getStaticTypeName());
        q.append("&rows=" + 3);
        q.append("&sort=" + I_CmsSearchField.FIELD_DATE_LASTMODIFIED + " desc");
        List<CmsResource> resources = collector.getResults(getCmsObject(), "byContext", q.toString());

        // assert that 3 files are returned
        assertEquals(3, resources.size());

        CmsResource res;
        res = resources.get(0);
        assertEquals("/sites/default/folder1/sub1/file5", res.getRootPath());
        res = resources.get(1);
        assertEquals("/sites/default/folder1/file4", res.getRootPath());
        res = resources.get(2);
        assertEquals("/sites/default/folder1/file3", res.getRootPath());
    }
}
