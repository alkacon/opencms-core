/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.search.config;

import org.opencms.json.JSONException;
import org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.test.OpenCmsTestRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/** Test cases for the class {@link org.opencms.jsp.search.config.CmsSearchConfigurationPagination}. */
public class TestSearchConfigurationExtension extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/", "/../org/opencms/search/solr");
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

    /**
     * Tests if the configurations are extended as expected.
     * We have just one simple test and do not add edge cases yet.
     *
     * @throws IOException
     * @throws URISyntaxException
     * @throws CmsException
     */
    @Test
    public void testConfigurationExtension() throws IOException, URISyntaxException, CmsException {

        String configString = new String(
            Files.readAllBytes(Paths.get(getClass().getResource("configToExtend.json").toURI())));
        String configExtensionString = new String(
            Files.readAllBytes(Paths.get(getClass().getResource("configExtension.json").toURI())));
        try {
            CmsSearchConfiguration config = new CmsSearchConfiguration(
                new CmsJSONSearchConfigurationParser(configString),
                getCmsObject());
            CmsSearchConfiguration configExtension = new CmsSearchConfiguration(
                new CmsJSONSearchConfigurationParser(configExtensionString),
                getCmsObject());
            // check if the single configurations are as expected
            // config
            assertEquals(1, config.getFieldFacetConfigs().size());
            assertTrue(config.getFieldFacetConfigs().containsKey("field1"));
            assertEquals(1, config.getGeneralConfig().getAdditionalParameters().size());
            assertTrue(config.getGeneralConfig().getAdditionalParameters().containsKey("p1"));
            assertEquals(1, config.getRangeFacetConfigs().size());
            assertTrue(config.getRangeFacetConfigs().containsKey("range1"));
            assertEquals("fq=type:plain", config.getGeneralConfig().getExtraSolrParams());
            //configExtension
            assertEquals(1, configExtension.getFieldFacetConfigs().size());
            assertTrue(configExtension.getFieldFacetConfigs().containsKey("field2"));
            assertEquals(1, configExtension.getGeneralConfig().getAdditionalParameters().size());
            assertTrue(configExtension.getGeneralConfig().getAdditionalParameters().containsKey("p2"));
            assertEquals(1, configExtension.getRangeFacetConfigs().size());
            assertTrue(configExtension.getRangeFacetConfigs().containsKey("range2"));
            assertEquals("fq=test:2", configExtension.getGeneralConfig().getExtraSolrParams());

            // extend the config
            config.extend(configExtension);
            assertEquals(2, config.getFieldFacetConfigs().size());
            assertTrue(config.getFieldFacetConfigs().containsKey("field1"));
            assertTrue(config.getFieldFacetConfigs().containsKey("field2"));
            assertEquals(2, config.getGeneralConfig().getAdditionalParameters().size());
            assertTrue(config.getGeneralConfig().getAdditionalParameters().containsKey("p1"));
            assertTrue(config.getGeneralConfig().getAdditionalParameters().containsKey("p2"));
            assertEquals(2, config.getRangeFacetConfigs().size());
            assertTrue(config.getRangeFacetConfigs().containsKey("range1"));
            assertTrue(config.getRangeFacetConfigs().containsKey("range2"));
            assertEquals("fq=type:plain&fq=test:2", config.getGeneralConfig().getExtraSolrParams());

        } catch (JSONException e) {
            e.printStackTrace();
            assertTrue(false);
        }

    }

}
