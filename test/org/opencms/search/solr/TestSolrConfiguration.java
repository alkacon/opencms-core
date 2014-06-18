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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.documents.CmsExtractionResultCache;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;

/**
 * Tests the Solr configuration.<p>
 * 
 * @since 8.5.0
 */
public class TestSolrConfiguration extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestSolrConfiguration(String arg0) {

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
        suite.setName(TestSolrConfiguration.class.getName());
        suite.addTest(new TestSolrConfiguration("testPermissionHandling"));
        suite.addTest(new TestSolrConfiguration("testExtractionResults"));
        // suite.addTest(new TestSolrConfiguration("testIndexingPerformance"));
        // suite.addTest(new TestSolrConfiguration("testMultipleIndices"));
        // suite.addTest(new TestSolrConfiguration("testMultipleLanguages"));
        suite.addTest(new TestSolrConfiguration("testReindexPublishedSiblings"));
        suite.addTest(new TestSolrConfiguration("testPostProcessor"));
        suite.addTest(new TestSolrConfiguration("testShutDown"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("solrtest", "", "/../org/opencms/search/solr");
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

    /**
     * @throws Throwable
     */
    public void testExtractionResults() throws Throwable {

        echo("Testing extraction results");
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

    /**
     * @throws Throwable
     */
    public void testPermissionHandling() throws Throwable {

        echo("Testing search for permission check by comparing result counts");
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);

        CmsSolrQuery squery = new CmsSolrQuery(getCmsObject(), null);
        squery.setSearchRoots("/sites/default/");
        squery.setRows(new Integer(100));
        CmsSolrResultList results = index.search(getCmsObject(), squery);
        AllTests.printResults(getCmsObject(), results, true);
        assertEquals(56, results.getNumFound());

        CmsObject cms = OpenCms.initCmsObject(getCmsObject(), new CmsContextInfo("test1"));
        results = index.search(cms, squery);
        AllTests.printResults(cms, results, false);
        assertEquals(50, results.getNumFound());

        cms = OpenCms.initCmsObject(getCmsObject(), new CmsContextInfo("test2"));
        results = index.search(cms, squery);
        AllTests.printResults(cms, results, true);
        assertEquals(52, results.getNumFound());
    }

    /**
     * Tests the CmsSearch with folder names with upper case letters.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testPostProcessor() throws Exception {

        echo("Testing Solr link processor");
        CmsObject cms = getCmsObject();
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        String query = "q=+text:>>SearchEgg1<<";
        CmsSolrResultList results = index.search(cms, query);
        CmsSearchResource res = AllTests.getByPath(results, "/sites/default/xmlcontent/article_0001.html");
        String link = res.getDocument().getFieldValueAsString("link");
        assertEquals("/data/opencms/xmlcontent/article_0001.html", link);
    }

    /**
     * Test result count for changed content of two siblings.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testReindexPublishedSiblings() throws Throwable {

        echo("Test result count for changed content of two siblings");
        CmsObject cms = getCmsObject();

        // create a folder with two siblings and publish them together
        String folder = "/reindexPublishedSiblings/";
        cms.createResource(folder, CmsResourceTypeFolder.getStaticTypeId());
        String brother = folder + "test_brother.txt";
        CmsProperty firstTitleProperty = new CmsProperty(
            CmsPropertyDefinition.PROPERTY_TITLE,
            "BROTHER AND SISTER",
            null);
        List<CmsProperty> props = new ArrayList<CmsProperty>();
        props.add(firstTitleProperty);
        CmsResource resource = cms.createResource(
            brother,
            CmsResourceTypePlain.getStaticTypeId(),
            "Solr Enterprise Serach".getBytes(),
            props);
        String sister = folder + "test_sister.txt";
        cms.createSibling(brother, sister, props);
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        // modify and publish only the source
        CmsFile file = cms.readFile(resource);
        file.setContents("OpenCms Enterprise Content Management System".getBytes());
        cms.lockResource(file);
        cms.writeFile(file);
        OpenCms.getPublishManager().publishResource(cms, brother, false, null);
        OpenCms.getPublishManager().waitWhileRunning();

        // create a query matching the new content
        CmsSolrQuery query = new CmsSolrQuery(getCmsObject(), null);
        query.setQuery("\"OpenCms Enterprise Content Management System\"");
        List<Locale> locles = Collections.emptyList();
        query.setLocales(locles);
        query.setSearchRoots("/");
        query.setRows(new Integer(10));

        // Offline and Online both siblings should be found for the new content
        CmsSolrIndex oindex = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);
        CmsSolrResultList rl = oindex.search(cms, query);
        assertEquals("Both siblings must be found, they have the same content.", 2, rl.size());

        CmsSearchResource brotherDoc = rl.get(0);
        CmsSearchResource sisterDoc = rl.get(1);

        assertEquals(
            "Brother must be there",
            "/sites/default/reindexPublishedSiblings/test_brother.txt",
            brotherDoc.getRootPath());
        assertEquals(
            "Sister must be there",
            "/sites/default/reindexPublishedSiblings/test_sister.txt",
            sisterDoc.getRootPath());

        assertEquals(
            "The content must be",
            "OpenCms Enterprise Content Management System",
            brotherDoc.getField("content"));

        assertEquals(
            "The content of the found documents must be equal",
            brotherDoc.getField("content"),
            sisterDoc.getField("content"));

        query.setQuery("\"Solr Enterprise Serach\"");
        CmsSolrResultList rl2 = oindex.search(cms, query);
        assertEquals("Old content must not be found anymore", 0, rl2.size());

        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
        String sisterContent = new String(cms.readFile(sister).getContents());
        String brotherContent = new String(cms.readFile(brother).getContents());
        assertEquals(
            "Sister and brother must have the same content in the online project",
            sisterContent,
            brotherContent);
    }

    /**
     * Tests shutting down Solr.<p>
     * 
     * @throws Throwable
     */
    public void testShutDown() throws Throwable {

        echo("Testing Solr shutdown");
        CmsSolrIndex index = new CmsSolrIndex(AllTests.INDEX_TEST);
        index.setProject("Offline");
        index.setLocale(Locale.GERMAN);
        index.setRebuildMode(CmsSearchIndex.REBUILD_MODE_AUTO);
        index.setFieldConfigurationName("solr_fields");
        index.addSourceName("solr_source2");
        OpenCms.getSearchManager().addSearchIndex(index);
        OpenCms.getSearchManager().rebuildIndex(AllTests.INDEX_TEST, new CmsShellReport(Locale.ENGLISH));
        for (int i = 0; i < 250; i++) {
            index.search(getCmsObject(), "q=*:*");
        }

        // shut down
        CoreContainer container = ((EmbeddedSolrServer)index.m_solr).getCoreContainer();
        for (SolrCore core : container.getCores()) {
            echo("Open count for core: " + core.getName() + ": " + core.getOpenCount());
        }
        container.shutdown();

        // wait for a moment
        Thread.sleep(500);

        // success ?
        CmsFileUtil.purgeDirectory(new File(index.getPath()));
        assertTrue(
            "The index folder must be deleted, otherwise some index lock may have prevent a successful purge.",
            !new File(index.getPath()).exists());
    }
}
