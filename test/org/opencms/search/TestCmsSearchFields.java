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

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.search.BooleanClause.Occur;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for searching in special fields of extracted document text.<p>
 *
 */
public class TestCmsSearchFields extends OpenCmsTestCase {

    /** Name of the index used for testing. */
    public static final String INDEX_OFFLINE = "Offline project (VFS)";

    /** The index used for testing. */
    public static final String INDEX_ONLINE = "Online project (VFS)";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsSearchFields(String arg0) {

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
        suite.setName(TestCmsSearchFields.class.getName());

        suite.addTest(new TestCmsSearchFields("testSearchInFields"));
        suite.addTest(new TestCmsSearchFields("testExcerptCreationFromFields"));
        suite.addTest(new TestCmsSearchFields("testSearchWithFieldQuery"));
        suite.addTest(new TestCmsSearchFields("testSearchWithCombinedFieldQuery"));
        suite.addTest(new TestCmsSearchFields("testSearchWithPreBuildQuery"));
        suite.addTest(new TestCmsSearchFields("testExcerptCreationWithFieldQuery"));
        suite.addTest(new TestCmsSearchFields("testSearchWithResouceTypeLimitaion"));

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
     * Tests excerpt generation only from searched fields.<p>
     *
     * @throws Exception if the test fails
     */
    public void testExcerptCreationFromFields() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing excerpt generation only from searched fields");

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");
        searchBean.setQuery("Cologne");
        // search only "special" field
        // NOTE: This has NOT been included in excerpt generation in opencms-search.xml
        searchBean.setField(new String[] {"special"});

        // use the default setting for "excerpt only from searched",
        // so an excerpt is generated for some results even though the searches "special" field has no excerpt at all
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found searching in 'special' index field, some excerpts should be available:");
        TestCmsSearch.printResults(searchResult, cms, true);
        Iterator<CmsSearchResult> i = searchResult.iterator();
        boolean excerptFound = false;
        while (i.hasNext()) {
            CmsSearchResult res = i.next();
            // not all results have excerpts, some are images
            excerptFound |= CmsStringUtil.isNotEmpty(res.getExcerpt());
        }
        assertTrue(excerptFound);

        // now change the setting for "excerpt only from searched", use excerpt only from searched field
        // since the "special" field has no excerpt, all excerpts must be empty
        searchBean.setExcerptOnlySearchedFields(true);
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found searching in 'special' index field, NO excerpts should be available:");
        TestCmsSearch.printResults(searchResult, cms, true);
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = i.next();
            assertTrue(CmsStringUtil.isEmpty(res.getExcerpt()));
        }

