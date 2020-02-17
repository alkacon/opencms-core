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

import org.opencms.file.CmsObject;
import org.opencms.jsp.search.config.CmsSearchConfiguration;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Test cases for the plain query search configuration parser. */
public class TestPlainQuerySearchConfigurationParser extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestPlainQuerySearchConfigurationParser(String arg0) {

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
        suite.addTest(new TestPlainQuerySearchConfigurationParser("testSpecialParamExtraction"));
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
     * Tests the extraction of the core, index and maxresults parameters.
     * @throws CmsException if the cms object cannot be retrieved.
     */
    @org.junit.Test
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
