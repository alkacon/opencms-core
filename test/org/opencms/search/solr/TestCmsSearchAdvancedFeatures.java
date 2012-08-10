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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.A_CmsSearchIndex;
import org.opencms.search.CmsLuceneIndex;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchCategoryCollector;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.fields.CmsSearchFieldConfigurationOldCategories;
import org.opencms.search.fields.I_CmsSearchField;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.schema.DateField;

/**
 * Unit test for advanced search features.<p>
 * 
 */
public class TestCmsSearchAdvancedFeatures extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSearchAdvancedFeatures(String arg0) {

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
        suite.setName(TestCmsSearchAdvancedFeatures.class.getName());

        suite.addTest(new TestCmsSearchAdvancedFeatures("testSortSearchResults"));
        //        suite.addTest(new TestCmsSearchAdvancedFeatures("testSearchCategories"));
        suite.addTest(new TestCmsSearchAdvancedFeatures("testMultipleSearchRoots"));
        //        suite.addTest(new TestCmsSearchAdvancedFeatures("testSearchRestriction"));
        // suite.addTest(new TestCmsSearchAdvancedFeatures("testLimitTimeRanges"));
        // suite.addTest(new TestCmsSearchAdvancedFeatures("testLimitTimeRangesOptimized"));
        //        suite.addTest(new TestCmsSearchAdvancedFeatures("testOnlyFilterSearch"));

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
     * Tests searching with limiting the time ranges.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testLimitTimeRanges() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing searching with limiting to time ranges");

        // TODO There should be a way to limit searches by time ranges in Solr too.
        //        CmsLuceneIndex index = OpenCms.getSearchManager().getIndexLucene(TestCmsSearch.SOLR_OFFLINE);
        //        index.addConfigurationParameter(A_CmsSearchIndex.TIME_RANGE, "true");
        //        assertTrue("Index '" + TestCmsSearch.SOLR_OFFLINE + "' not checking time range as expected", index.isCheckingTimeRange());

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(TestCmsSearch.SOLR_OFFLINE);
        String query = "q=+text_en:OpenCms";
        List<CmsSearchResource> results = index.search(getCmsObject(), query);
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
        OpenCms.getSearchManager().rebuildIndex(TestCmsSearch.SOLR_OFFLINE, report);

        results = index.search(getCmsObject(), query);
        assertEquals(1, results.size());

        // check max date created
        String maxDate = DateField.formatExternal(new Date(stamp.getTime() - 1000));
        query = "q=+text_en:OpenCms  +created:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount, results.size());

        query = "q=+text_en:OpenCms  +created:[* TO NOW]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount + 1, results.size());

        // check min date last modified
        stamp = new Date();
        date = DateField.formatExternal(stamp);
        query = "q=+text_en:OpenCms +lastmodified:[" + date + " TO NOW]";
        results = index.search(getCmsObject(), query);
        assertEquals(0, results.size());

        CmsFile file = cms.readFile(resName);
        file.setContents("OpenCms ist toll".getBytes());
        cms.writeFile(file);
        report = new CmsShellReport(cms.getRequestContext().getLocale());
        OpenCms.getSearchManager().rebuildIndex(TestCmsSearch.SOLR_OFFLINE, report);

        results = index.search(getCmsObject(), query);
        assertEquals(1, results.size());

        // check max date last modified
        maxDate = DateField.formatExternal(new Date(stamp.getTime() - 1000));
        query = "q=+text_en:OpenCms +lastmodified:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount, results.size());

        query = "q=+text_en:OpenCms +lastmodified:[* TO NOW]";
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

        // TODO There should be a way to limit searches by time ranges in Solr too.        
        //      CmsLuceneIndex index = OpenCms.getSearchManager().getIndexLucene(TestCmsSearch.SOLR_OFFLINE);
        //      index.addConfigurationParameter(A_CmsSearchIndex.TIME_RANGE, "false");
        //      assertFalse("Index '" + TestCmsSearch.SOLR_OFFLINE + "' checking time range but should not", index.isCheckingTimeRange());

        String query = "q=+text_en:OpenCms";
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(TestCmsSearch.SOLR_OFFLINE);
        List<CmsSearchResource> results = index.search(getCmsObject(), query);
        int orgCount = results.size();

        // check min date created
        Date stamp = new Date();
        String date = DateField.formatExternal(new Date(stamp.getTime() - 20000));
        query = "q=+text_en:OpenCms +created:[" + date + " TO NOW]";
        results = index.search(getCmsObject(), query);
        // we must find one match because of the previous time range test
        assertEquals(1, results.size());

        String resName = "search_new_2.txt";
        cms.createResource(resName, CmsResourceTypePlain.getStaticTypeId(), "OpenCms".getBytes(), null);
        I_CmsReport report = new CmsShellReport(cms.getRequestContext().getLocale());
        OpenCms.getSearchManager().rebuildIndex(TestCmsSearch.SOLR_OFFLINE, report);

