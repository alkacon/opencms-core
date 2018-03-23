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

import org.opencms.acacia.shared.I_CmsSerialDateValue;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Test cases for indexing of contents using serial dates. */
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
        suite.addTest(new TestSolrSerialDateIndexing("testIndexedDates"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms(null, null, "/../org/opencms/search/solr");
                // disable all lucene indexes
                for (String indexName : OpenCms.getSearchManager().getIndexNames()) {
                    if (!indexName.equalsIgnoreCase(AllTests.SOLR_ONLINE)) {
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

    /** Tests if for a series content the correct special dates are indexed.
     * @throws Exception if module import or read/write fails.
     */
    public void testIndexedDates() throws Exception {

        String path = "/sites/default/content/sdt_00001.xml";
        echo("Testing if the content with path " + path + " is indexed correctly");
        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/");
        importModule(cms, TEST_MODULE_NAME);

        String query = "q=*:*&fq=path:\"" + path + "\"";
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE);
        CmsSolrResultList results = index.search(cms, query);

        assertEquals("The content should be indexed 5 times", 5, results.getNumFound());

        Set<Long> expectedStartDates = new HashSet<Long>();
        long startDate = 1491202800000L;
        for (int i = 0; i < 5; i++) {
            expectedStartDates.add(Long.valueOf(startDate));
            startDate += I_CmsSerialDateValue.DAY_IN_MILLIS;
        }
        Set<Long> expectedEndDates = new HashSet<Long>();
        long endDate = 1491231600000L;
        for (int i = 0; i < 5; i++) {
            expectedEndDates.add(Long.valueOf(endDate));
            endDate += I_CmsSerialDateValue.DAY_IN_MILLIS;
        }

        Set<Long> actualStartDates = new HashSet<Long>();
        Set<Long> actualEndDates = new HashSet<Long>();
        Set<Long> actualTillCurrentDates = new HashSet<Long>();

        String startDateField = CmsSearchField.FIELD_INSTANCEDATE + CmsSearchField.FIELD_POSTFIX_DATE;
        String endDateField = CmsSearchField.FIELD_INSTANCEDATE_END + CmsSearchField.FIELD_POSTFIX_DATE;
        String currentTillDateField = CmsSearchField.FIELD_INSTANCEDATE_CURRENT_TILL
            + CmsSearchField.FIELD_POSTFIX_DATE;
        Date date;
        for (CmsSearchResource result : results) {
            date = result.getDateField(startDateField);
            assertNotNull(date);
            actualStartDates.add(Long.valueOf(date.getTime()));
            date = result.getDateField(endDateField);
            assertNotNull(date);
            actualEndDates.add(Long.valueOf(date.getTime()));
            date = result.getDateField(currentTillDateField);
            assertNotNull(date);
            actualTillCurrentDates.add(Long.valueOf(date.getTime()));
        }
        assertEquals(expectedStartDates, actualStartDates);
        assertEquals(expectedEndDates, actualEndDates);
        assertEquals(expectedEndDates, actualTillCurrentDates);

        actualEndDates.clear();
        actualStartDates.clear();
        actualTillCurrentDates.clear();

        path = "/sites/default/content/sdt_00002.xml";
        echo("Testing if the content with path " + path + " is indexed correctly");
        query = "q=*:*&fq=path:\"" + path + "\"";
        index = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE);
        results = index.search(cms, query);
        for (CmsSearchResource result : results) {
            date = result.getDateField(startDateField);
            assertNotNull(date);
            actualStartDates.add(Long.valueOf(date.getTime()));
            date = result.getDateField(endDateField);
            assertNotNull(date);
            actualEndDates.add(Long.valueOf(date.getTime()));
            date = result.getDateField(currentTillDateField);
            assertNotNull(date);
            actualTillCurrentDates.add(Long.valueOf(date.getTime()));
        }
        assertEquals(expectedStartDates, actualStartDates);
        assertEquals(expectedEndDates, actualEndDates);
        assertEquals(expectedStartDates, actualTillCurrentDates);

    }

    /** Tests if a series content is indexed correctly many times and if all entries are removed, if the content is removed.
     * @throws Exception if module import or read/write fails.
     */
    public void testIndexingAndDeletion() throws Exception {

        String path = "/sites/default/content/sdt_00001.xml";
        echo("Testing if the content with path " + path + " is indexed correctly");
        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/");
        importModule(cms, TEST_MODULE_NAME);

        String query = "q=*:*&fq=path:\"" + path + "\"";
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE);
        CmsSolrResultList results = index.search(cms, query);

        assertEquals("The content should be indexed 5 times", 5, results.getNumFound());

        cms.lockResource(path);
        cms.deleteResource(path, CmsResource.DELETE_REMOVE_SIBLINGS);
        OpenCms.getPublishManager().publishResource(cms, path);
        OpenCms.getPublishManager().waitWhileRunning();

        results = index.search(cms, query);
        assertEquals("The content should not be found anymore, since it was deleted.", 0, results.getNumFound());

        importModule(cms, TEST_MODULE_NAME);
        results = index.search(cms, query);
        assertEquals("The content should be indexed 5 times", 5, results.getNumFound());

    }

}
