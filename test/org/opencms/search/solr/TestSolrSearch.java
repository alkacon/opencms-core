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

package org.opencms.search.solr;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.CmsSearchUtil;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsRequestUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

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

        suite.addTest(new TestSolrSearch("testDocumentTypes"));
        suite.addTest(new TestSolrSearch("testFolderName"));
        suite.addTest(new TestSolrSearch("testIndexer"));
        suite.addTest(new TestSolrSearch("testIndexGeneration"));
        suite.addTest(new TestSolrSearch("testIssueWithSpecialFoldernames"));
        suite.addTest(new TestSolrSearch("testLimitTimeRanges"));
        suite.addTest(new TestSolrSearch("testLimitTimeRangesOptimized"));
        suite.addTest(new TestSolrSearch("testLocaleRestriction"));
        suite.addTest(new TestSolrSearch("testMultipleSearchRoots"));
        suite.addTest(new TestSolrSearch("testQueryDefaults"));
        suite.addTest(new TestSolrSearch("testQueryParameterStrength"));
        suite.addTest(new TestSolrSearch("testSortResults"));
        suite.addTest(new TestSolrSearch("testXmlContent"));
        suite.addTest(new TestSolrSearch("testAdvancedFacetting"));
        suite.addTest(new TestSolrSearch("testAdvancedHighlighting"));
        suite.addTest(new TestSolrSearch("testPermissionFilterAndLimits"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/", "/../org/opencms/search/solr");
                // disable all lucene indexes
                for (String indexName : OpenCms.getSearchManager().getIndexNames()) {
                    if (!indexName.equalsIgnoreCase(AllTests.SOLR_ONLINE)) {
                        I_CmsSearchIndex index = OpenCms.getSearchManager().getIndex(indexName);
                        if (index != null) {
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
     * @throws Throwable if something goes wrong
     */
    public void testAdvancedFacetting() throws Throwable {

        echo("Testing facet query count");

        // creating the query: facet=true&facet.field=Title_exact&facet.mincount=1&facet.query=text:OpenCms&rows=0
        SolrQuery query = new CmsSolrQuery(getCmsObject(), null);
        // facet=true
        query.setFacet(true);
        // facet.field=Title_exact
        query.addFacetField("Title_exact");
        // facet.mincount=1
        query.add("facet.mincount", "1");
        // facet.query=text:OpenCms
        query.addFacetQuery("text:OpenCms");
        // facet.query=Title_prop:OpenCms
        query.addFacetQuery("Title_prop:OpenCms");
        // rows=0
        query.setRows(Integer.valueOf(0));

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        CmsSolrResultList results = index.search(getCmsObject(), query);
        long facetTextCount = results.getFacetQuery().get("text:OpenCms").intValue();
        long facetTitleCount = results.getFacetQuery().get("Title_prop:OpenCms").intValue();
        echo(
            "Found '"
                + results.getFacetField("Title_exact").getValueCount()
                + "' facets for the field \"Title_exact\" and '"
                + facetTextCount
                + "' of them containing the word: \"OpenCms\" in the field 'text' and '"
                + facetTitleCount
                + "' of them containing the word \"OpenCms\" in the field 'Title_prop!'");

        query = new CmsSolrQuery(getCmsObject(), CmsRequestUtil.createParameterMap("q=text:OpenCms"));
        results = index.search(getCmsObject(), query);
        long numExpected = results.getNumFound();

        assertEquals(numExpected, facetTextCount);
        echo("Great Solr works fine!");
    }

    /**
     * @throws Throwable
     */
    public void testAdvancedHighlighting() throws Throwable {

        // TODO: improve
        echo("Testing highlighting");

        // creating the query: facet=true&facet.field=Title_exact&facet.mincount=1&facet.query=text:OpenCms&rows=0
        SolrQuery query = new CmsSolrQuery(getCmsObject(), null);
        // hl=true
        query.setHighlight(true);
        // add df to the query - otherwise highlighting will fail
        query.set("df", "text");

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        CmsSolrResultList results = index.search(getCmsObject(), query);

        assertNotNull(results.getHighLighting());
        echo("Highlighting works fine!");

        CmsSolrQuery q = new CmsSolrQuery(getCmsObject(), null);
        q.setTextSearchFields("content_en");
        q.setText("OpenCms");
        q.setHighlight(true);
        q.setHighlightFragsize(200);
        q.setHighlightFields("content_en");
        CmsSolrResultList res = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE).search(getCmsObject(), q);
        Map<String, Map<String, List<String>>> highlighting = res.getHighLighting();
        assertTrue("There should be some highlighted documents", highlighting != null);

        if (highlighting != null) {
            for (Map<String, List<String>> map : highlighting.values()) {
                for (List<String> entry : map.values()) {
                    for (String s : entry) {
                        assertTrue(
                            "There must occure OpenCms in the highlighting",
                            s.toLowerCase().contains("OpenCms".toLowerCase()));
                    }
                }
            }
        }
    }

    /**
     * @throws Throwable
     */
    public void testAdvancedMoreLikeThis() throws Throwable {

        // TODO: implement
    }

    /**
     * @throws Throwable
     */
    public void testAdvancedPaging() throws Throwable {

        // TODO: implement
    }

    /**
     * @throws Throwable
     */
    public void testAdvancedRangingDates() throws Throwable {

        // TODO: implement
    }

    /**
     * @throws Throwable
     */
    public void testAdvancedRangingNumerics() throws Throwable {

        // TODO: implement
    }

    /**
     * @throws Throwable
     */
    public void testAdvancedSorting() throws Throwable {

        // TODO: implement
    }

    /**
     * @throws Throwable
     */
    public void testAdvancedSpellChecking() throws Throwable {

        // TODO: implement
    }

    /**
     * @throws Throwable
     */
    public void testAdvancedSugesstion() throws Throwable {

        // TODO: implement
    }

    /**
     * @throws Throwable
     */
    public void testAdvancedSynonyms() throws Throwable {

        // TODO: implement
    }

    /**
     * Tests searching in various document types.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDocumentTypes() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing search for various document types");

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        CmsSolrQuery squery = new CmsSolrQuery();
        squery.setText("+Alkacon +OpenCms +Text");
        squery.setSearchRoots("/sites/default/types/");
        List<CmsSearchResource> results = index.search(cms, squery);

        assertEquals(1, results.size());
        assertEquals("/sites/default/types/text.txt", (results.get(0)).getRootPath());
    }

    /**
     * Tests the CmsSearch with folder names with upper case letters.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testFolderName() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing search for case sensitive folder names");

        echo("Testing search for case sensitive folder name: /testUPPERCASE/");
        testUppercaseFolderNameUtil(cms, "/testUPPERCASE/", 1);

        // extension of this test for 7.0.2:
        // now it is possible to search in restricted folders in a case sensitive way
        echo("Testing search for case sensitive folder name: /TESTuppercase/");
        testUppercaseFolderNameUtil(cms, "/TESTuppercase/", 1);

        // let's see if we find 2 results when we don't use a search root
        echo("Testing search for case sensitive folder names without a site root");
        testUppercaseFolderNameUtil(cms, null, 2);
    }

    /**
     * Test the cms search indexer.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testIndexer() throws Throwable {

        OpenCms.getSearchManager().rebuildAllIndexes(new CmsShellReport(Locale.ENGLISH));
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

        CmsSolrIndex index = new CmsSolrIndex(AllTests.INDEX_TEST);

        index.setProject("Offline");
        // important: use german locale for a special treat on term analyzing
        index.setLocale(Locale.GERMAN);
        index.setRebuildMode(I_CmsSearchIndex.REBUILD_MODE_AUTO);
        // available pre-configured in the test configuration files opencms-search.xml
        index.setFieldConfigurationName("solr_fields");
        index.addSourceName("solr_source2");

        // add the search index to the manager
        OpenCms.getSearchManager().addSearchIndex(index);

        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        OpenCms.getSearchManager().rebuildIndex(AllTests.INDEX_TEST, report);

        // perform a search on the newly generated index
        String query;
        CmsSolrResultList results;

        query = "q=*:*";
        results = index.search(getCmsObject(), query);
        AllTests.printResults(getCmsObject(), results, false);
        assertEquals(4, results.size());

        query = "q=+text:\"SearchEgg1\"";
        results = index.search(getCmsObject(), query);

        // assert one file is found in the default site
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", results.get(0).getRootPath());

        // change search root and assert no more files are found
        query = "q=+text:>>SearchEgg1<< +parent-folders:/folder1/";
        results = index.search(getCmsObject(), query);
        assertEquals(0, results.size());

        OpenCms.getSearchManager().removeSearchIndex(index);
    }

    /**
     * Tests an issue where no results are found in folders that have names
     * like <code>/basisdienstleistungen_-_zka/</code>.<p>
     *
     * @throws Exception if the test fails
     */
    public void testIssueWithSpecialFoldernames() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing search issue with special folder name");

        String folderName = "/basisdienstleistungen_-_zka/";
        cms.copyResource("/types/", folderName);
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        OpenCms.getSearchManager().getSearchIndexes();
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);

        // perform a search on the newly generated index
        String query;
        CmsSolrResultList results;

        query = "q=+text:\"Alkacon\" +text:\"OpenCms\" +parent-folders:\"/sites/default/\"&sort=path asc";
        results = index.search(getCmsObject(), query);
        AllTests.printResults(cms, results, false);
        assertEquals(8, results.size());
        assertEquals("/sites/default" + folderName + "text.txt", results.get(0).getRootPath());

        query = "q=+text:Alkacon +text:OpenCms +parent-folders:\"/sites/default" + folderName + "\"";
        results = index.search(getCmsObject(), query);
        AllTests.printResults(cms, results, false);
        assertEquals(1, results.size());
        assertEquals("/sites/default" + folderName + "text.txt", results.get(0).getRootPath());
    }

    /**
     * Tests searching with limiting the time ranges.<p>
     *
     * @throws Exception if the test fails
     */
    public void testLimitTimeRanges() throws Exception {

        DateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DF.setTimeZone(CmsSearchUtil.TIMEZONE_UTC);

        CmsObject cms = getCmsObject();
        echo("Testing searching with limiting to time ranges");

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        String query = "?rows=50&q=text:OpenCms";
        CmsSolrResultList results = index.search(getCmsObject(), query);
        int orgCount = results.size();

        // check min date created
        Date stamp = new Date();
        String date = DF.format(stamp);
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
        String maxDate = DF.format(new Date(stamp.getTime() - 1000));
        query = "?rows=50&q=+text:OpenCms  +created:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount, results.size());

        query = "?rows=50&q=+text:OpenCms  +created:[* TO NOW]";
        results = index.search(getCmsObject(), query);
        AllTests.printResults(cms, results, false);
        assertEquals(orgCount + 1, results.size());

        // wait a second because the Solr time range precision
        Thread.sleep(1000);

        // check min date last modified
        stamp = new Date();
        date = DF.format(stamp);
        query = "?rows=50&q=+text:OpenCms" + "&fq=lastmodified:[" + date + " TO NOW]";
        results = index.search(getCmsObject(), query);
        AllTests.printResults(getCmsObject(), results, false);
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
        maxDate = DF.format(new Date(stamp.getTime() - 1000));
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

        DateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DF.setTimeZone(CmsSearchUtil.TIMEZONE_UTC);

        CmsObject cms = getCmsObject();
        echo("Testing searching with optimized limiting to time ranges");

        String query = "?rows=50&q=+text:OpenCms";
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        CmsSolrResultList results = index.search(getCmsObject(), query);
        int orgCount = results.size();

        // check min date created
        Date stamp = new Date();
        String date = DF.format(new Date(stamp.getTime() - 20000));
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
        String maxDate = DF.format(new Date(stamp.getTime() - (1000 * 60 * 60 * 24)));
        query = "?rows=50&q=+text:OpenCms +created:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        // we will find one result less then before because of the previous date range rest
        assertEquals(orgCount - 1, results.size());

        maxDate = DF.format(new Date());
        query = "?rows=50&q=+text:OpenCms +created:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount + 1, results.size());

        // wait a second because the Solr time range precision
        Thread.sleep(1000);

        // check min date last modified
        Date newStamp = new Date();
        date = DF.format(newStamp);

        // move to tomorrow because of granularity level in optimized date search
        String minDate = DF.format(new Date(newStamp.getTime() + (1000 * 60 * 60 * 24)));
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
        AllTests.printResults(getCmsObject(), results, false);
        assertEquals(1, results.size());

        maxDate = DF.format(new Date(newStamp.getTime() - (1000 * 60 * 60 * 24)));
        query = "?rows=50&q=+text:OpenCms +lastmodified:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount - 1, results.size());

        query = "?rows=50&q=+text:OpenCms +lastmodified:[* TO NOW]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount + 1, results.size());
    }

    /**
     * @throws Throwable
     */
    public void testLocaleRestriction() throws Throwable {

        // turn off the language detection
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        index.setLanguageDetection(false);

        // some Strings
        String queryText = "Language Detection Document";
        String folderName = "/folder1/subfolder12/subsubfolder121/";
        String refsSource = "org/opencms/search/solr/lang-detect-doc.pdf";

        // import some test files
        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        CmsResource master = importTestResource(
            cms,
            refsSource,
            folderName + "master.pdf",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());
        CmsResource de = importTestResource(
            cms,
            refsSource,
            folderName + "master_de.pdf",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());
        CmsResource en = importTestResource(
            cms,
            refsSource,
            folderName + "master_en.pdf",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());
        CmsResource fr = importTestResource(
            cms,
            refsSource,
            folderName + "master_fr.pdf",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());

        // publish the project and update the search index
        OpenCms.getPublishManager().publishProject(cms, null);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setLocale(Locale.GERMAN);
        CmsSolrQuery query = new CmsSolrQuery(cms, null);
        query.setText(queryText);

        CmsSolrResultList result = index.search(cms, query);
        assertTrue(result.contains(master));
        assertTrue(!result.contains(en));
        assertTrue(result.contains(de));
        assertTrue(!result.contains(fr));

        query = new CmsSolrQuery();
        query.setLocales(Collections.singletonList(Locale.GERMAN));
        query.setText(queryText);
        result = index.search(cms, query);
        assertTrue(result.contains(master));
        assertTrue(!result.contains(en));
        assertTrue(result.contains(de));
        assertTrue(!result.contains(fr));

        query = new CmsSolrQuery();
        query.setLocales(Collections.singletonList(Locale.FRENCH));
        query.setText(queryText);
        result = index.search(cms, query);
        assertTrue(result.contains(master));
        assertTrue(!result.contains(en));
        assertTrue(!result.contains(de));
        assertTrue(result.contains(fr));

        // Locale set to English: should return the master and the master_en
        query = new CmsSolrQuery();
        query.setLocales(Collections.singletonList(Locale.ENGLISH));
        query.setText(queryText);
        result = index.search(cms, query);
        assertTrue(result.contains(master));
        assertTrue(result.contains(en));
        assertTrue(!result.contains(de));
        assertTrue(!result.contains(fr));

        // Locale not set: Search in all locales should return all 4 docs
        query = new CmsSolrQuery();
        List<Locale> l = Collections.emptyList();
        query.setLocales(l);
        query.setText(queryText);
        result = index.search(cms, query);
        assertTrue(result.contains(master));
        assertTrue(result.contains(en));
        assertTrue(result.contains(de));
        assertTrue(result.contains(fr));

        // turn the language detection on again
        index.setLanguageDetection(true);
    }

    /**
     * Tests searching with multiple search roots.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMultipleSearchRoots() throws Exception {

        echo("Testing searching with multiple search roots");

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);

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
                query += "parent-folders:\"" + rootList[j] + "\"";
                if (rootList.length > (j + 1)) {
                    query += " OR ";
                }
            }
            query += ")";

            CmsSolrResultList results = index.search(getCmsObject(), query);
            System.err.println(query);
            System.out.println("Result for search " + i + " (found " + results.size() + ", expected " + expect + ")");
            AllTests.printResults(getCmsObject(), results, false);
            assertEquals(expect, results.size());
        }
    }

    public void testPermissionFilterAndLimits() throws Throwable {

        echo("Testing permission filter");

        echo("Adding resources to search");
        CmsObject adminCms = OpenCms.initCmsObject(getCmsObject());
        adminCms.getRequestContext().setSiteRoot("/sites/default/");
        String mainFolder = "searchfolder";
        String subFolder1 = mainFolder + "/a";
        String subFolder2 = mainFolder + "/b";
        String searchUser = "searchUser";
        I_CmsResourceType folderType = OpenCms.getResourceManager().getResourceType(
            CmsResourceTypeFolder.getStaticTypeName());

        List<CmsResource> publishResources = new ArrayList<>(103);
        publishResources.add(adminCms.createResource(mainFolder, folderType));
        publishResources.add(adminCms.createResource(subFolder1, folderType));
        publishResources.add(adminCms.createResource(subFolder2, folderType));
        I_CmsResourceType plainType = OpenCms.getResourceManager().getResourceType(
            CmsResourceTypePlain.getStaticTypeName());
        for (int i = 11; i <= 60; i++) {
            publishResources.add(adminCms.createResource(subFolder1 + "/" + i + "a.txt", plainType));
        }
        for (int i = 11; i <= 60; i++) {
            publishResources.add(adminCms.createResource(subFolder2 + "/" + i + "b.txt", plainType));
        }

        adminCms.createUser(searchUser, "password", "Test user for permission check in search", null);
        adminCms.chacc(subFolder1, I_CmsPrincipal.PRINCIPAL_USER, "searchUser", "");

        // publish the project and update the search index
        OpenCms.getPublishManager().publishProject(
            adminCms,
            new CmsShellReport(adminCms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        echo("Querying indexed resource as Admin to ensure indexing went correctly");

        CmsSolrQuery query = new CmsSolrQuery(getCmsObject(), null);
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        query.addFilterQuery("parent-folders:\"/sites/default/" + mainFolder + "/\"");
        query.setSort(CmsSearchField.FIELD_FILENAME, ORDER.asc);
        query.setRows(Integer.valueOf(20));
        CmsSolrResultList adminResults = index.search(adminCms, query);
        assertEquals(100, adminResults.getNumFound());
        assertEquals(20, adminResults.size());

        echo("Searching as user with restricted permissions on parts of the indexed resources.");
        CmsObject userCms = OpenCms.initCmsObject(adminCms);
        userCms.getRequestContext().setCurrentProject(userCms.readProject(CmsProject.ONLINE_PROJECT_ID));
        userCms.loginUser(searchUser, "password");
        CmsSolrResultList userResults = index.search(userCms, query);
        assertEquals(80, userResults.getNumFound());
        assertEquals(20, userResults.size());

        query.setSort(CmsSearchField.FIELD_PATH, ORDER.asc);
        userResults = index.search(userCms, query);
        assertEquals(50, userResults.getNumFound());
        assertEquals(20, userResults.size());

        query.setStart(Integer.valueOf(60));
        userResults = index.search(userCms, query);
        assertEquals(50, userResults.getNumFound());
        assertEquals(0, userResults.size());

        echo("Testing processing limit");
        query.setStart(Integer.valueOf(0));
        userResults = index.search(userCms, query, false, null, false, null, 55);
        assertEquals(50, userResults.getNumFound());
        assertEquals(5, userResults.size());

        query.setStart(Integer.valueOf(60));
        userResults = index.search(userCms, query, false, null, false, null, 55);
        assertEquals(50, userResults.getNumFound());
        assertEquals(0, userResults.size());

    }

    /**
     * @throws Throwable
     */
    public void testQueryDefaults() throws Throwable {

        // test default query
        String defaultQuery = "q=*:*&fl=*,score&qt=edismax&rows=10&fq=expired:[NOW TO *]&fq=released:[* TO NOW]";
        CmsSolrQuery query = new CmsSolrQuery();
        assertEquals(defaultQuery, CmsEncoder.decode(query.toString()));

        // test creating default query by String
        query = new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(defaultQuery));
        assertEquals(defaultQuery, CmsEncoder.decode(query.toString()));

        // test creating default query by String
        String defaultContextQuery = "q=*:*&fl=*,score&qt=edismax&rows=10&fq=con_locales:en&fq=parent-folders:\"/sites/default/\"&fq=expired:[NOW TO *]&fq=released:[* TO NOW]";
        query = new CmsSolrQuery(getCmsObject(), null);
        assertEquals(defaultContextQuery, CmsEncoder.decode(query.toString()));

        // test creating default context query by String
        query = new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(defaultContextQuery));
        assertEquals(defaultContextQuery, CmsEncoder.decode(query.toString()));

        // test creating default context query by context and String
        query = new CmsSolrQuery(getCmsObject(), CmsRequestUtil.createParameterMap(defaultContextQuery));
        assertEquals(defaultContextQuery, CmsEncoder.decode(query.toString()));
    }

    /**
     * @throws Throwable
     */
    public void testQueryParameterStrength() throws Throwable {

        String defaultContextQuery = "q=*:*&fl=*,score&qt=edismax&rows=10&fq=con_locales:en&fq=parent-folders:\"/sites/default/\"&fq=expired:[NOW TO *]&fq=released:[* TO NOW]";
        String modifiedContextQuery = "q=*:*&fl=*,score&qt=edismax&rows=10&fq=con_locales:en&fq=parent-folders:\"/\"&fq=expired:[NOW TO *]&fq=released:[* TO NOW]";

        // members should be stronger than request context
        CmsSolrQuery query = new CmsSolrQuery(getCmsObject(), null);
        assertEquals(defaultContextQuery, CmsEncoder.decode(query.toString()));
        query.setSearchRoots("/");
        assertEquals(
            modifiedContextQuery,

            "q=*:*&fl=*,score&qt=edismax&rows=10&fq=con_locales:en&fq=parent-folders:\"/\"&fq=expired:[NOW TO *]&fq=released:[* TO NOW]");
        query.setLocales(Locale.GERMAN, Locale.FRENCH, Locale.ENGLISH);
        query.setLocales(Locale.GERMAN, Locale.FRENCH);
        assertEquals(
            "q=*:*&fl=*,score&qt=edismax&rows=10&fq=expired:[NOW TO *]&fq=released:[* TO NOW]&fq=parent-folders:\"/\"&fq=con_locales:(de OR fr)",
            CmsEncoder.decode(query.toString()));

        // parameters should be stronger than request context
        //        query = new CmsSolrQuery(getCmsObject(), CmsRequestUtil.createParameterMap("fq=parent-folders:\"/\""));
        //        assertEquals(modifiedContextQuery, query.toString());

        // parameters should be stronger than request context and members
        query = new CmsSolrQuery(
            getCmsObject(),
            CmsRequestUtil.createParameterMap(
                "q=test&fq=parent-folders:\"/\"&fq=con_locales:fr&fl=content_fr&rows=50&qt=edismax&fq=type:v8news&fq=expired:[NOW TO *]&fq=released:[* TO NOW]"));
        query.setText("test");
        query.setTextSearchFields("pla");
        query.setLocales(Locale.GERMAN);
        query.setFields("pla,plub");
        query.setRows(Integer.valueOf(1000));
        query.setRequestHandler("lucene");
        query.setResourceTypes("article");
        String ex = "q={!q.op=OR type=lucene qf=text_de}test&fl=pla,plub&qt=lucene&rows=1000&fq=parent-folders:\"/\"&fq=expired:[NOW TO *]&fq=released:[* TO NOW]&fq=con_locales:de&fq=type:article";
        assertEquals(ex, CmsEncoder.decode(query.toString()));

        assertEquals("article", CmsSolrQuery.getResourceType(query.getFilterQueries()));

    }

    /**
     * Tests sorting of search results.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSortResults() throws Exception {

        echo("Testing sorting of search results");
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);

        String query = "q=content_en:opencms meta_en:opencms";
        CmsSolrResultList results = index.search(getCmsObject(), query);
        System.out.println("Result sorted by relevance:");
        AllTests.printResults(getCmsObject(), results, false);

        // first run is default sort order
        float maxScore = results.getMaxScore().floatValue();
        int score = results.get(0).getScore(maxScore);
        assertTrue("Best match by score must always be 100 but is " + score, score == 100);
        for (int i = 1; i < results.size(); i++) {
            assertTrue(
                "Resource "
                    + results.get(i - 1).getRootPath()
                    + " not sorted as expected - index ["
                    + (i - 1)
                    + "/"
                    + i
                    + "]",
                results.get(i - 1).getScore(maxScore) >= results.get(i).getScore(maxScore));
        }

        // second run use Title sort order
        String lastTitle = null;
        CmsSolrQuery q = new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(query));
        Map<String, ORDER> orders = new LinkedHashMap<String, ORDER>();
        orders.put("Title_prop", ORDER.asc);
        orders.put("score", ORDER.asc);
        q.addSortFieldOrders(orders);
        results = index.search(getCmsObject(), q);
        System.out.println("Result sorted by title:");
        AllTests.printResults(getCmsObject(), results, false);
        Iterator<CmsSearchResource> i = results.iterator();
        while (i.hasNext()) {
            CmsSearchResource res = i.next();
            if (lastTitle != null) {
                // make sure result is sorted correctly
                assertTrue(lastTitle.compareTo(res.getField(CmsSearchField.FIELD_TITLE)) <= 0);
            }
            lastTitle = res.getField(CmsSearchField.FIELD_TITLE);
        }

        // third run use date last modified
        long lastTime = 0;
        q = new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(query));
        q.setRows(Integer.valueOf(100));
        orders = new LinkedHashMap<String, ORDER>();
        orders.put("lastmodified", ORDER.desc);
        q.addSortFieldOrders(orders);
        results = index.search(getCmsObject(), q);
        System.out.println("Result sorted by date last modified:");
        AllTests.printResults(getCmsObject(), results, false);
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
     * Test the cms search indexer.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testXmlContent() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing search for xml contents");

        String query;
        List<CmsSearchResource> results;

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        // String query = "+text:Alkacon +text:OpenCms +text:Text +parent-folders:/sites/default/types/*";
        query = "q=+text:\">>SearchEgg1<<\"";
        results = index.search(cms, query);
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", results.get(0).getRootPath());

        query = "q=+text:\">>SearchEgg2<<\"";
        results = index.search(cms, query);
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0002.html", results.get(0).getRootPath());

        query = "q=+text:\">>SearchEgg3<<\"";
        results = index.search(cms, query);
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0003.html", results.get(0).getRootPath());

        // check (on console) that the file does contain a link to the /xmlcontent/ folder
        CmsFile article4 = cms.readFile("/xmlcontent/article_0004.html");
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, article4, true);
        echo(content.toString());

        // now search for another Query "xmlcontent", this must not be found in article 4 since it is excluded
        query = "q=+text:\"xmlcontent\"";
        results = index.search(cms, query);
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0003.html", (results.get(0)).getRootPath());
        // assertEquals("/sites/default/xmlcontent/article_0004.html", ((CmsSearchResult)results.get(1)).getPath());
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
    private void testUppercaseFolderNameUtil(CmsObject cms, String folderName, int expected) throws Exception {

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

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        CmsSolrQuery query = new CmsSolrQuery();
        query.setText("Testfile Struktur");
        if (folderName != null) {
            query.setSearchRoots(cms.getRequestContext().addSiteRoot(folderName));
        }
        CmsSolrResultList results = index.search(cms, query);
        AllTests.printResults(cms, results, false);

        assertEquals(expected, results.size());
    }
}