        results = index.search(getCmsObject(), query);
        // we must find one match because of the previous time range test
        assertEquals(2, results.size());

        // check max date created (must move back one day because of granularity level in optimized date range search)
        String maxDate = DateField.formatExternal(new Date(stamp.getTime() - (1000 * 60 * 60 * 24)));
        query = "q=+text_en:OpenCms +created:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        // we will find one result less then before because of the previous date range rest
        assertEquals(orgCount - 1, results.size());

        maxDate = DateField.formatExternal(new Date());
        query = "q=+text_en:OpenCms +created:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount + 1, results.size());

        // check min date last modified
        Date newStamp = new Date();
        date = DateField.formatExternal(newStamp);

        // move to tomorrow because of granularity level in optimized date search
        String minDate = DateField.formatExternal(new Date(newStamp.getTime() + (1000 * 60 * 60 * 24)));
        query = "q=+text_en:OpenCms +lastmodified:[ " + minDate + " TO NOW]";
        results = index.search(getCmsObject(), query);
        assertEquals(0, results.size());

        CmsFile file = cms.readFile(resName);
        file.setContents("OpenCms ist toll".getBytes());
        cms.writeFile(file);
        report = new CmsShellReport(cms.getRequestContext().getLocale());
        OpenCms.getSearchManager().rebuildIndex(TestCmsSearch.SOLR_OFFLINE, report);

        query = "q=+text_en:OpenCms +lastmodified:[" + date + " TO NOW]";
        results = index.search(getCmsObject(), query);
        // TODO This test finds two results for Lucene, but it should only be one
        // TODO This variant is correct for Solr
        assertEquals(1, results.size());

        maxDate = DateField.formatExternal(new Date(newStamp.getTime() - (1000 * 60 * 60 * 24)));
        query = "q=+text_en:OpenCms +lastmodified:[* TO " + maxDate + "]";
        results = index.search(getCmsObject(), query);
        assertEquals(orgCount - 1, results.size());

