/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.search;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.lucene.document.Document;

/**
 * Unit test for special search features added for OpenCms 7.5.<p>
 */
public class TestCmsSearchSpecialFeatures extends OpenCmsTestCase {

    /** Name of the search index created using API. */
    public static final String INDEX_SPECIAL = "Special Test Index";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSearchSpecialFeatures(String arg0) {

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
        suite.setName(TestCmsSearchSpecialFeatures.class.getName());

        suite.addTest(new TestCmsSearchSpecialFeatures("testSearchIndexSetup"));
        suite.addTest(new TestCmsSearchSpecialFeatures("testIncrementalIndexUpdate"));
        suite.addTest(new TestCmsSearchSpecialFeatures("testLazyContentFields"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Creates a new search index setup for this test.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testSearchIndexSetup() throws Exception {

        CmsSearchIndex searchIndex = new CmsSearchIndex(INDEX_SPECIAL);
        searchIndex.setProjectName("Online");
        searchIndex.setLocale(Locale.ENGLISH);
        searchIndex.setRebuildMode(CmsSearchIndex.REBUILD_MODE_AUTO);
        // available pre-configured in the test configuration files opencms-search.xml
        searchIndex.addSourceName("source1");
        searchIndex.addConfigurationParameter(CmsSearchIndex.BACKUP_REINDEXING, "true");

        // initialize the new index
        searchIndex.initialize();

        // add the search index to the manager
        OpenCms.getSearchManager().addSearchIndex(searchIndex);

        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        // this call does not throws the rebuild index event
        OpenCms.getSearchManager().rebuildIndex(INDEX_SPECIAL, report);
        OpenCms.getSearchManager().rebuildIndex(INDEX_SPECIAL, report);

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        searchBean.init(getCmsObject());
        searchBean.setIndex(INDEX_SPECIAL);
        searchBean.setQuery(">>SearchEgg1<<");

        // assert one file is found in the default site     
        searchResult = searchBean.getSearchResult();
        assertEquals(1, searchResult.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", (searchResult.get(0)).getPath());
    }

    /**
     * Tests incremental index updates with the new content blob feature.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testIncrementalIndexUpdate() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing search incremental index update - new content blob feature");

        // create test folder
        cms.createResource("/test/", CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, null);
        cms.unlockResource("/test/");

        String fileName = "/test/master.pdf";

        // create master resource
        importTestResource(
            cms,
            "org/opencms/search/extractors/test1.pdf",
            fileName,
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.EMPTY_LIST);

        // create 5 siblings
        for (int i = 0; i < 5; i++) {
            cms.createSibling(fileName, "/test/sibling" + i + ".pdf", null);
        }

        // publish the project and update the search index
        I_CmsReport report = new CmsShellReport(cms.getRequestContext().getLocale());
        OpenCms.getPublishManager().publishProject(cms, report);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.lockResource(fileName);
        cms.writePropertyObject(fileName, new CmsProperty(
            CmsPropertyDefinition.PROPERTY_TITLE,
            "Title of the PDF",
            null));

        // publish the project and update the search index
        report = new CmsShellReport(cms.getRequestContext().getLocale());
        OpenCms.getPublishManager().publishProject(cms, report);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Ensures the content and content blob fields are loaded lazy.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testLazyContentFields() throws Exception {

        echo("Testing lazy status of content fields in search index");

        String fileName = "/sites/default/test/master.pdf";

        CmsSearchIndex searchIndex = OpenCms.getSearchManager().getIndex(INDEX_SPECIAL);
        Document doc = searchIndex.getDocument(CmsSearchField.FIELD_PATH, fileName);

        assertNotNull("Document '" + fileName + "' not found", doc);
        assertNotNull("No 'content' field available", doc.getFieldable(CmsSearchField.FIELD_CONTENT));
        assertTrue("Content field not lazy", doc.getFieldable(CmsSearchField.FIELD_CONTENT).isLazy());
        assertNotNull("No 'content blob' field available", doc.getFieldable(CmsSearchField.FIELD_CONTENT_BLOB));
        assertTrue("Content blob field not lazy", doc.getFieldable(CmsSearchField.FIELD_CONTENT_BLOB).isLazy());
    }
}