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

package org.opencms.search.solr;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.A_CmsSearchIndex;
import org.opencms.search.CmsLuceneIndex;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.fields.I_CmsSearchField;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * Unit test for the cms search indexer.<p>
 */
public class TestCmsSearch extends OpenCmsTestCase {

    /** Name of the search index created using API. */
    public static final String INDEX_TEST = "Test new index";

    /** Name of the index used for testing. */
    public static final String SOLR_OFFLINE = "Solr Offline";

    /** Name of the index used for testing. */
    public static final String SOLR_ONLINE = "Solr Online";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSearch(String arg0) {

        super(arg0);
    }

    /**
     * Prints a Solr query response.<p>
     * 
     * @param qr the query response
     */
    public static void printResultCount(QueryResponse qr) {

        System.out.println("——————————–");
        SolrDocumentList sdl = qr.getResults();
        // System.out.println(sdl.toString());
        System.out.println("Found: " + sdl.getNumFound());
        System.out.println("Start: " + sdl.getStart());
        System.out.println("Max Score: " + sdl.getMaxScore());
        System.out.println("——————————–");
    }

    /**
     * Prints a Solr query response.<p>
     * 
     * @param qr the query response
     */
    public static void printResultDetails(QueryResponse qr) {

        printResultCount(qr);
        SolrDocumentList sdl = qr.getResults();
        qr.getExplainMap();

        // System.out.println(sdl.toString());

        ArrayList<HashMap<String, Object>> hitsOnPage = new ArrayList<HashMap<String, Object>>();
        for (SolrDocument d : sdl) {
            HashMap<String, Object> values = new HashMap<String, Object>();
            Iterator<Map.Entry<String, Object>> i = d.iterator();
            while (i.hasNext()) {
                Map.Entry<String, Object> e2 = i.next();
                values.put(e2.getKey(), e2.getValue());
            }

            hitsOnPage.add(values);
            System.out.println(values.get("path") + " (" + values.get("Title") + ")");
        }
        List<FacetField> facets = qr.getFacetFields();

        if (facets != null) {
            for (FacetField facet : facets) {
                List<FacetField.Count> facetEntries = facet.getValues();

                if (facetEntries != null) {
                    for (FacetField.Count fcount : facetEntries) {
                        System.out.println(fcount.getName() + ": " + fcount.getCount());
                    }
                }
            }
        }
    }

