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

package org.opencms.search;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for searching in "offline" indexes, added with OpenCms version 7.5.<p>
 */
public class TestCmsSearchOffline extends OpenCmsTestCase {

    /** Name of the search index created using API. */
    public static final String INDEX_SPECIAL = "Offline Index";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsSearchOffline(String arg0) {

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
        suite.setName(TestCmsSearchOffline.class.getName());

        suite.addTest(new TestCmsSearchOffline("testSearchIndexSetup"));
        suite.addTest(new TestCmsSearchOffline("testIndexUpdateOnModification"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
                OpenCms.getSearchManager().setOfflineUpdateFrequency(1000);
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Creates a new search index setup for this test.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testSearchIndexSetup() throws Exception {

        CmsSearchIndex searchIndex = new CmsSearchIndex(INDEX_SPECIAL);
        searchIndex.setProject("Offline");
        searchIndex.setLocale(Locale.ENGLISH);
        searchIndex.setRebuildMode(CmsSearchIndex.REBUILD_MODE_OFFLINE);
        // available pre-configured in the test configuration files opencms-search.xml
        searchIndex.addSourceName("source1");

        // initialize the new index
        searchIndex.initialize();

        // add the search index to the manager
        OpenCms.getSearchManager().addSearchIndex(searchIndex);

        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        // this call does not throws the rebuild index event
        OpenCms.getSearchManager().rebuildIndex(INDEX_SPECIAL, report);

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        searchBean.init(getCmsObject());
        searchBean.setIndex(INDEX_SPECIAL);
        searchBean.setQuery(">>SearchEgg1<<");

        // assert one file is found in the default site
        searchResult = searchBean.getSearchResult();
        assertEquals(1, searchResult.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", searchResult.get(0).getPath());
    }

    /**
     * Delays execution.<p>
     *
     * @throws InterruptedException if sth. goes wrong
     */
    protected void waitForUpdate() throws InterruptedException {

        // wait for the offline index
        Thread.sleep(OpenCms.getSearchManager().getOfflineUpdateFrequency() * 2);
    }

    /**
     * Tests automatic index update after modification of a resource.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testIndexUpdateOnModification() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing automatic index update after modification of a resource");

        // create test folder
        cms.createResource("/test/", CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, null);
        cms.unlockResource("/test/");

        // create a new resource
        String fileName = "/test/test.txt";
        String text1 = "Alkacon OpenCms is great!";
        cms.createResource(fileName, CmsResourceTypePlain.getStaticTypeId(), text1.getBytes(), null);

        // wait for the offline index
        waitForUpdate();

        // search for the new resource in the offline index
        CmsSearch cmsSearchBean = new CmsSearch();
        cmsSearchBean.setIndex(INDEX_SPECIAL);
        cmsSearchBean.setSearchRoot("/");
        cmsSearchBean.setQuery("+Alkacon +OpenCms");
        List<CmsSearchResult> results;
        cmsSearchBean.init(cms);

        results = cmsSearchBean.getSearchResult();

        TestCmsSearch.printResults(results, cms);
        assertEquals(8, results.size());
        assertEquals("/sites/default/test/test.txt", results.get(7).getPath());

        cms.writePropertyObject(
            fileName,
            new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "Alkacon OpenCms IN THE NEW FILE", ""));

        // wait for the offline index
        waitForUpdate();
        // repeat the last search with the same parameters
        cmsSearchBean.setQuery("+Alkacon +OpenCms");
        results = cmsSearchBean.getSearchResult();

        TestCmsSearch.printResults(results, cms);
        assertEquals(8, results.size());
        assertEquals("/sites/default/test/test.txt", (results.get(1)).getPath());

        echo("Delete Test - start");

        // delete a resource
        String deleteFileName = "/types/text.txt";
        cms.lockResource(deleteFileName);
        cms.deleteResource(deleteFileName, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // wait for the offline index
        waitForUpdate();
        // repeat the last search with the same parameters
        cmsSearchBean.setQuery("+Alkacon +OpenCms");
        results = cmsSearchBean.getSearchResult();

        TestCmsSearch.printResults(results, cms);
        assertEquals(7, results.size());
        assertEquals("/sites/default/test/test.txt", (results.get(0)).getPath());

        echo("Delete Test - end");
        echo("Delete New Test - start");

        OpenCms.getSearchManager().getIndex(INDEX_SPECIAL).setCheckPermissions(false);
        String fileName222 = "/test/test222.txt";
        String text222 = "Alkacon OpenCms is so great!";
        cms.createResource(fileName222, CmsResourceTypePlain.getStaticTypeId(), text222.getBytes(), null);
        // wait for the offline index
        waitForUpdate();
        cmsSearchBean.setQuery("+\"Alkacon OpenCms is so great!\"");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        cms.deleteResource(fileName222, CmsResource.DELETE_PRESERVE_SIBLINGS);
        waitForUpdate();
        cmsSearchBean.setQuery("+\"Alkacon OpenCms is so great!\"");
        results = cmsSearchBean.getSearchResult();
        assertEquals(0, results.size());
        OpenCms.getSearchManager().getIndex(INDEX_SPECIAL).setCheckPermissions(true);

        echo("Delete New Test - end");
        echo("Move Test - start");

        // move a resource
        String moveFileName = "/test/test_moved.txt";
        cms.moveResource(fileName, moveFileName);

        // wait for the offline index
        waitForUpdate();
        // repeat the last search with the same parameters
        cmsSearchBean.setQuery("+Alkacon +OpenCms");
        results = cmsSearchBean.getSearchResult();

        TestCmsSearch.printResults(results, cms);
        assertEquals(7, results.size());
        assertEquals("/sites/default/test/test_moved.txt", (results.get(0)).getPath());

        echo("Move Test - end");
    }
}