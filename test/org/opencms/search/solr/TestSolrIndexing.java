/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.search.documents.CmsExtractionResultCache;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the Solr index process.<p>
 * 
 * @since 8.5.0
 */
public class TestSolrIndexing extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestSolrIndexing(String arg0) {

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
        suite.setName(TestSolrIndexing.class.getName());
        suite.addTest(new TestSolrIndexing("testMultipleIndices"));
        suite.addTest(new TestSolrIndexing("testMultipleLanguages"));
        suite.addTest(new TestSolrIndexing("testExtractionResults"));
        suite.addTest(new TestSolrIndexing("testIndexingPerformance"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("solrtest", "/", "/../org/opencms/search/solr");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * @throws Throwable
     */
    public void testExtractionResults() throws Throwable {

        CmsObject cms = getCmsObject();
        CmsResource res = cms.createSibling(
            "/xmlcontent/link_article_0001.html",
            "/xmlcontent/link2_article_0001.html",
            null);
        // publish the project and update the search index
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();
        I_CmsDocumentFactory factory = OpenCms.getSearchManager().getDocumentFactory(
            CmsSolrDocumentXmlContent.TYPE_XMLCONTENT_SOLR,
            "text/html");
        CmsExtractionResultCache cache = factory.getCache();
        String cacheName = cache.getCacheName(res, Locale.ENGLISH, CmsSolrDocumentXmlContent.TYPE_XMLCONTENT_SOLR);
        CmsExtractionResult result = cache.getCacheObject(cacheName);
        assertNotNull(result);
    }

    /**
     * @throws Throwable
     */
    public void testIndexingPerformance() throws Throwable {

        // TODO: implement
    }

    /**
     * @throws Throwable
     */
    public void testMultipleIndices() throws Throwable {

        // TODO: implement
    }

    /**
     * @throws Throwable
     */
    public void testMultipleLanguages() throws Throwable {

        // TODO: implement
    }
}
