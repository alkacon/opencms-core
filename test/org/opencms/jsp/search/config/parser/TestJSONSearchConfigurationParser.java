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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.opencms.json.JSONException;
import org.opencms.jsp.search.config.CmsSearchConfiguration;
import org.opencms.jsp.search.config.CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.CmsSearchConfigurationDidYouMean;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetQuery;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetQuery.CmsFacetQueryItem;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetRange;
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
import org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSorting;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/** Tests the JSON configuration parser of cms:search. */
public class TestJSONSearchConfigurationParser {

    /**
     * Reads a complete configuration (with all options used) and tests if it matches the expectation.
     *
     * @throws IOException thrown if reading the configuration file with the test configuration fails
     * @throws URISyntaxException thrown if reading the configuration file with the test configuration fails
     */
    @Test
    public void testParseCompleteConfiguration() throws IOException, URISyntaxException {

        String configString = new String(
            Files.readAllBytes(Paths.get(getClass().getResource("fullConfig.json").toURI())));
        try {
            CmsSearchConfiguration config = new CmsSearchConfiguration(
                new CmsJSONSearchConfigurationParser(configString));
            testParseCompleteConfigurationInternal(config);
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
        I_CmsSearchConfigurationCommon commonConfig = new CmsSearchConfigurationCommon(
            "querytest",
            "lastquerytest",
            Boolean.TRUE,
            "reloadedparam",
            Boolean.FALSE,
            Boolean.TRUE,
            "content_en:%(query) OR content_de:%(query) OR spell:%(query) OR Title_prop:%(query)",
            "Solr Test Index",
            "Solr Test Core",
            "fq=type:plain",
            additionalParameters,
            Boolean.TRUE,
            Boolean.TRUE);
        ConfigurationTester.testGeneralConfiguration(commonConfig, config.getGeneralConfig());

        // Test pagination configuration
        I_CmsSearchConfigurationPagination paginationConfig = new CmsSearchConfigurationPagination(
            "pageparam",
            Integer.valueOf(20),
            Integer.valueOf(9));
        ConfigurationTester.testPaginationConfiguration(paginationConfig, config.getPaginationConfig());

        // Test sorting configuration
        I_CmsSearchConfigurationSortOption sortOption1 = new CmsSearchConfigurationSortOption(
            "lastmodified descending",
            "sort1",
            "lastmodified desc");
        I_CmsSearchConfigurationSortOption sortOption2 = new CmsSearchConfigurationSortOption(
            null,
            null,
            "lastmodified desc");
        List<I_CmsSearchConfigurationSortOption> sortOptions = new ArrayList<I_CmsSearchConfigurationSortOption>(2);
        sortOptions.add(sortOption1);
        sortOptions.add(sortOption2);
        I_CmsSearchConfigurationSorting sortingConfig = new CmsSearchConfigurationSorting(
            "sortparam",
            sortOptions,
            sortOption1);
        ConfigurationTester.testSortingConfiguration(sortingConfig, config.getSortConfig());

        // Test field facet configuration
        List<String> preselection = new ArrayList<String>(2);
        preselection.add("location/europe/");
        preselection.add("topic/");
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
            Boolean.TRUE);
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
            Boolean.TRUE);
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
            Boolean.TRUE);
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
    }

}