        query = "q=+text_en:OpenCms +lastmodified:[* TO NOW]";
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

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(TestCmsSearch.SOLR_OFFLINE);

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
            String query = "q=+text_en:OpenCms AND (";
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
            TestCmsSearch.printResults(results.getQueryResponse(), results, getCmsObject());
            assertEquals(expect, results.size());
        }
    }

    /**
     * Tests searching without a query only using a filter.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testOnlyFilterSearch() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing searching without a query only using a filter");

        CmsLuceneIndex index = OpenCms.getSearchManager().getIndexLucene(TestCmsSearch.SOLR_OFFLINE);
        index.addConfigurationParameter(A_CmsSearchIndex.TIME_RANGE, "false");
        assertFalse(
            "Index '" + TestCmsSearch.SOLR_OFFLINE + "' checking time range but should not",
            index.isCheckingTimeRange());

        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        searchBean.init(cms);
        searchBean.setIndex(TestCmsSearch.SOLR_OFFLINE);
        searchBean.setMatchesPerPage(1000);
        searchBean.getParameters().setIgnoreQuery(true);

        // query all files created since yesterday, this should be 2 because of previous tests
        Date stamp = new Date();
        searchBean.getParameters().setMinDateCreated(stamp.getTime() - (1000 * 60 * 60 * 24));

        searchResult = searchBean.getSearchResult();
        assertEquals(2, searchResult.size());

        // now do a search for all documents of type "plain"
        searchBean.getParameters().setMinDateCreated(Long.MIN_VALUE);
        searchBean.setResourceType(CmsResourceTypePlain.getStaticTypeName());
        searchBean.init(cms);
        searchResult = searchBean.getSearchResult();
        assertEquals(7, searchResult.size());

        // now do a search for all documents of type "plain" created since yesterday    
        searchBean.setResourceType(CmsResourceTypePlain.getStaticTypeName());
        searchBean.getParameters().setMinDateCreated(stamp.getTime() - (1000 * 60 * 60 * 24));
        searchBean.init(cms);
        searchResult = searchBean.getSearchResult();
        assertEquals(2, searchResult.size());

        // now do a search for all documents of type "binary" created since yesterday      
        searchBean.setResourceType(CmsResourceTypeBinary.getStaticTypeName());
        searchBean.getParameters().setMinDateCreated(stamp.getTime() - (1000 * 60 * 60 * 24));
        searchBean.init(cms);
        searchResult = searchBean.getSearchResult();
        assertEquals(0, searchResult.size());

        // now do a search for all documents
        searchBean.getParameters().setMinDateCreated(Long.MIN_VALUE);
        searchBean.setResourceTypes(null);
        searchBean.init(cms);
        searchResult = searchBean.getSearchResult();
        assertEquals(48, searchResult.size());
    }

    /**
     * Tests search category grouping.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSearchCategories() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing searching for categories");

        CmsLuceneIndex index = OpenCms.getSearchManager().getIndexLucene(TestCmsSearch.SOLR_OFFLINE);
        assertTrue(index.getFieldConfiguration() instanceof CmsSearchFieldConfigurationOldCategories);

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;
        String query = "q=OpenCms";

        // apply search categories to some folders

        CmsProperty cat1 = new CmsProperty(CmsPropertyDefinition.PROPERTY_SEARCH_CATEGORY, "category_1", null, true);
        CmsProperty cat2 = new CmsProperty(CmsPropertyDefinition.PROPERTY_SEARCH_CATEGORY, "category_2", null, true);
        CmsProperty cat3 = new CmsProperty(CmsPropertyDefinition.PROPERTY_SEARCH_CATEGORY, "category_3", null, true);

        cms.lockResource("/folder1/");
        cms.writePropertyObject("/folder1/", cat1);
        cms.unlockResource("/folder1/");
        cms.lockResource("/folder2/");
        cms.writePropertyObject("/folder2/", cat2);
        cms.unlockResource("/folder2/");
        cms.lockResource("/types/");
        cms.writePropertyObject("/types/", cat3);
        cms.unlockResource("/types/");

        // update the search index used
        //        OpenCms.getSearchManager().rebuildIndex(
        //            TestCmsSearch.SOLR_OFFLINE,
        //            new CmsShellReport(cms.getRequestContext().getLocale()));

        searchBean.init(cms);
        searchBean.setIndex(TestCmsSearch.SOLR_OFFLINE);
        searchBean.setQuery(query);
        searchBean.setMatchesPerPage(1000);
        searchBean.setCalculateCategories(true);

        // first run is default sort order
        searchResult = searchBean.getSearchResult();
        System.out.println("Result sorted by relevance:");
        //TestCmsSearch.printResults(searchResult, cms);
        assertEquals(13, searchResult.size());

        Map<String, Integer> categories = searchBean.getSearchResultCategories();
        // make sure categories where found
        assertNotNull(categories);
        // print the categories 
        System.out.println(CmsSearchCategoryCollector.formatCategoryMap(categories));
        // make sure the results are as expected
        assertTrue(categories.containsKey(cat1.getValue()));
        assertTrue(categories.containsKey(cat2.getValue()));
        assertTrue(categories.containsKey(cat3.getValue()));
        assertTrue(categories.containsKey(CmsSearchCategoryCollector.UNKNOWN_CATEGORY));
        // result must be all 3 categories plus 1 for "unknown"
        assertEquals(4, categories.size());
        // assert result count
        assertEquals(new Integer(7), categories.get(cat1.getValue()));
        assertEquals(new Integer(4), categories.get(cat2.getValue()));
        assertEquals(new Integer(1), categories.get(cat3.getValue()));
        assertEquals(new Integer(1), categories.get(CmsSearchCategoryCollector.UNKNOWN_CATEGORY));

        // count the category results
        searchBean.setCalculateCategories(false);

        String[][] cats = new String[][] {
            new String[] {cat1.getValue()},
            new String[] {cat2.getValue()},
            new String[] {cat3.getValue()},
            new String[] {cat1.getValue(), cat3.getValue()},
            new String[] {cat2.getValue(), cat3.getValue()},
            new String[] {cat1.getValue(), cat2.getValue()},
            new String[] {cat1.getValue(), cat2.getValue(), cat3.getValue()}};

        int[] expected = new int[] {7, 4, 1, 8, 5, 11, 12};

        for (int k = 0; k < expected.length; k++) {
            int expect = expected[k];
            String[] catList = cats[k];
            searchBean.setCategories(catList);
            searchResult = searchBean.getSearchResult();
            System.out.println("Result for search "
                + k
                + " (found "
                + searchResult.size()
                + ", expected "
                + expect
                + ")");
            //TestCmsSearch.printResults(searchResult, cms);
            assertEquals(expect, searchResult.size());
        }
    }

    /**
     * Tests searching with restrictions.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSearchRestriction() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing searching in search results");

        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;
        String query = "q=OpenCms";

        searchBean.init(cms);
        searchBean.setIndex(TestCmsSearch.SOLR_OFFLINE);
        searchBean.setMatchesPerPage(1000);
        searchBean.setQuery(query);

        // first part of the rest is identical to "testMultipleSearchRoots()"
        String[][] roots = new String[][] {
            new String[] {"/folder1/"},
            new String[] {"/folder2/"},
            new String[] {"/types/"},
            new String[] {"/folder2/", "/types/"},
            new String[] {"/folder1/", "/types/"},
            new String[] {"/folder1/", "/folder2/"},
            new String[] {"/folder1/", "/folder2/", "/types/"}};

        int[] expected = new int[] {7, 4, 1, 5, 8, 11, 12};

        for (int i = 0; i < expected.length; i++) {
            int expect = expected[i];
            String[] rootList = roots[i];
            searchBean.setSearchRoots(rootList);
            searchResult = searchBean.getSearchResult();
            System.out.println("Result for search "
                + i
                + " (found "
                + searchResult.size()
                + ", expected "
                + expect
                + ")");
            //TestCmsSearch.printResults(searchResult, cms);
            assertEquals(expect, searchResult.size());
        }

        // now create a restriction to search for an additional "Alkacon" (effectivly searching for "OpenCms Alkacon")
        CmsSearchParameters restriction;
        restriction = new CmsSearchParameters("Alkacon", null, null, null, null, false, null);

        expected = new int[] {3, 2, 1, 3, 4, 5, 6};

        for (int i = 0; i < expected.length; i++) {
            int expect = expected[i];
            String[] rootList = roots[i];
            searchBean.setSearchRoots(rootList);
            searchBean.setResultRestriction(restriction);
            searchResult = searchBean.getSearchResult();
            System.out.println("Result for search "
                + i
                + " (found "
                + searchResult.size()
                + ", expected "
                + expect
                + ")");
            //TestCmsSearch.printResults(searchResult, cms);
            assertEquals(expect, searchResult.size());
        }

        // another run of tests using searching only in the "meta" field
        restriction = new CmsSearchParameters(
            "Alkacon",
            Arrays.asList(new String[] {"meta"}),
            null,
            null,
            null,
            false,
            null);

        expected = new int[] {0, 0, 1, 1, 1, 0, 1};

        for (int i = 0; i < expected.length; i++) {
            int expect = expected[i];
            String[] rootList = roots[i];
            searchBean.setSearchRoots(rootList);
            searchBean.setResultRestriction(restriction);
            searchResult = searchBean.getSearchResult();
            System.out.println("Result for search "
                + i
                + " (found "
                + searchResult.size()
                + ", expected "
                + expect
                + ")");
            //TestCmsSearch.printResults(searchResult, cms);
            assertEquals(expect, searchResult.size());
        }

        // another run of tests using categories that have been defined in "testSearchCategories()"
        // reset search bean settings (restriction changed them) to initial values (see start of method)
        searchBean = new CmsSearch();
        searchBean.init(cms);
        searchBean.setIndex(TestCmsSearch.SOLR_OFFLINE);
        searchBean.setMatchesPerPage(1000);
        searchBean.setQuery(query);
        restriction = new CmsSearchParameters(
            null,
            null,
            null,
            Arrays.asList(new String[] {"category_1", "category_3"}),
            null,
            false,
            null);

        expected = new int[] {7, 0, 1, 1, 8, 7, 8};

        for (int i = 0; i < expected.length; i++) {
            int expect = expected[i];
            String[] rootList = roots[i];
            searchBean.setSearchRoots(rootList);
            searchBean.setResultRestriction(restriction);
            searchResult = searchBean.getSearchResult();
            System.out.println("Result for search "
                + i
                + " (found "
                + searchResult.size()
                + ", expected "
                + expect
                + ")");
            //TestCmsSearch.printResults(searchResult, cms);
            assertEquals(expect, searchResult.size());
        }
    }

    /**
     * Tests sorting of search results.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSortSearchResults() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing sorting of search results");

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(TestCmsSearch.SOLR_OFFLINE);
        // update the search index used
        OpenCms.getSearchManager().rebuildIndex(
            TestCmsSearch.SOLR_OFFLINE,
            new CmsShellReport(cms.getRequestContext().getLocale()));

        String query = "q=content_en:opencms meta_en:opencms";
        CmsSolrResultList results = index.search(getCmsObject(), query);
        System.out.println("Result sorted by relevance:");
        TestCmsSearch.printResults(results.getQueryResponse(), results, cms);
        // first run is default sort order

        float maxScore = results.getQueryResponse().getResults().getMaxScore().floatValue();
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
        SolrQuery q = new SolrQuery(query);
        q.addSortField("title-key", ORDER.asc);
        q.addSortField("score", ORDER.asc);
        results = index.search(getCmsObject(), q.getQuery());
        System.out.println("Result sorted by title:");
        TestCmsSearch.printResults(results.getQueryResponse(), results, cms);
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
        q = new SolrQuery(query);
        q.setQueryType("dismax");
        q.addField("*,score");
        q.setRows(new Integer(100));
        q.addSortField("lastmodified", ORDER.desc);
        results = index.search(getCmsObject(), q);
        System.out.println("Result sorted by date last modified:");
        TestCmsSearch.printResults(results.getQueryResponse(), results, cms);
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
}