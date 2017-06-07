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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSorting;

import java.util.Iterator;
import java.util.Map;

/** Helper to compare two configurations. */
public class ConfigurationTester {

    /**
     * Tests if expected and actual configuration are identically.
     *
     * @param expectedConfig the expected configuration
     * @param actualConfig the actual configuration
     */
    static void testDidYouMeanConfiguration(
        I_CmsSearchConfigurationDidYouMean expectedConfig,
        I_CmsSearchConfigurationDidYouMean actualConfig) {

        if (null == expectedConfig) {
            assertNull(actualConfig);
            return;
        }
        assertNotNull(actualConfig);

        assertEquals(Boolean.valueOf(expectedConfig.getCollate()), Boolean.valueOf(actualConfig.getCollate()));
        assertEquals(expectedConfig.getCount(), actualConfig.getCount());
        assertEquals(expectedConfig.getQueryParam(), actualConfig.getQueryParam());
    }

    /**
     * Tests if expected and actual configuration are identically.
     *
     * @param expectedConfig the expected configuration
     * @param actualConfig the actual configuration
     */
    static void testFieldFacetConfiguration(
        I_CmsSearchConfigurationFacetField expectedConfig,
        I_CmsSearchConfigurationFacetField actualConfig) {

        if (null == expectedConfig) {
            assertNull(actualConfig);
            return;
        }
        assertNotNull(actualConfig);

        assertEquals(expectedConfig.getField(), actualConfig.getField());
        assertEquals(expectedConfig.getName(), actualConfig.getName());
        assertEquals(expectedConfig.getLabel(), actualConfig.getLabel());
        assertEquals(expectedConfig.getMinCount(), actualConfig.getMinCount());
        assertEquals(expectedConfig.getLimit(), actualConfig.getLimit());
        assertEquals(expectedConfig.getPrefix(), actualConfig.getPrefix());
        assertEquals(expectedConfig.getSortOrder(), actualConfig.getSortOrder());
        assertEquals(expectedConfig.modifyFilterQuery("query"), actualConfig.modifyFilterQuery("query"));
        assertEquals(Boolean.valueOf(expectedConfig.getIsAndFacet()), Boolean.valueOf(actualConfig.getIsAndFacet()));
        assertEquals(expectedConfig.getPreSelection(), actualConfig.getPreSelection());
        assertEquals(
            Boolean.valueOf(expectedConfig.getIgnoreAllFacetFilters()),
            Boolean.valueOf(actualConfig.getIgnoreAllFacetFilters()));
        assertEquals(expectedConfig.getIgnoreMaxParamKey(), actualConfig.getIgnoreMaxParamKey());
        assertEquals(expectedConfig.getIgnoreTags(), actualConfig.getIgnoreTags());
        assertEquals(expectedConfig.getParamKey(), actualConfig.getParamKey());
    }

    /**
     * Tests if expected and actual configuration are identically.
     *
     * @param expectedConfig the expected configuration
     * @param actualConfig the actual configuration
     */
    static void testGeneralConfiguration(
        I_CmsSearchConfigurationCommon expectedConfig,
        I_CmsSearchConfigurationCommon actualConfig) {

        if (null == expectedConfig) {
            assertNull(actualConfig);
            return;
        }
        assertNotNull(actualConfig);

        assertEquals(expectedConfig.getSolrCore(), actualConfig.getSolrCore());
        assertEquals(expectedConfig.getSolrIndex(), actualConfig.getSolrIndex());
        assertEquals(
            Boolean.valueOf(expectedConfig.getSearchForEmptyQueryParam()),
            Boolean.valueOf(actualConfig.getSearchForEmptyQueryParam()));
        assertEquals(
            Boolean.valueOf(expectedConfig.getIgnoreQueryParam()),
            Boolean.valueOf(actualConfig.getIgnoreQueryParam()));
        assertEquals(
            Boolean.valueOf(expectedConfig.getIgnoreReleaseDate()),
            Boolean.valueOf(actualConfig.getIgnoreReleaseDate()));
        assertEquals(
            Boolean.valueOf(expectedConfig.getIgnoreExpirationDate()),
            Boolean.valueOf(actualConfig.getIgnoreExpirationDate()));
        assertEquals(expectedConfig.getModifiedQuery("q"), actualConfig.getModifiedQuery("q"));
        assertEquals(expectedConfig.getQueryParam(), actualConfig.getQueryParam());
        assertEquals(expectedConfig.getLastQueryParam(), actualConfig.getLastQueryParam());
        assertEquals(
            Boolean.valueOf(expectedConfig.getEscapeQueryChars()),
            Boolean.valueOf(actualConfig.getEscapeQueryChars()));
        assertEquals(expectedConfig.getReloadedParam(), actualConfig.getReloadedParam());
        assertEquals(expectedConfig.getExtraSolrParams(), actualConfig.getExtraSolrParams());
        Map<String, String> expectedAdditionalParams = expectedConfig.getAdditionalParameters();
        Map<String, String> actualAdditionalParams = actualConfig.getAdditionalParameters();
        assertEquals(expectedAdditionalParams.size(), actualAdditionalParams.size());
        for (String key : expectedAdditionalParams.keySet()) {
            assertEquals(expectedAdditionalParams.get(key), actualAdditionalParams.get(key));
        }

    }