    /**
     * Prints the result.<p>
     * 
     * @param qr the Solr query response
     * @param results the results to print
     * @param cms the cms object
     */
    public static void printResults(QueryResponse qr, List<CmsSearchResource> results, CmsObject cms) {

        Iterator<CmsSearchResource> i = results.iterator();
        int count = 0;
        int colPath = 0;
        int colTitle = 0;
        while (i.hasNext()) {
            CmsSearchResource res = i.next();
            String path = cms.getRequestContext().removeSiteRoot(res.getRootPath());
            colPath = Math.max(colPath, path.length() + 3);
            String title = res.getField(I_CmsSearchField.FIELD_TITLE
                + "_"
                + cms.getRequestContext().getLocale().toString());
            if (title == null) {
                title = "";
            } else {
                title = title.trim();
            }
            colTitle = Math.max(colTitle, title.length() + 3);
        }

        for (CmsSearchResource res : results) {
            System.out.print(CmsStringUtil.padRight("" + ++count, 4));
            System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getRootPath()), colPath));
            String title = res.getField(I_CmsSearchField.FIELD_TITLE
                + "_"
                + cms.getRequestContext().getLocale().toString());
            if (title == null) {
                title = "";
            } else {
                title = title.trim();
            }
            System.out.print(CmsStringUtil.padRight(title, colTitle));
            String type = res.getField(I_CmsSearchField.FIELD_TYPE);
            if (type == null) {
                type = "";
            }
            System.out.print(CmsStringUtil.padRight(type, 10));
            System.out.print(CmsStringUtil.padRight(
                "" + CmsDateUtil.getDateTime(new Date(res.getDateLastModified()), DateFormat.SHORT, Locale.GERMAN),
                17));

            System.out.println("score: " + res.getScore(qr.getResults().getMaxScore().floatValue()));
        }
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
        suite.addTest(new TestCmsSearch("testCmsSearchUppercaseFolderName"));
        suite.addTest(new TestCmsSearch("testCmsSearchDocumentTypes"));
        suite.addTest(new TestCmsSearch("testCmsSearchXmlContent"));
        suite.addTest(new TestCmsSearch("testIndexGeneration"));
        suite.addTest(new TestCmsSearch("testSearchIssueWithSpecialFoldernames"));

        // This test is intended only for performance/resource monitoring
        // suite.addTest(new TestCmsSearch("testCmsSearchLargeResult"));

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
     * Tests searching in various document types.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsSearchDocumentTypes() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing search for various document types");

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(SOLR_OFFLINE);
        CmsSolrQuery squery = new CmsSolrQuery(cms);
        squery.setTexts(new String[] {"Alkacon", "OpenCms", "Text"});
        squery.setSearchRoots("/sites/default/types/");
        List<CmsSearchResource> results = index.search(cms, squery);

        assertEquals(1, results.size());
        assertEquals("/sites/default/types/text.txt", (results.get(0)).getRootPath());
    }

    /**
     * Test the cms search indexer.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsSearchIndexer() throws Throwable {

        for (String indexName : OpenCms.getSearchManager().getIndexNames()) {
            if (!indexName.equalsIgnoreCase(SOLR_OFFLINE)) {
                A_CmsSearchIndex index = OpenCms.getSearchManager().getIndex(indexName);
                if (index instanceof CmsLuceneIndex) {
                    OpenCms.getSearchManager().removeSearchIndex(index);
                }
            }
        }
        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        OpenCms.getSearchManager().rebuildIndex(SOLR_OFFLINE, report);
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
        cms.createResource("/test/", CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, null);
        cms.unlockResource("/test/");

        // create master resource
        importTestResource(
            cms,
            "org/opencms/search/pdf-test-112.pdf",
            "/test/master.pdf",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());

        // create a copy
        cms.copyResource("/test/master.pdf", "/test/copy.pdf");
        cms.chacc("/test/copy.pdf", "group", "Users", "-r");

        // create siblings
        for (int i = 0; i < 100; i++) {
            cms.createSibling("/test/master.pdf", "/test/sibling" + i + ".pdf", null);
        }

        // publish the project and update the search index
        I_CmsReport report = new CmsShellReport(cms.getRequestContext().getLocale());
        OpenCms.getSearchManager().rebuildIndex(SOLR_OFFLINE, report);

        // search for "pdf"
        CmsSearch cmsSearchBean = new CmsSearch();
        cmsSearchBean.init(cms);
        cmsSearchBean.setIndex(SOLR_OFFLINE);
        List<CmsSearchResult> results;

        cms.createUser("test", "test", "", null);
        cms.loginUser("test", "test");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        cmsSearchBean.setQuery("pdf");

        echo("With Permission check, with excerpt");
        OpenCms.getSearchManager().getIndex(SOLR_OFFLINE).addConfigurationParameter(
            A_CmsSearchIndex.PERMISSIONS,
            CmsStringUtil.TRUE);
        OpenCms.getSearchManager().getIndex(SOLR_OFFLINE).addConfigurationParameter(
            CmsLuceneIndex.EXCERPT,
            CmsStringUtil.TRUE);

        cmsSearchBean.setSearchPage(1);
        long duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search1: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        for (Iterator<CmsSearchResult> i = results.iterator(); i.hasNext();) {
            CmsSearchResult res = i.next();
            echo(res.getPath() + res.getExcerpt());
        }

        cmsSearchBean.setSearchPage(2);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search2: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        for (Iterator<CmsSearchResult> i = results.iterator(); i.hasNext();) {
            CmsSearchResult res = i.next();
            echo(res.getPath() + res.getExcerpt());
        }

        echo("With Permission check, without excerpt");
        OpenCms.getSearchManager().getIndex(SOLR_OFFLINE).addConfigurationParameter(
            A_CmsSearchIndex.PERMISSIONS,
            CmsStringUtil.TRUE);
        OpenCms.getSearchManager().getIndex(SOLR_OFFLINE).addConfigurationParameter(
            CmsLuceneIndex.EXCERPT,
            CmsStringUtil.FALSE);

        cmsSearchBean.setSearchPage(1);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search1: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        cmsSearchBean.setSearchPage(2);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search2: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        echo("Without Permission check, with excerpt");
        OpenCms.getSearchManager().getIndex(SOLR_OFFLINE).addConfigurationParameter(
            A_CmsSearchIndex.PERMISSIONS,
            CmsStringUtil.FALSE);
        OpenCms.getSearchManager().getIndex(SOLR_OFFLINE).addConfigurationParameter(
            CmsLuceneIndex.EXCERPT,
            CmsStringUtil.TRUE);

        cmsSearchBean.setSearchPage(1);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search1: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        cmsSearchBean.setSearchPage(2);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search2: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        echo("Without Permission check, without excerpt");
        OpenCms.getSearchManager().getIndex(SOLR_OFFLINE).addConfigurationParameter(
            A_CmsSearchIndex.PERMISSIONS,
            CmsStringUtil.FALSE);
        OpenCms.getSearchManager().getIndex(SOLR_OFFLINE).addConfigurationParameter(
            CmsLuceneIndex.EXCERPT,
            CmsStringUtil.FALSE);

        cmsSearchBean.setSearchPage(1);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search1: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        cmsSearchBean.setSearchPage(2);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo("Search2: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");
    }

    /**
     * Tests the CmsSearch with folder names with upper case letters.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testCmsSearchUppercaseFolderName() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing search for case sensitive folder names");

        echo("Testing search for case sensitive folder name: /testUPPERCASE/");
        testCmsSearchUppercaseFolderNameUtil(cms, "/testUPPERCASE/", 1);

        // extension of this test for 7.0.2:
        // now it is possible to search in restricted folders in a case sensitive way
        echo("Testing search for case sensitive folder name: /TESTuppercase/");
        testCmsSearchUppercaseFolderNameUtil(cms, "/TESTuppercase/", 1);

        // let's see if we find 2 results when we don't use a search root
        echo("Testing search for case sensitive folder names without a site root");
        testCmsSearchUppercaseFolderNameUtil(cms, null, 2);
    }

    /**
     * Test the cms search indexer.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsSearchXmlContent() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing search for xml contents");

        String query;
        List<CmsSearchResource> results;

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(SOLR_OFFLINE);
        // String query = "+text_en:Alkacon +text_en:OpenCms +text_en:Text +parent-folders:/sites/default/types/*";
        query = "q=+text_en:>>SearchEgg1<<";
        results = index.search(cms, query);
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", results.get(0).getRootPath());

        query = "q=+text_en:>>SearchEgg2<<";
        results = index.search(cms, query);
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0002.html", results.get(0).getRootPath());

        query = "q=+text_en:>>SearchEgg3<<";
        results = index.search(cms, query);
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0003.html", results.get(0).getRootPath());

        // check (on console) that the file does contain a link to the /xmlcontent/ folder 
        CmsFile article4 = cms.readFile("/xmlcontent/article_0004.html");
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, article4, true);
        echo(content.toString());

        // now search for another Query "xmlcontent", this must not be found in article 4 since it is excluded
        query = "q=+text_en:xmlcontent";
        results = index.search(cms, query);
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0003.html", (results.get(0)).getRootPath());
        // assertEquals("/sites/default/xmlcontent/article_0004.html", ((CmsSearchResult)results.get(1)).getPath());
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

        CmsSolrIndex index = new CmsSolrIndex(INDEX_TEST);

        index.setProjectName("Offline");
        // important: use german locale for a special treat on term analyzing
        index.setLocale(Locale.GERMAN);
        index.setRebuildMode(A_CmsSearchIndex.REBUILD_MODE_AUTO);
        // available pre-configured in the test configuration files opencms-search.xml
        index.setFieldConfigurationName("solr_fields");
        index.addSourceName("source1");

        // initialize the new index
        index.initialize();

        // add the search index to the manager
        OpenCms.getSearchManager().addSearchIndex(index);

        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        // this call does not throws the rebuild index event
        OpenCms.getSearchManager().rebuildAllIndexes(report);

        // perform a search on the newly generated index
        String query;
        List<CmsSearchResource> results;

        query = "q=+text_en:SearchEgg1";
        results = index.search(getCmsObject(), query);

        // assert one file is found in the default site     
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", results.get(0).getRootPath());

        // change seach root and assert no more files are found
        query = "q=+text_en:>>SearchEgg1<< +parent-folders:/folder1/";
        results = index.search(getCmsObject(), query);
        assertEquals(0, results.size());
    }

    /**
     * Tests an issue where no results are found in folders that have names
     * like <code>/basisdienstleistungen_-_zka/</code>.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSearchIssueWithSpecialFoldernames() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing search issue with special folder name");

        String folderName = "/basisdienstleistungen_-_zka/";
        cms.copyResource("/types/", folderName);
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        OpenCms.getSearchManager().getSearchIndexes();
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(SOLR_OFFLINE);

        // perform a search on the newly generated index
        String query;
        CmsSolrResultList results;

        query = "q=+text_en:Alkacon +text_en:OpenCms +parent-folders:/sites/default/&sort=path asc";
        results = index.search(getCmsObject(), query);
        printResultCount(results.getQueryResponse());
        assertEquals(8, results.size());
        assertEquals("/sites/default" + folderName + "text.txt", results.get(0).getRootPath());

        query = "q=+text_en:Alkacon +text_en:OpenCms +parent-folders:/sites/default" + folderName;
        results = index.search(getCmsObject(), query);
        printResultCount(results.getQueryResponse());
        assertEquals(1, results.size());
        assertEquals("/sites/default" + folderName + "text.txt", results.get(0).getRootPath());
    }

    /**
     * Internal helper for test with same name.<p>
     * 
     * @param cms the current users OpenCms context
     * @param folderName the folder name to perform the test in
     * @param expected the expected result size of the search
     * 
     * @throws Exception in case the test fails
     */
    private void testCmsSearchUppercaseFolderNameUtil(CmsObject cms, String folderName, int expected) throws Exception {

        if (folderName != null) {
            // create test folder
            cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, null);
            cms.unlockResource(folderName);

            // create master resource
            importTestResource(
                cms,
                "org/opencms/search/pdf-test-112.pdf",
                folderName + "master.pdf",
                CmsResourceTypeBinary.getStaticTypeId(),
                Collections.<CmsProperty> emptyList());

            // publish the project and update the search index
            I_CmsReport report = new CmsShellReport(cms.getRequestContext().getLocale());
            OpenCms.getSearchManager().rebuildIndex(SOLR_OFFLINE, report);
        }

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(SOLR_OFFLINE);
        String query = "q=+text_en:Testfile +text_en:Struktur";
        if (folderName != null) {
            query += " +parent-folders:" + cms.getRequestContext().addSiteRoot(folderName);
        }
        CmsSolrResultList results = index.search(cms, query);

        printResultCount(results.getQueryResponse());
        printResults(results.getQueryResponse(), results, cms);
        assertEquals(expected, results.size());
    }
}