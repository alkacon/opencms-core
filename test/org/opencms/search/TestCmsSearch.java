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

package org.opencms.search;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the cms search indexer.<p>
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
     * Prints the given list of search results to STDOUT.<p>
     *
     * @param searchResult the list to print
     * @param cms the current OpenCms user context
     */
    public static void printResults(List<CmsSearchResult> searchResult, CmsObject cms) {

        printResults(searchResult, cms, false);
    }

    /**
     * Prints the given list of search results to STDOUT.<p>
     *
     * @param searchResult the list to print
     * @param cms the current OpenCms user context
     * @param showExcerpt if <code>true</code>, the generated excerpt is also displayed
     */
    public static void printResults(List<CmsSearchResult> searchResult, CmsObject cms, boolean showExcerpt) {

        Iterator<CmsSearchResult> i = searchResult.iterator();
        int count = 0;
        int colPath = 0;
        int colTitle = 0;
        while (i.hasNext()) {
            CmsSearchResult res = i.next();
            String path = cms.getRequestContext().removeSiteRoot(res.getPath());
            colPath = Math.max(colPath, path.length() + 3);
            String title = res.getField(CmsSearchField.FIELD_TITLE);
            if (title == null) {
                title = "";
            } else {
                title = title.trim();
            }
            colTitle = Math.max(colTitle, title.length() + 3);
        }
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = i.next();
            count++;
            System.out.print(CmsStringUtil.padRight("" + count, 4));
            System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getPath()), colPath));
            String title = res.getField(CmsSearchField.FIELD_TITLE);
            if (title == null) {
                title = "";
            } else {
                title = title.trim();
            }
            System.out.print(CmsStringUtil.padRight(title, colTitle));
            String type = res.getDocumentType();
            if (type == null) {
                type = "";
            }
            System.out.print(CmsStringUtil.padRight(type, 10));
            if (res.getDateLastModified() != null) {
                System.out.print(
                    CmsStringUtil.padRight(
                        "" + CmsDateUtil.getDateTime(res.getDateLastModified(), DateFormat.SHORT, Locale.GERMAN),
                        17));
            }
            System.out.println("score: " + res.getScore());
            if (showExcerpt) {
                System.out.println(res.getExcerpt());
            }
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
        suite.addTest(new TestCmsSearch("testQueryEncoding"));
        suite.addTest(new TestCmsSearch("testSearchIssueWithSpecialFoldernames"));
        suite.addTest(new TestCmsSearch("testShutdownWhileIndexing"));
        suite.addTest(new TestCmsSearch("testHasAnalyzerForAll"));

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

        CmsSearch cmsSearchBean = new CmsSearch();
        cmsSearchBean.init(cms);
        cmsSearchBean.setIndex(INDEX_OFFLINE);
        cmsSearchBean.setSearchRoot("/types/");
        List<CmsSearchResult> results;

        cmsSearchBean.setQuery("+Alkacon +OpenCms +Text");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        assertEquals("/sites/default/types/text.txt", (results.get(0)).getPath());
    }

    /**
     * Test the cms search indexer.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCmsSearchIndexer() throws Throwable {

        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        // this call does not throws the rebuild index event
        OpenCms.getSearchManager().rebuildAllIndexes(report);
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
        OpenCms.getSearchManager().rebuildIndex(INDEX_OFFLINE, report);

        // search for "pdf"
        CmsSearch cmsSearchBean = new CmsSearch();
        cmsSearchBean.init(cms);
        cmsSearchBean.setIndex(INDEX_OFFLINE);
        List<CmsSearchResult> results;

        cms.createUser("test", "test", "", null);
        cms.loginUser("test", "test");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        cmsSearchBean.setQuery("pdf");

        echo("With Permission check, with excerpt");
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(
            CmsSearchIndex.PERMISSIONS,
            CmsStringUtil.TRUE);
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(
            CmsSearchIndex.EXCERPT,
            CmsStringUtil.TRUE);

        cmsSearchBean.setSearchPage(1);
        long duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo(
            "Search1: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        for (Iterator<CmsSearchResult> i = results.iterator(); i.hasNext();) {
            CmsSearchResult res = i.next();
            echo(res.getPath() + res.getExcerpt());
        }

        cmsSearchBean.setSearchPage(2);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo(
            "Search2: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        for (Iterator<CmsSearchResult> i = results.iterator(); i.hasNext();) {
            CmsSearchResult res = i.next();
            echo(res.getPath() + res.getExcerpt());
        }

        echo("With Permission check, without excerpt");
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(
            CmsSearchIndex.PERMISSIONS,
            CmsStringUtil.TRUE);
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(
            CmsSearchIndex.EXCERPT,
            CmsStringUtil.FALSE);

        cmsSearchBean.setSearchPage(1);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo(
            "Search1: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        cmsSearchBean.setSearchPage(2);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo(
            "Search2: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        echo("Without Permission check, with excerpt");
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(
            CmsSearchIndex.PERMISSIONS,
            CmsStringUtil.FALSE);
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(
            CmsSearchIndex.EXCERPT,
            CmsStringUtil.TRUE);

        cmsSearchBean.setSearchPage(1);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo(
            "Search1: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        cmsSearchBean.setSearchPage(2);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo(
            "Search2: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        echo("Without Permission check, without excerpt");
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(
            CmsSearchIndex.PERMISSIONS,
            CmsStringUtil.FALSE);
        OpenCms.getSearchManager().getIndex(INDEX_OFFLINE).addConfigurationParameter(
            CmsSearchIndex.EXCERPT,
            CmsStringUtil.FALSE);

        cmsSearchBean.setSearchPage(1);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo(
            "Search1: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");

        cmsSearchBean.setSearchPage(2);
        duration = -System.currentTimeMillis();
        results = cmsSearchBean.getSearchResult();
        duration += System.currentTimeMillis();
        echo(
            "Search2: " + cmsSearchBean.getSearchResultCount() + " results found, total duration: " + duration + " ms");
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

        CmsSearch cmsSearchBean = new CmsSearch();
        cmsSearchBean.init(cms);
        cmsSearchBean.setIndex(INDEX_OFFLINE);
        List<CmsSearchResult> results;

        cmsSearchBean.setQuery(">>SearchEgg1<<");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", results.get(0).getPath());

        cmsSearchBean.setQuery(">>SearchEgg2<<");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0002.html", results.get(0).getPath());

        cmsSearchBean.setQuery(">>SearchEgg3<<");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0003.html", results.get(0).getPath());

        // check (on console) that the file does contain a link to the /xmlcontent/ folder
        CmsFile article4 = cms.readFile("/xmlcontent/article_0004.html");
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, article4, true);
        echo(content.toString());

        // now search for another Query "xmlcontent", this must not be found in article 4 since it is excluded
        cmsSearchBean.setQuery("xmlcontent");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0003.html", (results.get(0)).getPath());
        // assertEquals("/sites/default/xmlcontent/article_0004.html", ((CmsSearchResult)results.get(1)).getPath());
    }

    /**
     * Check if we have an analyzer entry for the pseudo-locale 'all'.
     *
     * @throws Exception if something goes wrong
     */
    public void testHasAnalyzerForAll() throws Exception {

        OpenCms.getSearchManager().getAnalyzer(new Locale("all"));
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

        CmsSearchIndex searchIndex = new CmsSearchIndex(INDEX_TEST);
        searchIndex.setProject("Offline");
        // important: use german locale for a special treat on term analyzing
        searchIndex.setLocale(Locale.GERMAN);
        searchIndex.setRebuildMode(CmsSearchIndex.REBUILD_MODE_AUTO);
        // available pre-configured in the test configuration files opencms-search.xml
        searchIndex.addSourceName("source1");

        // initialize the new index
        searchIndex.initialize();

        // add the search index to the manager
        OpenCms.getSearchManager().addSearchIndex(searchIndex);

        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        // this call does not throws the rebuild index event
        OpenCms.getSearchManager().rebuildAllIndexes(report);

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        searchBean.init(getCmsObject());
        searchBean.setIndex(INDEX_TEST);
        searchBean.setQuery(">>SearchEgg1<<");

        // assert one file is found in the default site
        searchResult = searchBean.getSearchResult();
        assertEquals(1, searchResult.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", searchResult.get(0).getPath());

        // change seach root and assert no more files are found
        searchBean.setSearchRoot("/folder1/");
        searchResult = searchBean.getSearchResult();
        assertEquals(0, searchResult.size());
    }

    /**
     * Tests if <code>{@link CmsSearch#setQuery(String)}</code> modifies
     * the query in an undesireable way (changes url encoded Strings). <p>
     */
    public void testQueryEncoding() {

        // without encoding
        String query = "�lm�hlm�her";
        CmsSearch test = new CmsSearch();
        test.setQuery(query);
        assertEquals(query, test.getQuery());

        // with encoding
        query = CmsEncoder.encode(query, CmsEncoder.ENCODING_UTF_8);
        test.setQuery(query);
        assertEquals(query, test.getQuery());
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

        CmsSearch cmsSearchBean = new CmsSearch();
        cmsSearchBean.init(cms);
        cmsSearchBean.setIndex(INDEX_TEST);
        List<CmsSearchResult> results;

        cmsSearchBean.setSearchRoot("/");
        cmsSearchBean.setQuery("+Alkacon +OpenCms");
        results = cmsSearchBean.getSearchResult();
        TestCmsSearch.printResults(results, cms);
        assertEquals(8, results.size());
        assertEquals("/sites/default" + folderName + "text.txt", results.get(0).getPath());

        cmsSearchBean.setSearchRoot(folderName);
        cmsSearchBean.setQuery("+Alkacon +OpenCms");
        results = cmsSearchBean.getSearchResult();
        TestCmsSearch.printResults(results, cms);
        assertEquals(1, results.size());
        assertEquals("/sites/default" + folderName + "text.txt", results.get(0).getPath());
    }

    /**
     * Test the cms search indexer.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testShutdownWhileIndexing() throws Throwable {

        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        // this call does not throws the rebuild index event
        OpenCms.getSearchManager().rebuildIndex(INDEX_OFFLINE, report);
        OpenCms.getSearchManager().shutDown();
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
            OpenCms.getSearchManager().rebuildIndex(INDEX_OFFLINE, report);
        }

        // search for "pdf"
        CmsSearch cmsSearchBean = new CmsSearch();
        cmsSearchBean.init(cms);
        cmsSearchBean.setIndex(INDEX_OFFLINE);
        cmsSearchBean.setQuery("+Testfile +Struktur");

        if (folderName != null) {
            CmsSearchParameters parameters = cmsSearchBean.getParameters();
            parameters.setSearchRoots(folderName);
            cmsSearchBean.setParameters(parameters);
        }

        List<CmsSearchResult> results = cmsSearchBean.getSearchResult();
        printResults(results, cms);
        assertEquals(expected, results.size());
    }
}