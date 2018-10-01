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

package org.opencms.jsp.search.result;

import org.opencms.file.CmsResourceFilter;
import org.opencms.json.JSONException;
import org.opencms.jsp.search.config.CmsSearchConfiguration;
import org.opencms.jsp.search.config.I_CmsSearchConfiguration;
import org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser;
import org.opencms.jsp.search.controller.CmsSearchController;
import org.opencms.jsp.search.controller.I_CmsSearchControllerCommon;
import org.opencms.jsp.search.controller.I_CmsSearchControllerMain;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Test cases for the class {@link org.opencms.jsp.search.config.CmsSearchConfigurationPagination}. */
public class TestSearchStateParameters extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestSearchStateParameters(String arg0) {

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
        suite.addTest(new TestSearchStateParameters("testCheckAndUncheckFacetItems"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/", "/../org/opencms/search/solr");
                // disable all lucene indexes
                for (String indexName : OpenCms.getSearchManager().getIndexNames()) {
                    if (!indexName.equalsIgnoreCase(CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE)) {
                        CmsSearchIndex index = OpenCms.getSearchManager().getIndex(indexName);
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
     * Test if adding and removing facet items in the state parameters works.
     *
     * @throws CmsException ...
     * @throws IOException ...
     * @throws URISyntaxException ...
     * @throws JSONException ...
     */
    @org.junit.Test
    public void testCheckAndUncheckFacetItems() throws CmsException, IOException, URISyntaxException, JSONException {

        I_CmsSearchResultWrapper result = search();

        String cat1_0 = "cat1";
        String cat1_1 = "cat123".substring(0, 4);

        I_CmsSearchStateParameters stateParams = result.getStateParameters();
        assertFalse(stateParams.toString().contains("facet_Keywords_prop="));
        stateParams = stateParams.getCheckFacetItem().get("Keywords_prop").get(cat1_0);
        assertTrue(stateParams.toString().contains("facet_Keywords_prop=cat1"));
        stateParams = stateParams.getCheckFacetItem().get("Keywords_prop").get("cat2");
        assertTrue(stateParams.toString().contains("facet_Keywords_prop=cat1"));
        assertTrue(stateParams.toString().contains("facet_Keywords_prop=cat2"));
        stateParams = stateParams.getUncheckFacetItem().get("Keywords_prop").get(cat1_1);
        assertFalse(stateParams.toString().contains("facet_Keywords_prop=cat1"));
        assertTrue(stateParams.toString().contains("facet_Keywords_prop=cat2"));
        stateParams = stateParams.getUncheckFacetItem().get("Keywords_prop").get("cat2");
        assertFalse(stateParams.toString().contains("facet_Keywords_prop="));

    }

    /**
     * Performs a search according to the config.json search configuration.
     * @return the search result
     * @throws CmsException ...
     * @throws IOException ...
     * @throws URISyntaxException ...
     * @throws JSONException ...
     */
    private I_CmsSearchResultWrapper search() throws CmsException, IOException, URISyntaxException, JSONException {

        String configString = new String(Files.readAllBytes(Paths.get(getClass().getResource("config.json").toURI())));
        I_CmsSearchConfiguration config = new CmsSearchConfiguration(
            new CmsJSONSearchConfigurationParser(configString));

        I_CmsSearchControllerMain searchController = new CmsSearchController(config);

        String indexName = searchController.getCommon().getConfig().getSolrIndex();
        // try to use configured index
        CmsSolrIndex index = null;
        if ((indexName != null) && !indexName.trim().isEmpty()) {
            index = OpenCms.getSearchManager().getIndexSolr(indexName);
        }
        // if not successful, use the following default
        if (index == null) {
            index = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE);
        }

        Map<String, String[]> parameterMap = new HashMap<>();
        searchController.updateFromRequestParameters(parameterMap, false);
        I_CmsSearchControllerCommon common = searchController.getCommon();

        CmsSearchResultWrapper searchResult = null;
        // Do not search for empty query, if configured
        if (common.getState().getQuery().isEmpty()
            && (!common.getConfig().getIgnoreQueryParam() && !common.getConfig().getSearchForEmptyQueryParam())) {
            searchResult = new CmsSearchResultWrapper(searchController, null, null, getCmsObject(), null);
        } else {
            CmsSolrQuery query = new CmsSolrQuery(null, null);
            searchController.addQueryParts(query);
            // use "complicated" constructor to allow more than 50 results -> set ignoreMaxResults to true
            // also set resource filter to allow for returning unreleased/expired resources if necessary.
            CmsSolrResultList solrResultList = index.search(
                getCmsObject(),
                query.clone(), // use a clone of the query, since the search function manipulates the query (removes highlighting parts), but we want to keep the original one.
                true,
                CmsResourceFilter.IGNORE_EXPIRATION);
            searchResult = new CmsSearchResultWrapper(searchController, solrResultList, query, getCmsObject(), null);
        }
        return searchResult;
    }

}
