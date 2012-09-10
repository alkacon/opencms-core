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
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.A_CmsSearchIndex;
import org.opencms.search.CmsLuceneIndex;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.fields.I_CmsSearchField;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.schema.DateField;

/**
 * Tests if Solr search queries are able to do what was earlier done with Lucene.<p>
 * 
 * @since 8.5.0
 */
public class TestSolrSearch extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestSolrSearch(String arg0) {

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
        suite.setName(TestSolrSearch.class.getName());

        suite.addTest(new TestSolrSearch("testCmsSearchIndexer"));
        suite.addTest(new TestSolrSearch("testCmsSearchUppercaseFolderName"));
        suite.addTest(new TestSolrSearch("testCmsSearchDocumentTypes"));
        suite.addTest(new TestSolrSearch("testCmsSearchXmlContent"));
        suite.addTest(new TestSolrSearch("testIndexGeneration"));
        suite.addTest(new TestSolrSearch("testSearchIssueWithSpecialFoldernames"));
        suite.addTest(new TestSolrSearch("testSortSearchResults"));
        suite.addTest(new TestSolrSearch("testMultipleSearchRoots"));
        suite.addTest(new TestSolrSearch("testLimitTimeRanges"));
        suite.addTest(new TestSolrSearch("testLimitTimeRangesOptimized"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
                // disable all lucene indexes
                for (String indexName : OpenCms.getSearchManager().getIndexNames()) {
                    if (!indexName.equalsIgnoreCase(AllSolrTests.SOLR_ONLINE)) {
                        A_CmsSearchIndex index = OpenCms.getSearchManager().getIndex(indexName);
                        if (index instanceof CmsLuceneIndex) {
                            index.setEnabled(false);
                        }
                    }
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
     * Tests searching in various document types.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsSearchDocumentTypes() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing search for various document types");

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllSolrTests.SOLR_ONLINE);
        CmsSolrQuery squery = new CmsSolrQuery();
        squery.setTexts("Alkacon", "OpenCms", "Text");
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

        OpenCms.getSearchManager().rebuildAllIndexes(new CmsShellReport(Locale.ENGLISH));
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

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllSolrTests.SOLR_ONLINE);
        // String query = "+text:Alkacon +text:OpenCms +text:Text +parent-folders:/sites/default/types/*";
        query = "q=+text:>>SearchEgg1<<";
        results = index.search(cms, query);
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", results.get(0).getRootPath());

        query = "q=+text:>>SearchEgg2<<";
        results = index.search(cms, query);
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0002.html", results.get(0).getRootPath());

        query = "q=+text:>>SearchEgg3<<";
        results = index.search(cms, query);
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0003.html", results.get(0).getRootPath());

        // check (on console) that the file does contain a link to the /xmlcontent/ folder 
        CmsFile article4 = cms.readFile("/xmlcontent/article_0004.html");
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, article4, true);
        echo(content.toString());

        // now search for another Query "xmlcontent", this must not be found in article 4 since it is excluded
        query = "q=+text:xmlcontent";
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

        CmsSolrIndex index = new CmsSolrIndex(AllSolrTests.INDEX_TEST);

        index.setProject("Offline");
        // important: use german locale for a special treat on term analyzing
        index.setLocale(Locale.GERMAN);
        index.setRebuildMode(A_CmsSearchIndex.REBUILD_MODE_AUTO);
        // available pre-configured in the test configuration files opencms-search.xml
        index.setFieldConfigurationName("solr_fields");
        index.addSourceName("solr_source2");

        // add the search index to the manager
        OpenCms.getSearchManager().addSearchIndex(index);

        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        // this call does not throws the rebuild index event
        OpenCms.getSearchManager().rebuildIndex(AllSolrTests.INDEX_TEST, report);
        // rebuildAllIndexes(report);

        // perform a search on the newly generated index
        String query;
        CmsSolrResultList results;