    /**
     * Tests if expected and actual configuration are identically.
     *
     * @param expectedConfig the expected configuration
     * @param actualConfig the actual configuration
     */
    static void testHighlightingConfiguration(
        I_CmsSearchConfigurationHighlighting expectedConfig,
        I_CmsSearchConfigurationHighlighting actualConfig) {

        if (null == expectedConfig) {
            assertNull(actualConfig);
            return;
        }
        assertNotNull(actualConfig);

        assertEquals(expectedConfig.getAlternateHighlightField(), actualConfig.getAlternateHighlightField());
        assertEquals(expectedConfig.getFormatter(), actualConfig.getFormatter());
        assertEquals(expectedConfig.getFragmenter(), actualConfig.getFragmenter());
        assertEquals(expectedConfig.getFragSize(), actualConfig.getFragSize());
        assertEquals(expectedConfig.getHightlightField(), actualConfig.getHightlightField());
        assertEquals(
            expectedConfig.getMaxAlternateHighlightFieldLength(),
            actualConfig.getMaxAlternateHighlightFieldLength());
        assertEquals(expectedConfig.getSimplePost(), actualConfig.getSimplePost());
        assertEquals(expectedConfig.getSimplePre(), actualConfig.getSimplePre());
        assertEquals(expectedConfig.getSnippetsCount(), actualConfig.getSnippetsCount());
        assertEquals(expectedConfig.getUseFastVectorHighlighting(), actualConfig.getUseFastVectorHighlighting());
    }

    /**
     * Tests if expected and actual configuration are identically.
     * @param expectedConfig the expected configuration
     * @param actualConfig the actual configuration
     */
    static void testPaginationConfiguration(
        I_CmsSearchConfigurationPagination expectedConfig,
        I_CmsSearchConfigurationPagination actualConfig) {

        if (null == expectedConfig) {
            assertNull(actualConfig);
            return;
        }
        assertNotNull(actualConfig);

        assertEquals(expectedConfig.getPageParam(), actualConfig.getPageParam());
        assertEquals(expectedConfig.getPageSize(), actualConfig.getPageSize());
        assertEquals(expectedConfig.getPageNavLength(), actualConfig.getPageNavLength());

    }

    /**
     * Tests if expected and actual configuration are identically.
     *
     * @param expectedConfig the expected configuration
     * @param actualConfig the actual configuration
     */
    static void testQueryFacetConfiguration(
        I_CmsSearchConfigurationFacetQuery expectedConfig,
        I_CmsSearchConfigurationFacetQuery actualConfig) {

        if (null == expectedConfig) {
            assertNull(actualConfig);
            return;
        }
        assertNotNull(actualConfig);

        assertEquals(expectedConfig.getName(), actualConfig.getName());
        assertEquals(expectedConfig.getLabel(), actualConfig.getLabel());
        assertEquals(expectedConfig.getMinCount(), actualConfig.getMinCount());
        assertEquals(Boolean.valueOf(expectedConfig.getIsAndFacet()), Boolean.valueOf(actualConfig.getIsAndFacet()));
        assertEquals(expectedConfig.getPreSelection(), actualConfig.getPreSelection());
        assertEquals(
            Boolean.valueOf(expectedConfig.getIgnoreAllFacetFilters()),
            Boolean.valueOf(actualConfig.getIgnoreAllFacetFilters()));
        assertEquals(expectedConfig.getIgnoreMaxParamKey(), actualConfig.getIgnoreMaxParamKey());
        assertEquals(expectedConfig.getIgnoreTags(), actualConfig.getIgnoreTags());
        assertEquals(expectedConfig.getParamKey(), actualConfig.getParamKey());
        assertEquals(expectedConfig.getQueryList(), actualConfig.getQueryList());

    }

