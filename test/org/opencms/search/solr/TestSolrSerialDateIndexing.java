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

package org.opencms.search.solr;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.io.IOException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSolrSerialDateIndexing extends OpenCmsTestCase {

    /** Name of the module to import. */
    protected static final String TEST_MODULE_NAME = "org.opencms.test.modules.solr.serialdate";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestSolrSerialDateIndexing(String arg0) {

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
        suite.setName(TestSolrSerialDateIndexing.class.getName());

        suite.addTest(new TestSolrSerialDateIndexing("testIndexingAndDeletion"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() throws CmsException, IOException, InterruptedException {

                CmsObject cms = setupOpenCms(null, null, "/../org/opencms/search/solr");
                // disable all lucene indexes
                for (String indexName : OpenCms.getSearchManager().getIndexNames()) {
                    if (!indexName.equalsIgnoreCase(AllTests.SOLR_ONLINE)) {
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

    public void testIndexingAndDeletion() throws Exception {

        String path = "/sites/default/content/sdt_00001.xml";
        echo("Testing if the content with path " + path + " is indexed correctly");
        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/");
        importModule(cms, TEST_MODULE_NAME);

        String query = "q=*:*&fq=path:\"" + path + "\"";
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE);
        CmsSolrResultList results = index.search(cms, query);

        assertEquals("The content should be indexed 100 times", 100, results.getNumFound());

        cms.lockResource(path);
        cms.deleteResource(path, CmsResource.DELETE_REMOVE_SIBLINGS);
        OpenCms.getPublishManager().publishResource(cms, path);
        OpenCms.getPublishManager().waitWhileRunning();

        results = index.search(cms, query);
        assertEquals("The content should not be found anymore, since it was deleted.", 0, results.getNumFound());

        importModule(cms, TEST_MODULE_NAME);
        results = index.search(cms, query);
        assertEquals("The content should be indexed 100 times", 100, results.getNumFound());

    }

}
