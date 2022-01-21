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

package org.opencms.jsp.search.config.parser;

import org.opencms.json.JSONException;
import org.opencms.jsp.search.config.CmsSearchConfiguration;
import org.opencms.jsp.search.config.CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.CmsSearchConfigurationDidYouMean;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetQuery;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetQuery.CmsFacetQueryItem;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.CmsSearchConfigurationGeoFilter;
import org.opencms.jsp.search.config.CmsSearchConfigurationHighlighting;
import org.opencms.jsp.search.config.CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.CmsSearchConfigurationSorting;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet.SortOrder;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery.I_CmsFacetQueryItem;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange.Other;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationGeoFilter;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSorting;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests the JSON configuration parser of cms:search. */
public class TestJSONSearchConfigurationParser extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestJSONSearchConfigurationParser(String arg0) {

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
        suite.addTest(new TestJSONSearchConfigurationParser("testParseCompleteConfiguration"));
        suite.addTest(new TestJSONSearchConfigurationParser("testParseMultiplePageSizes"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/", "/../org/opencms/search/solr");
                // disable all lucene indexes
                for (String indexName : OpenCms.getSearchManager().getIndexNames()) {
                    if (!indexName.equalsIgnoreCase(CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE)) {
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
     * Reads a complete configuration (with all options used) and tests if it matches the expectation.
     *
     * @throws IOException thrown if reading the configuration file with the test configuration fails
     * @throws URISyntaxException thrown if reading the configuration file with the test configuration fails
     * @throws CmsException thrown if the CmsObject cannot be retrieved.
     */
    @org.junit.Test
    public void testParseCompleteConfiguration() throws IOException, URISyntaxException, CmsException {

        String configString = new String(
            Files.readAllBytes(Paths.get(getClass().getResource("fullConfig.json").toURI())));
        try {
            CmsSearchConfiguration config = new CmsSearchConfiguration(
                new CmsJSONSearchConfigurationParser(configString),
                getCmsObject());
            testParseCompleteConfigurationInternal(config);
        } catch (JSONException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test if parsing multiple page sizes as hyphen separated string works.
     * @throws IOException
     * @throws URISyntaxException
     * @throws CmsException
     */
    @org.junit.Test
    public void testParseMultiplePageSizes() throws IOException, URISyntaxException, CmsException {

        String configString = new String(
            Files.readAllBytes(Paths.get(getClass().getResource("multiplePageSizes.json").toURI())));
        try {
            CmsSearchConfiguration config = new CmsSearchConfiguration(
                new CmsJSONSearchConfigurationParser(configString),
                getCmsObject());
            List<Integer> pageSizes = new ArrayList<>(2);
            pageSizes.add(Integer.valueOf(5));
            pageSizes.add(Integer.valueOf(8));
            I_CmsSearchConfigurationPagination expected = new CmsSearchConfigurationPagination(
                CmsSearchConfigurationPagination.DEFAULT_PAGE_PARAM,
                pageSizes,
                Integer.valueOf(CmsSearchConfigurationPagination.DEFAULT_PAGE_NAV_LENGTH));
            ConfigurationTester.testPaginationConfiguration(expected, config.getPaginationConfig());
        } catch (JSONException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Checks if the configuration matches the expectation.
     *
     * @param config the actual configuration.
     */
    private void testParseCompleteConfigurationInternal(CmsSearchConfiguration config) {

        // Test general/common configuration
        Map<String, String> additionalParameters = new HashMap<String, String>(2);
        additionalParameters.put("p1", "fq=lastmodified:[%(value) TO *]");
        additionalParameters.put("p2", "fq=%(value)");
        additionalParameters.put("p3", null);
        I_CmsSearchConfigurationCommon commonConfig = new CmsSearchConfigurationCommon(
            "querytest",
            "lastquerytest",
            Boolean.TRUE,
            "reloadedparam",
            Boolean.FALSE,
            Boolean.TRUE,
            "content_en:%(query) OR content_de:%(query) OR spell:%(query) OR Title_prop:%(query)",
            "Test Index",
            "Test Core",
            "fq=type:plain",
            additionalParameters,
            Boolean.TRUE,
            Boolean.TRUE,
            345);
        ConfigurationTester.testGeneralConfiguration(commonConfig, config.getGeneralConfig());

        // Test pagination configuration
        I_CmsSearchConfigurationPagination paginationConfig = new CmsSearchConfigurationPagination(
            "pageparam",
            Integer.valueOf(20),
            Integer.valueOf(9));
        ConfigurationTester.testPaginationConfiguration(paginationConfig, config.getPaginationConfig());

        // Test sorting configuration
        I_CmsSearchConfigurationSortOption sortOption1 = new CmsSearchConfigurationSortOption(
            null,
            null,
            "lastmodified desc");
        I_CmsSearchConfigurationSortOption sortOption2 = new CmsSearchConfigurationSortOption(
            "lastmodified ascending",
            "sort2",
            "lastmodified asc");
        List<I_CmsSearchConfigurationSortOption> sortOptions = new ArrayList<I_CmsSearchConfigurationSortOption>(2);
        sortOptions.add(sortOption1);
        sortOptions.add(sortOption2);
        I_CmsSearchConfigurationSorting sortingConfig = new CmsSearchConfigurationSorting(
            "sortparam",
            sortOptions,
            sortOption2);
        ConfigurationTester.testSortingConfiguration(sortingConfig, config.getSortConfig());

        // Test field facet configuration
        List<String> preselection = new ArrayList<String>(2);
        preselection.add("location/europe/");
        preselection.add("topic/");
        List<String> excludeTags = new ArrayList<String>(2);
        excludeTags.add("oneKey");
        excludeTags.add("anotherKey");
        I_CmsSearchConfigurationFacetField fieldFacet1 = new CmsSearchConfigurationFacetField(
            "category_exact",
            "category",
            Integer.valueOf(1),
            Integer.valueOf(6),
            "location/",
            "Categories",
            SortOrder.index,
            "nonsense - %(value)",
            Boolean.TRUE,
            preselection,
            Boolean.TRUE,
            excludeTags);
        Collection<String> facetNames = new ArrayList<String>(5);
        facetNames.add("category");
        facetNames.add("Keywords");
        facetNames.add("modification");
        facetNames.add("size");
        facetNames.add("query_query");
        fieldFacet1.propagateAllFacetNames(facetNames);
        I_CmsSearchConfigurationFacetField fieldFacet2 = new CmsSearchConfigurationFacetField(
            "Keywords",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

        Map<String, I_CmsSearchConfigurationFacetField> actualFieldFacetConfigs = config.getFieldFacetConfigs();
        assertEquals(2, actualFieldFacetConfigs.size());
        ConfigurationTester.testFieldFacetConfiguration(fieldFacet1, actualFieldFacetConfigs.get("category"));
        ConfigurationTester.testFieldFacetConfiguration(fieldFacet2, actualFieldFacetConfigs.get("Keywords"));

        // Test range facet configuration
        Collection<I_CmsSearchConfigurationFacetRange.Other> other = new ArrayList<I_CmsSearchConfigurationFacetRange.Other>(
            5);
        other.add(Other.before);
        other.add(Other.after);
        other.add(Other.between);
        other.add(Other.all);
        other.add(Other.none);

        preselection.clear();
        preselection.add("2015-01-01T00:00:00Z");
        preselection.add("2016-01-01T00:00:00Z");
        I_CmsSearchConfigurationFacetRange rangeFacet1 = new CmsSearchConfigurationFacetRange(
            "lastmodified",
            "NOW/MONTH-20MONTHS",
            "NOW/MONTH",
            "+1MONTHS",
            other,
            Boolean.FALSE,
            "modification",
            Integer.valueOf(1),
            "Date lastmodified",
            Boolean.TRUE,
            preselection,
            Boolean.TRUE,
            excludeTags);
        rangeFacet1.propagateAllFacetNames(facetNames);
        I_CmsSearchConfigurationFacetRange rangeFacet2 = new CmsSearchConfigurationFacetRange(
            "size",
            "0",
            "1000000",
            "1000",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
        Map<String, I_CmsSearchConfigurationFacetRange> actualRangeFacets = config.getRangeFacetConfigs();
        assertEquals(2, actualFieldFacetConfigs.size());
        ConfigurationTester.testRangeFacetConfiguration(rangeFacet1, actualRangeFacets.get("modification"));
        ConfigurationTester.testRangeFacetConfiguration(rangeFacet2, actualRangeFacets.get("size"));

        // Test query facet configuration
        preselection.clear();
        preselection.add("created:[NOW-1MONTH TO NOW]");
        preselection.add("created:[* TO NOW-1MONTHS]");
        List<I_CmsFacetQueryItem> queries = new ArrayList<I_CmsFacetQueryItem>(3);
        queries.add(new CmsFacetQueryItem("created:[* TO NOW-1YEARS]", "older than one year"));
        queries.add(new CmsFacetQueryItem("created:[* TO NOW-1MONTHS]", "older than one month"));
        queries.add(new CmsFacetQueryItem("created:[NOW-1MONTH TO NOW]", null));

        I_CmsSearchConfigurationFacetQuery queryFacet = new CmsSearchConfigurationFacetQuery(
            queries,
            "Creation date",
            Boolean.TRUE,
            preselection,
            Boolean.TRUE,
            excludeTags);
        queryFacet.propagateAllFacetNames(facetNames);
        ConfigurationTester.testQueryFacetConfiguration(queryFacet, config.getQueryFacetConfig());

        // Test Highlighter configuration
        I_CmsSearchConfigurationHighlighting highlightingConfig = new CmsSearchConfigurationHighlighting(
            "content_en",
            Integer.valueOf(2),
            Integer.valueOf(123),
            "content",
            Integer.valueOf(124),
            "<strong>",
            "</strong>",
            "simple",
            "gap",
            Boolean.TRUE);
        ConfigurationTester.testHighlightingConfiguration(highlightingConfig, config.getHighlighterConfig());

        // Test DidYouMean configuration
        I_CmsSearchConfigurationDidYouMean didYouMeanConfig = new CmsSearchConfigurationDidYouMean(
            "dymparam",
            Boolean.FALSE,
            Integer.valueOf(7));
        ConfigurationTester.testDidYouMeanConfiguration(didYouMeanConfig, config.getDidYouMeanConfig());

        // Test Geo filter configuration
        I_CmsSearchConfigurationGeoFilter geoFilterConfig = new CmsSearchConfigurationGeoFilter(
            "0.000000,0.000000",
            "coordinates",
            "geocoords_loc",
            "1.23456",
            "radius",
            "km",
            "units");
        ConfigurationTester.testGeoFilterConfiguration(geoFilterConfig, config.getGeoFilterConfig());
    }
}