        query = "q=*:*";
        results = index.search(getCmsObject(), query);
        AllSolrTests.printResults(getCmsObject(), results, false);
        assertEquals(4, results.size());

        query = "q=+text:SearchEgg1";
        results = index.search(getCmsObject(), query);

        // assert one file is found in the default site     
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", results.get(0).getRootPath());

        // change seach root and assert no more files are found
        query = "q=+text:>>SearchEgg1<< +parent-folders:/folder1/";
        results = index.search(getCmsObject(), query);
        assertEquals(0, results.size());

        OpenCms.getSearchManager().removeSearchIndex(index);
    }

    /**
     * Tests searching with limiting the time ranges.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testLimitTimeRanges() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing searching with limiting to time ranges");

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllSolrTests.SOLR_ONLINE);
        String query = "?rows=50&q=+text:OpenCms";
        CmsSolrResultList results = index.search(getCmsObject(), query);
        int orgCount = results.size();

        // check min date created
        Date stamp = new Date();
        String date = DateField.formatExternal(stamp);
        query += " +created:[" + date + " TO NOW]";
        results = index.search(getCmsObject(), query);
        assertEquals(0, results.size());

        String resName = "search_new.txt";
        cms.createResource(resName, CmsResourceTypePlain.getStaticTypeId(), "OpenCms".getBytes(), null);
        I_CmsReport report = new CmsShellReport(cms.getRequestContext().getLocale());
        OpenCms.getPublishManager().publishProject(cms, report);
        OpenCms.getPublishManager().waitWhileRunning();

        results = index.search(getCmsObject(), query);
        assertEquals(1, results.size());

        // check max date created
        String maxDate = DateField.formatExternal(new Date(stamp.getTime() - 1000));
        query = "?rows=50&q=+text:OpenCms  +created:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount, results.size());

        query = "?rows=50&q=+text:OpenCms  +created:[* TO NOW]";
        results = index.search(getCmsObject(), query);
        AllSolrTests.printResults(cms, results, false);
        assertEquals(orgCount + 1, results.size());

        // check min date last modified
        stamp = new Date();
        date = DateField.formatExternal(stamp);
        query = "?rows=50&q=+text:OpenCms +lastmodified:[" + date + " TO NOW]";
        results = index.search(getCmsObject(), query);
        assertEquals(0, results.size());

        CmsFile file = cms.readFile(resName);
        file.setContents("OpenCms ist toll".getBytes());
        cms.lockResource(file);
        cms.writeFile(file);
        report = new CmsShellReport(cms.getRequestContext().getLocale());
        OpenCms.getPublishManager().publishProject(cms, report);
        OpenCms.getPublishManager().waitWhileRunning();

        results = index.search(getCmsObject(), query);
        assertEquals(1, results.size());

        // check max date last modified
        maxDate = DateField.formatExternal(new Date(stamp.getTime() - 1000));
        query = "?rows=50&q=+text:OpenCms +lastmodified:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount, results.size());

        query = "?rows=50&q=+text:OpenCms +lastmodified:[* TO NOW]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount + 1, results.size());
    }

    /**
     * Tests searching with optimized limiting the time ranges.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testLimitTimeRangesOptimized() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing searching with optimized limiting to time ranges");

        String query = "?rows=50&q=+text:OpenCms";
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllSolrTests.SOLR_ONLINE);
        List<CmsSearchResource> results = index.search(getCmsObject(), query);
        int orgCount = results.size();

        // check min date created
        Date stamp = new Date();
        String date = DateField.formatExternal(new Date(stamp.getTime() - 20000));
        query = "?rows=50&q=+text:OpenCms +created:[" + date + " TO NOW]";
        results = index.search(getCmsObject(), query);
        // we must find one match because of the previous time range test
        assertEquals(1, results.size());

        String resName = "search_new_2.txt";
        cms.createResource(resName, CmsResourceTypePlain.getStaticTypeId(), "OpenCms".getBytes(), null);
        I_CmsReport report = new CmsShellReport(cms.getRequestContext().getLocale());
        OpenCms.getPublishManager().publishProject(cms, report);
        OpenCms.getPublishManager().waitWhileRunning();

        results = index.search(getCmsObject(), query);
        // we must find one match because of the previous time range test
        assertEquals(2, results.size());

        // check max date created (must move back one day because of granularity level in optimized date range search)
        String maxDate = DateField.formatExternal(new Date(stamp.getTime() - (1000 * 60 * 60 * 24)));
        query = "?rows=50&q=+text:OpenCms +created:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        // we will find one result less then before because of the previous date range rest
        assertEquals(orgCount - 1, results.size());

        maxDate = DateField.formatExternal(new Date());
        query = "?rows=50&q=+text:OpenCms +created:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount + 1, results.size());

        // check min date last modified
        Date newStamp = new Date();
        date = DateField.formatExternal(newStamp);

        // move to tomorrow because of granularity level in optimized date search
        String minDate = DateField.formatExternal(new Date(newStamp.getTime() + (1000 * 60 * 60 * 24)));
        query = "?rows=50&q=+text:OpenCms +lastmodified:[ " + minDate + " TO NOW]";
        results = index.search(getCmsObject(), query);
        assertEquals(0, results.size());

        CmsFile file = cms.readFile(resName);
        file.setContents("OpenCms ist toll".getBytes());
        cms.lockResource(file);
        cms.writeFile(file);
        report = new CmsShellReport(cms.getRequestContext().getLocale());
        OpenCms.getPublishManager().publishProject(cms, report);
        OpenCms.getPublishManager().waitWhileRunning();

        query = "?rows=50&q=+text:OpenCms +lastmodified:[" + date + " TO NOW]";
        results = index.search(getCmsObject(), query);
        // TODO This test finds two results for Lucene, but it should only be one
        // TODO This variant is correct for Solr
        assertEquals(1, results.size());

        maxDate = DateField.formatExternal(new Date(newStamp.getTime() - (1000 * 60 * 60 * 24)));
        query = "?rows=50&q=+text:OpenCms +lastmodified:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount - 1, results.size());

        query = "?rows=50&q=+text:OpenCms +lastmodified:[* TO NOW]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount + 1, results.size());
    }

    /**
     * Tests searching with multiple search roots.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testMultipleSearchRoots() throws Exception {

        echo("Testing searching with multiple search roots");

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllSolrTests.SOLR_ONLINE);

        String[][] roots = new String[][] {
            new String[] {"/sites/default/folder1/"},
            new String[] {"/sites/default/folder2/"},
            new String[] {"/sites/default/types/"},
            new String[] {"/sites/default/folder2/", "/sites/default/types/"},
            new String[] {"/sites/default/folder1/", "/sites/default/types/"},
            new String[] {"/sites/default/folder1/", "/sites/default/folder2/"},
            new String[] {"/sites/default/folder1/", "/sites/default/folder2/", "/sites/default/types/"}};

        int[] expected = new int[] {7, 4, 1, 5, 8, 11, 12};

        for (int i = 0; i < expected.length; i++) {
            String query = "?rows=50&q=+text:OpenCms AND (";
            int expect = expected[i];
            String[] rootList = roots[i];
            for (int j = 0; j < rootList.length; j++) {
                query += "parent-folders:" + rootList[j];
                if (rootList.length > (j + 1)) {
                    query += " OR ";
                }
            }
            query += ")";

            CmsSolrResultList results = index.search(getCmsObject(), query);
            System.err.println(query);
            System.out.println("Result for search " + i + " (found " + results.size() + ", expected " + expect + ")");
            AllSolrTests.printResults(getCmsObject(), results, false);
            assertEquals(expect, results.size());
        }
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
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllSolrTests.SOLR_ONLINE);

        // perform a search on the newly generated index
        String query;
        CmsSolrResultList results;

        query = "q=+text:Alkacon +text:OpenCms +parent-folders:/sites/default/&sort=path asc";
        results = index.search(getCmsObject(), query);
        AllSolrTests.printResults(cms, results, false);
        assertEquals(8, results.size());
        assertEquals("/sites/default" + folderName + "text.txt", results.get(0).getRootPath());

        query = "q=+text:Alkacon +text:OpenCms +parent-folders:/sites/default" + folderName;
        results = index.search(getCmsObject(), query);
        AllSolrTests.printResults(cms, results, false);
        assertEquals(1, results.size());
        assertEquals("/sites/default" + folderName + "text.txt", results.get(0).getRootPath());
    }

    /**
     * Tests sorting of search results.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSortSearchResults() throws Exception {

        echo("Testing sorting of search results");
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllSolrTests.SOLR_ONLINE);

        String query = "q=content_en:opencms meta_en:opencms";
        CmsSolrResultList results = index.search(getCmsObject(), query);
        System.out.println("Result sorted by relevance:");
        AllSolrTests.printResults(getCmsObject(), results, false);

        // first run is default sort order
        float maxScore = results.getMaxScore().floatValue();
        int score = results.get(0).getScore(maxScore);
        assertTrue("Best match by score must always be 100 but is " + score, score == 100);
        for (int i = 1; i < results.size(); i++) {
            assertTrue("Resource "
                + results.get(i - 1).getRootPath()
                + " not sorted as expected - index ["
                + (i - 1)
                + "/"
                + i
                + "]", results.get(i - 1).getScore(maxScore) >= results.get(i).getScore(maxScore));
        }

        // second run use Title sort order
        String lastTitle = null;
        CmsSolrQuery q = new CmsSolrQuery(null, query);
        q.addSortField("title-key", ORDER.asc);
        q.addSortField("score", ORDER.asc);
        results = index.search(getCmsObject(), q.getQuery());
        System.out.println("Result sorted by title:");
        AllSolrTests.printResults(getCmsObject(), results, false);
        Iterator<CmsSearchResource> i = results.iterator();
        while (i.hasNext()) {
            CmsSearchResource res = i.next();
            if (lastTitle != null) {
                // make sure result is sorted correctly
                assertTrue(lastTitle.compareTo(res.getField(I_CmsSearchField.FIELD_TITLE)) <= 0);
            }
            lastTitle = res.getField(I_CmsSearchField.FIELD_TITLE);
        }

        // third run use date last modified
        long lastTime = 0;
        q = new CmsSolrQuery(null, query);
        q.setQueryType("dismax");
        q.addField("*,score");
        q.setRows(new Integer(100));
        q.addSortField("lastmodified", ORDER.desc);
        results = index.search(getCmsObject(), q);
        System.out.println("Result sorted by date last modified:");
        AllSolrTests.printResults(getCmsObject(), results, false);
        i = results.iterator();
        while (i.hasNext()) {
            CmsSearchResource res = i.next();
            if (lastTime > 0) {
                // make sure result is sorted correctly
                assertTrue(lastTime >= res.getDateLastModified());
                assertTrue(res.getScore(results.getMaxScore().floatValue()) <= 100);
            }
            lastTime = res.getDateLastModified();
        }
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
            OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(cms.getRequestContext().getLocale()));
            OpenCms.getPublishManager().waitWhileRunning();
        }

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllSolrTests.SOLR_ONLINE);
        CmsSolrQuery query = new CmsSolrQuery();
        query.setTexts("Testfile", "Struktur");
        if (folderName != null) {
            query.setSearchRoots(cms.getRequestContext().addSiteRoot(folderName));
        }
        CmsSolrResultList results = index.search(cms, query);
        AllSolrTests.printResults(cms, results, false);

        assertEquals(expected, results.size());
    }
}