    /**
     * Tests if expected and actual configuration are identically.
     *
     * @param expectedConfig the expected configuration
     * @param actualConfig the actual configuration
     */
    static void testRangeFacetConfiguration(
        I_CmsSearchConfigurationFacetRange expectedConfig,
        I_CmsSearchConfigurationFacetRange actualConfig) {

        if (null == expectedConfig) {
            assertNull(actualConfig);
            return;
        }
        assertNotNull(actualConfig);

        assertEquals(expectedConfig.getRange(), actualConfig.getRange());
        assertEquals(expectedConfig.getName(), actualConfig.getName());
        assertEquals(expectedConfig.getLabel(), actualConfig.getLabel());
        assertEquals(expectedConfig.getMinCount(), actualConfig.getMinCount());
        assertEquals(Boolean.valueOf(expectedConfig.getIsAndFacet()), Boolean.valueOf(actualConfig.getIsAndFacet()));
        assertEquals(expectedConfig.getPreSelection(), actualConfig.getPreSelection());
        assertEquals(
            Boolean.valueOf(expectedConfig.getIgnoreAllFacetFilters()),
            Boolean.valueOf(actualConfig.getIgnoreAllFacetFilters()));
        assertEquals(expectedConfig.getIgnoreMaxParamKey(), actualConfig.getIgnoreMaxParamKey());
        assertEquals(expectedConfig.getIgnoreTags(), actualConfig.getIgnoreTags());
        assertEquals(expectedConfig.getEnd(), actualConfig.getEnd());
        assertEquals(expectedConfig.getStart(), actualConfig.getStart());
        assertEquals(expectedConfig.getGap(), actualConfig.getGap());
        assertEquals(Boolean.valueOf(expectedConfig.getHardEnd()), Boolean.valueOf(actualConfig.getHardEnd()));
        assertEquals(expectedConfig.getOther(), actualConfig.getOther());
        assertEquals(expectedConfig.getParamKey(), actualConfig.getParamKey());

    }

    /**
     * Tests if expected and actual configuration are identically.
     * @param expectedConfig the expected configuration
     * @param actualConfig the actual configuration
     */
    static void testSortingConfiguration(
        I_CmsSearchConfigurationSorting expectedConfig,
        I_CmsSearchConfigurationSorting actualConfig) {

        if (null == expectedConfig) {
            assertNull(actualConfig);
            return;
        }
        assertNotNull(actualConfig);

        assertEquals(expectedConfig.getSortParam(), actualConfig.getSortParam());
        assertEquals(expectedConfig.getSortOptions().size(), actualConfig.getSortOptions().size());
        testSortOptionConfiguration(expectedConfig.getDefaultSortOption(), actualConfig.getDefaultSortOption());
        Iterator<I_CmsSearchConfigurationSortOption> expectedIterator = expectedConfig.getSortOptions().iterator();
        Iterator<I_CmsSearchConfigurationSortOption> actualIterator = actualConfig.getSortOptions().iterator();
        while (expectedIterator.hasNext()) {
            testSortOptionConfiguration(expectedIterator.next(), actualIterator.next());
        }
    }

    /**
     * Tests if expected and actual configuration are identically.
     * @param expectedConfig the expected configuration
     * @param actualConfig the actual configuration
     */
    static void testSortOptionConfiguration(
        I_CmsSearchConfigurationSortOption expectedConfig,
        I_CmsSearchConfigurationSortOption actualConfig) {

        if (null == expectedConfig) {
            assertNull(actualConfig);
            return;
        }
        assertNotNull(actualConfig);

        assertEquals(expectedConfig.getParamValue(), actualConfig.getParamValue());
        assertEquals(expectedConfig.getSolrValue(), actualConfig.getSolrValue());
        assertEquals(expectedConfig.getLabel(), actualConfig.getLabel());
    }

}
