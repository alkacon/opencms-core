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
import org.opencms.file.CmsProperty;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Collections;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the Solr full text search component.<p>
 * 
 * @since 8.5.0
 */
public class TestSolrSearchFulltextSearching extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestSolrSearchFulltextSearching(String arg0) {

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
        suite.setName(TestSolrSearchFulltextSearching.class.getName());
        suite.addTest(new TestSolrSearchFulltextSearching("testLocaleRestriction"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("solrtest", "/");
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
    public void testLocaleRestriction() throws Throwable {

        CmsObject cms = OpenCms.initCmsObject(getCmsObject());

        // use a folder that only contains GERMAN content @see manifest.xml -> locale poperty
        String folderName = "/folder1/subfolder12/subsubfolder121/";

        importTestResource(
            cms,
            "org/opencms/search/pdf-test-112.pdf",
            folderName + "master.pdf",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());

        importTestResource(
            cms,
            "org/opencms/search/pdf-test-112.pdf",
            folderName + "master_de.pdf",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());

        importTestResource(
            cms,
            "org/opencms/search/pdf-test-112.pdf",
            folderName + "master_en.pdf",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());

        importTestResource(
            cms,
            "org/opencms/search/pdf-test-112.pdf",
            folderName + "master_fr.pdf",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());

        // publish the project and update the search index
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllSolrTests.SOLR_ONLINE);

        CmsSolrQuery query = new CmsSolrQuery();
        query.setSearchRoots("/sites/default/folder1/subfolder12/subsubfolder121/");
        query.setResourceTypes("binary");
        CmsSolrResultList result = index.search(cms, query);
        assertEquals(4, result.getNumFound());

        cms.getRequestContext().setLocale(Locale.GERMAN);
        query = new CmsSolrQuery(cms);
        query.setTexts("Testfile", "Intranet");
        result = index.search(cms, query);
        assertEquals(2, result.getNumFound());

        query = new CmsSolrQuery();
        query.setLocales(Collections.singletonList(Locale.GERMAN));
        query.setTexts("Testfile", "Intranet");
        result = index.search(cms, query);
        assertEquals(2, result.getNumFound());

        query = new CmsSolrQuery();
        query.setLocales(Collections.singletonList(Locale.ENGLISH));
        query.setTexts("Testfile", "Intranet");
        result = index.search(cms, query);
        assertEquals(2, result.getNumFound());

        query = new CmsSolrQuery();
        query.setLocales(Collections.singletonList(Locale.FRENCH));
        query.setTexts("Testfile", "Intranet");
        result = index.search(cms, query);
        assertEquals(1, result.getNumFound());

        query = new CmsSolrQuery();
        query.setLocales(null);
        query.setTexts("Testfile", "Intranet");
        result = index.search(cms, query);
        assertEquals(4, result.getNumFound());
    }
}
