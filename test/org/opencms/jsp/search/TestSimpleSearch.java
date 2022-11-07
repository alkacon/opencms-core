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

package org.opencms.jsp.search;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.search.config.CmsSearchConfiguration;
import org.opencms.jsp.search.config.I_CmsSearchConfiguration;
import org.opencms.jsp.search.config.parser.CmsSimpleSearchConfigurationParser;
import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigParserUtils;
import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigurationBean;
import org.opencms.jsp.search.controller.CmsSearchController;
import org.opencms.jsp.search.controller.I_CmsSearchController;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;

import java.util.HashSet;
import java.util.Set;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Test cases for the simple search configuration via contents of type "list_config". */
public class TestSimpleSearch extends OpenCmsTestCase {

    /** The VFS folder where the list contents are placed in. */
    private static final String LIST_BASE_FOLDER = "/system/modules/org.opencms.test.modules.listtype/resources/lists/";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestSimpleSearch(String arg0) {

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
        suite.addTest(new TestSimpleSearch("testFolderAndCategoryRestrictions"));

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
     * Executes several searches with list configurations that differ in the
     * combined category folder restrictions and examines if the results are as expected.
     * @throws CmsException thrown if something unexpected goes wrong.
     */
    @org.junit.Test
    public void testFolderAndCategoryRestrictions() throws CmsException {

        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        importModule(cms, "org.opencms.test.modules.listtype");

        // All with category 1
        Set<String> result = searchForConfig(cms, "list_00001.xml");
        assertEquals(6, result.size());
        assertTrue(result.contains("a1.xml"));
        assertTrue(result.contains("a2.xml"));
        assertTrue(result.contains("a3.xml"));
        assertTrue(result.contains("b1.xml"));
        assertTrue(result.contains("b2.xml"));
        assertTrue(result.contains("b3.xml"));

        // All with category 1 and ((folder a or b) and (category 2 or 3))
        result = searchForConfig(cms, "list_00002.xml");
        assertEquals(4, result.size());
        assertTrue(result.contains("a1.xml"));
        assertTrue(result.contains("a2.xml"));
        assertTrue(result.contains("b1.xml"));
        assertTrue(result.contains("b3.xml"));

        // All with category 1 and (folder a and category 2 and 3)
        result = searchForConfig(cms, "list_00003.xml");
        assertEquals(1, result.size());
        assertTrue(result.contains("a1.xml"));

        // All with category 1 and ((folder a or b) and (category 2 or 3))
        result = searchForConfig(cms, "list_00004.xml");
        assertEquals(6, result.size());
        assertTrue(result.contains("a1.xml"));
        assertTrue(result.contains("a2.xml"));
        assertTrue(result.contains("a3.xml"));
        assertTrue(result.contains("b1.xml"));
        assertTrue(result.contains("b2.xml"));
        assertTrue(result.contains("b3.xml"));

        // All in folder a and folder b
        result = searchForConfig(cms, "list_00005.xml");
        assertEquals(0, result.size());

    }

    /**
     * Helper to read the list configuration and perform the search.
     * @param cms the context
     * @param listName the filename of the list configuration to use for the search.
     * @return the list of filenames of the found resources.
     * @throws CmsException if something unexpected goes wrong, e.g., the list config does not exist.
     */
    private Set<String> searchForConfig(CmsObject cms, String listName) throws CmsException {

        CmsResource listConfig = cms.readResource(CmsStringUtil.joinPaths(LIST_BASE_FOLDER, listName));
        CmsConfigurationBean listConfigBean = CmsConfigParserUtils.parseListConfiguration(cms, listConfig);
        I_CmsSearchConfiguration config = new CmsSearchConfiguration(
            CmsSimpleSearchConfigurationParser.createInstanceWithNoJsonConfig(getCmsObject(), listConfigBean),
            cms);
        I_CmsSearchController controller = new CmsSearchController(config);
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE);
        CmsSolrQuery query = new CmsSolrQuery();
        controller.addQueryParts(query, cms);
        CmsSolrResultList searchResult = index.search(
            cms,
            query.clone(),
            true,
            null,
            false,
            null,
            config.getGeneralConfig().getMaxReturnedResults());
        Set<String> result = new HashSet<>(searchResult.size());
        for (CmsSearchResource res : searchResult) {
            result.add(res.getName());
        }
        return result;
    }

}
