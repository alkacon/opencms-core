/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/search/TestCmsSearch.java,v $
 * Date   : $Date: 2005/03/24 17:38:21 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the cms search indexer.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.9 $
 */
public class TestCmsSearch extends OpenCmsTestCase {

    /** Name of the index used for testing. */
    public static final String INDEX_OFFLINE = "Offline project (VFS)";

    /** Name of the search index created using API. */
    public static final String INDEX_TEST = "Test new index";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSearch(String arg0) {

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
        suite.setName(TestCmsSearch.class.getName());

        suite.addTest(new TestCmsSearch("testCmsSearchIndexer"));
        suite.addTest(new TestCmsSearch("testCmsSearchDocumentTypes"));
        suite.addTest(new TestCmsSearch("testCmsSearchXmlContent"));
        suite.addTest(new TestCmsSearch("testIndexGeneration"));
        
        // This test is intended only for performance/resource monitoring
        // suite.addTest(new TestCmsSearch("testCmsSearchLargeResult"));

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
     * Tests searching in various document types.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsSearchDocumentTypes() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing search for various document types");

        CmsSearch cmsSearchBean = new CmsSearch();
        cmsSearchBean.init(cms);
        cmsSearchBean.setIndex(INDEX_OFFLINE);
        cmsSearchBean.setSearchRoot("/types/");
        List results;

        cmsSearchBean.setQuery("+Alkacon +OpenCms +Text");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        assertEquals("/sites/default/types/text.txt", ((CmsSearchResult)results.get(0)).getPath());
    }
    
    /**
     * Test the cms search indexer.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsSearchIndexer() throws Throwable {

        I_CmsReport report = new CmsShellReport();
        OpenCms.getSearchManager().updateIndex(report);
    }

    /**
     * Tests the cms search with a larger result set.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsSearchLargeResult() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing search with large result set");

        // create test folder
        cms.createResource("/test/", CmsResourceTypeFolder.C_RESOURCE_TYPE_ID, null, null);
        cms.unlockResource("/test/");

        // create master resource
        importTestResource(cms, 
            "org/opencms/search/pdf-test-112.pdf", "/test/master.pdf", 
            CmsResourceTypeBinary.getStaticTypeId(), 
            Collections.EMPTY_LIST);

        // create a copy
        cms.copyResource("/test/master.pdf", "/test/copy.pdf");
        cms.chacc("/test/copy.pdf", "group", "Users", "-r");

        // create siblings
        for (int i = 0; i < 100; i++) {
            cms.createSibling("/test/master.pdf", "/test/sibling" + i + ".pdf", null);
        }

        // publish the project and update the search index
        I_CmsReport report = new CmsShellReport();
        OpenCms.getSearchManager().updateIndex(INDEX_OFFLINE, report);

        // search for "pdf"
        CmsSearch cmsSearchBean = new CmsSearch();
        cmsSearchBean.init(cms);
        cmsSearchBean.setIndex(INDEX_OFFLINE);
        List results;

        cms.addUser("test", "test", "Users", "", null);
        cms.loginUser("test", "test");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        cmsSearchBean.setQuery("pdf");

        echo("With Permission check, with excerpt");
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(
            CmsSearchIndex.C_PERMISSIONS,
            "true");
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(CmsSearchIndex.C_EXCERPT, "true");

        cmsSearchBean.setPage(1);
        long duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search1: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        for (Iterator i = results.iterator(); i.hasNext();) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            echo(res.getPath() + res.getExcerpt());
        }

        cmsSearchBean.setPage(2);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search2: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        for (Iterator i = results.iterator(); i.hasNext();) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            echo(res.getPath() + res.getExcerpt());
        }

        echo("With Permission check, without excerpt");
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(
            CmsSearchIndex.C_PERMISSIONS,
            "true");
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(CmsSearchIndex.C_EXCERPT, "false");

        cmsSearchBean.setPage(1);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search1: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        cmsSearchBean.setPage(2);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search2: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        echo("Without Permission check, with excerpt");
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(
            CmsSearchIndex.C_PERMISSIONS,
            "false");
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(CmsSearchIndex.C_EXCERPT, "true");

        cmsSearchBean.setPage(1);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search1: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        cmsSearchBean.setPage(2);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search2: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        echo("Without Permission check, without excerpt");
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(
            CmsSearchIndex.C_PERMISSIONS,
            "false");
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(CmsSearchIndex.C_EXCERPT, "false");

        cmsSearchBean.setPage(1);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search1: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        cmsSearchBean.setPage(2);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search2: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");
    }

    /**
     * Test the cms search indexer.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsSearchXmlContent() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing search for xml contents");

        CmsSearch cmsSearchBean = new CmsSearch();
        cmsSearchBean.init(cms);
        cmsSearchBean.setIndex(INDEX_OFFLINE);
        List results;

        cmsSearchBean.setQuery(">>SearchEgg1<<");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", ((CmsSearchResult)results.get(0)).getPath());

        cmsSearchBean.setQuery(">>SearchEgg2<<");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0002.html", ((CmsSearchResult)results.get(0)).getPath());

        cmsSearchBean.setQuery(">>SearchEgg3<<");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0003.html", ((CmsSearchResult)results.get(0)).getPath());
    }
    
    /**
     * Tests index generation with different analyzers.<p>
     * 
     * This test was added in order to verify proper generation of resource "root path" information
     * in the index.
     * 
     * @throws Throwable if something goes wrong
     */
    public void testIndexGeneration() throws Throwable {

        CmsSearchIndex searchIndex = new CmsSearchIndex();
        searchIndex.setName(INDEX_TEST);
        searchIndex.setProjectName("Offline");
        // important: use german locale for a special treat on term analyzing
        searchIndex.setLocale(Locale.GERMAN.toString());
        searchIndex.setRebuildMode(CmsSearchIndex.C_AUTO_REBUILD);
        // available pre-configured in the test configuration files opencms-search.xml
        searchIndex.addSourceName("source1");

        // initialize the new index
        searchIndex.initialize();
        
        // add the search index to the manager
        OpenCms.getSearchManager().addSearchIndex(searchIndex);
        
        I_CmsReport report = new CmsShellReport();
        OpenCms.getSearchManager().updateIndex(report);
        
        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List searchResult;

        searchBean.init(getCmsObject());
        searchBean.setIndex(INDEX_TEST);        
        searchBean.setQuery(">>SearchEgg1<<");
        
        // assert one file is found in the default site     
        searchResult = searchBean.getSearchResult();
        assertEquals(1, searchResult.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", ((CmsSearchResult)searchResult.get(0)).getPath()); 

        // change seach root and assert no more files are found
        searchBean.setSearchRoot("/folder1/");        
        searchResult = searchBean.getSearchResult();
        assertEquals(0, searchResult.size());
    }
}