        // keep the setting for "excerpt only from searched",
        // but now search also "content" field, some excerpts must be available again
        searchBean.setField(new String[] {"content", "special"});
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println(
            "\n\nResults found searching in 'content' AND 'special' index fields, some excerpts should be available from 'content':");
        TestCmsSearch.printResults(searchResult, cms, true);
        excerptFound = false;
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = i.next();
            // not all results have excerpts, some are images
            excerptFound |= CmsStringUtil.isNotEmpty(res.getExcerpt());
        }
        assertTrue(excerptFound);
    }

    /**
     * Tests excerpt generation with a field query.<p>
     *
     * @throws Exception if the test fails
     */
    public void testExcerptCreationWithFieldQuery() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing excerpt generation with a field query");

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");
        searchBean.setQuery("Cologne");

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");

        // search for "Cologne" in the "special" field
        searchBean.addFieldQueryMust("special", "Cologne");

        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found with field query searching in 'special' index field:");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(7, searchResult.size());

        Iterator<CmsSearchResult> i = searchResult.iterator();
        boolean excerptFound = false;
        while (i.hasNext()) {
            CmsSearchResult res = i.next();
            if (CmsStringUtil.isNotEmpty(res.getExcerpt())) {
                excerptFound = true;
                System.out.println(res.getPath() + ":");
                System.out.println(res.getExcerpt());
            }
        }
        assertTrue(excerptFound);

        // now change the setting for "excerpt only from searched", use excerpt only from searched field
        // please note the "special" field has no excerpt
        searchBean.setExcerptOnlySearchedFields(true);
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found with field query searching in 'special' index field:");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(7, searchResult.size());

        i = searchResult.iterator();
        excerptFound = false;
        while (i.hasNext()) {
            CmsSearchResult res = i.next();
            if (CmsStringUtil.isNotEmpty(res.getExcerpt())) {
                excerptFound = true;
                System.out.println(res.getPath() + ":");
                System.out.println(res.getExcerpt());
            }
        }
        // in this search there must be no excerpt found
        // because we only searched in "special" which is not in the excerpt at all
        assertFalse(excerptFound);

        // now also require that "article" is part of the content
        // this is required because "special" is not in the excerpt
        searchBean.addFieldQueryMust(CmsSearchField.FIELD_CONTENT, "article");
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found with field query searching in 'special' and 'Content' index field:");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(4, searchResult.size());

        i = searchResult.iterator();
        excerptFound = false;
        while (i.hasNext()) {
            CmsSearchResult res = i.next();
            if (CmsStringUtil.isNotEmpty(res.getExcerpt())) {
                excerptFound = true;
                System.out.println(res.getPath() + ":");
                System.out.println(res.getExcerpt());
            }
        }
        assertTrue(excerptFound);
    }

    /**
     * Tests searching in non-standard fields for specific Strings that are placed in
     * various document formats.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSearchInFields() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing searching in non-standard content fields");

        // the search index may not have been created so far
        OpenCms.getSearchManager().rebuildIndex(INDEX_ONLINE, new CmsShellReport(Locale.ENGLISH));

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        // The following "non-standard" mapping is set in the "opencms-search.xml" for this test case:
        //
        // <field name="special" store="true" tokenize="true">
        //     <mapping type="element">special</mapping>
        //     <mapping type="xpath">Teaser[1]</mapping>
        //     <mapping type="xpath">Teaser[2]</mapping>
        //     <mapping type="xpath">Teaser[3]</mapping>
        //     <mapping type="property">NavText</mapping>
        //     <mapping type="property-search">search.special</mapping>
        // </field>

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");
        searchBean.setQuery("Cologne");

        // search only "special" field
        searchBean.setField(new String[] {"special"});
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found searching in 'special' index field:");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(7, searchResult.size());

        // search in the default fields (does not contain one result from a "NavText" property)
        System.out.println("\n\nResults found searching in standard index fields:");
        searchBean.setField(CmsSearchIndex.DOC_META_FIELDS);
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(3, searchResult.size());
    }

    /**
     * Tests searching with a combined field query that includes SHOULD and MUST.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSearchWithCombinedFieldQuery() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing search with a combined field query");

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");

        // search for "Cologne" in the "special" field
        searchBean.addFieldQueryMust("special", "Cologne");

        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found with field query searching in 'special' index field:");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(7, searchResult.size());

        // now also require that "SearchEgg1" is part of the Title
        searchBean.addFieldQueryShould(CmsSearchField.FIELD_TITLE_UNSTORED, "SearchEgg1");
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found with field query searching in 'special' and 'Title' index field again:");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(1, searchResult.size());
    }

    /**
     * Tests searching with a field query,
     * that is a query over multiple fields with different search terms per field.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSearchWithFieldQuery() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing search with a specific field query");

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");

        // search for "Cologne" in the "special" field
        searchBean.addFieldQueryMust("special", "Cologne");

        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found with field query searching in 'special' index field:");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(7, searchResult.size());

        // now also require that "article" is part of the Title
        searchBean.addFieldQueryMust(CmsSearchField.FIELD_TITLE_UNSTORED, "article");
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found with field query searching in 'special' and 'Title' index field:");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(5, searchResult.size());

        // now also require that "SearchEgg1" is part of the Title
        searchBean.addFieldQueryMust(CmsSearchField.FIELD_TITLE_UNSTORED, "SearchEgg1");
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found with field query searching in 'special' and 'Title' index field again:");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(1, searchResult.size());

        // now do a new search with some different parameters
        // perform a search on the newly generated index
        searchBean = new CmsSearch();
        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");

        // search for "Cologne" in the "special" field
        searchBean.addFieldQueryMust("special", "Cologne");

        // now also require that "article" is part of the Title
        searchBean.addFieldQueryMustNot(CmsSearchField.FIELD_TITLE_UNSTORED, "article");
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println(
            "\n\nResults found with field query searching in 'special' and 'Title' index field with NOT option:");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(2, searchResult.size());
    }

    /**
     * Tests searching with a pre-build field query.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSearchWithPreBuildQuery() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing search with a pre-build field query");

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");

        // search for "article" or "opencms" in the "title" field, or "opencms" in the content field
        searchBean.addFieldQuery(
            new CmsSearchParameters.CmsSearchFieldQuery(
                CmsSearchField.FIELD_TITLE_UNSTORED,
                Occur.SHOULD,
                Arrays.asList("article", "opencms"),
                Occur.SHOULD));
        searchBean.addFieldQueryShould(CmsSearchField.FIELD_CONTENT, "opencms");
        // extend the search to make the query more complex
        searchBean.addFieldQuery(
            new CmsSearchParameters.CmsSearchFieldQuery(
                CmsSearchField.FIELD_TITLE_UNSTORED,
                Occur.MUST,
                Arrays.asList("article", "page*", "index", "alkacon"),
                Occur.SHOULD));
        // extend the search to make the query more complex
        searchBean.addFieldQuery(
            new CmsSearchParameters.CmsSearchFieldQuery(
                CmsSearchField.FIELD_TITLE_UNSTORED,
                Occur.MUST_NOT,
                Arrays.asList("subfolder", "page1"),
                Occur.SHOULD));

        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found with a comlex field query:");
        TestCmsSearch.printResults(searchResult, cms);
        String parsedQuery1 = searchBean.getParsedQuery();
        echo("Query: " + parsedQuery1);
        assertEquals(8, searchResult.size());

        // now do a new search with the same parameters from a string
        searchBean = new CmsSearch();
        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");

        // search for "Cologne" in the "special" field
        searchBean.setParsedQuery(parsedQuery1);

        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found when reusing pre-parsed query:");
        TestCmsSearch.printResults(searchResult, cms);
        String parsedQuery2 = searchBean.getParsedQuery();
        echo("Query: " + parsedQuery2);
        assertEquals(8, searchResult.size());

        // now do a new search with the same parameters from a string
        searchBean = new CmsSearch();
        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");

        // search for "Cologne" in the "special" field
        searchBean.setParsedQuery(parsedQuery2);

        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found when reusing pre-parsed query:");
        TestCmsSearch.printResults(searchResult, cms);
        String parsedQuery3 = searchBean.getParsedQuery();
        echo("Query: " + parsedQuery3);
        assertEquals(8, searchResult.size());
    }

    /**
     * Tests limiting the search result to certain resource types.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSearchWithResouceTypeLimitaion() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Test searching with a limitation on resource types");

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");

        // search for "Cologne" in the "special" field
        searchBean.addFieldQueryMust("special", "Cologne");

        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found with field query searching in 'special' index field:");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(7, searchResult.size());

        // now limit the search to types "image", "plain" and "xmlpage"
        searchBean.setResourceTypes(new String[] {"image", "plain", "xmlpage"});
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println(
            "\n\nResults found with field query and a limit of the search to types \"image\", \"plain\" and \"xmlpage\":");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(3, searchResult.size());

        // now limit the search to type "article"
        searchBean.setResourceType("article");
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found with field query and a limit of the search to type \"article\":");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(4, searchResult.size());
    }
}