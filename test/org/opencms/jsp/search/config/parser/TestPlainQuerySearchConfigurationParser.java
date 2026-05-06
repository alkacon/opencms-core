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

package org.opencms.jsp.search.config.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import static org.junit.jupiter.api.Assertions.*;

import org.opencms.file.CmsObject;
import org.opencms.jsp.search.config.CmsSearchConfiguration;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.test.OpenCmsJupiterTestCase;
import org.opencms.test.OpenCmsTestProperties;


/** Test cases for the plain query search configuration parser. */
@org.junit.jupiter.api.TestInstance(org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS)
@org.junit.jupiter.api.TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class TestPlainQuerySearchConfigurationParser extends OpenCmsJupiterTestCase {

    /**
     * @see org.opencms.test.OpenCmsJupiterTestCase#getImportFolder()
     */
    @Override
    protected String getImportFolder() {

        return "simpletest";
    }

    /**
     * @see org.opencms.test.OpenCmsJupiterTestCase#getTargetFolder()
     */
    @Override
    protected String getTargetFolder() {

        return "/";
    }

    /**
     * @see org.opencms.test.OpenCmsJupiterTestCase#getSpecialConfigFolder()
     */
    @Override
    protected String getSpecialConfigFolder() {

        return "/../org/opencms/search/solr";
    }

    /**
     * Disables all Lucene indexes to match the legacy suite wrapper setup.<p>
     */
    @org.junit.jupiter.api.BeforeAll
    public void disableIndexes() {

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
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    

    /**
     * Tests the extraction of the core, index and maxresults parameters.
     * @throws CmsException if the cms object cannot be retrieved.
     */
        @Test
    @Order(1)
    public void testSpecialParamExtraction() throws CmsException {

        CmsObject cms = getCmsObject();
        String configString = "a=foo&b=bar&fl=id,path";
        CmsSearchConfiguration config = new CmsSearchConfiguration(
            new CmsPlainQuerySearchConfigurationParser(configString),
            cms);
        assertEquals(configString, config.getGeneralConfig().getExtraSolrParams());

        String configString2 = "a=foo&b=bar&fl=id,path&core=test";
        config = new CmsSearchConfiguration(new CmsPlainQuerySearchConfigurationParser(configString2), cms);
        assertEquals(configString, config.getGeneralConfig().getExtraSolrParams());
        assertEquals("test", config.getGeneralConfig().getSolrCore());

        String configString3 = "a=foo&core=test&b=bar&fl=id,path";
        config = new CmsSearchConfiguration(new CmsPlainQuerySearchConfigurationParser(configString3), cms);
        assertEquals(configString, config.getGeneralConfig().getExtraSolrParams());
        assertEquals("test", config.getGeneralConfig().getSolrCore());

        String configString4 = "core=test&a=foo&b=bar&fl=id,path";
        config = new CmsSearchConfiguration(new CmsPlainQuerySearchConfigurationParser(configString4), cms);
        assertEquals(configString, config.getGeneralConfig().getExtraSolrParams());
        assertEquals("test", config.getGeneralConfig().getSolrCore());

        String configString5 = "core=test&a=foo&index=Test Index&b=bar&fl=id,path";
        config = new CmsSearchConfiguration(new CmsPlainQuerySearchConfigurationParser(configString5), cms);
        assertEquals(configString, config.getGeneralConfig().getExtraSolrParams());
        assertEquals("test", config.getGeneralConfig().getSolrCore());
        assertEquals("Test Index", config.getGeneralConfig().getSolrIndex());
        assertEquals(
            OpenCms.getSearchManager().getIndexSolr("Test Index").getMaxProcessedResults(),
            config.getGeneralConfig().getMaxReturnedResults());

        String configString6 = "core=test&a=foo&index=Test Index&b=bar&fl=id,path&maxresults=123";
        config = new CmsSearchConfiguration(new CmsPlainQuerySearchConfigurationParser(configString6), cms);
        assertEquals(configString, config.getGeneralConfig().getExtraSolrParams());
        assertEquals("test", config.getGeneralConfig().getSolrCore());
        assertEquals("Test Index", config.getGeneralConfig().getSolrIndex());
        assertEquals(123, config.getGeneralConfig().getMaxReturnedResults());
    }
}
