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
import org.opencms.util.CmsRequestUtil;

import java.util.Collections;
import java.util.List;
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
        // suite.addTest(new TestSolrSearchFulltextSearching("testLocaleRestriction"));
        suite.addTest(new TestSolrSearchFulltextSearching("testSolrQueryDefaults"));
        suite.addTest(new TestSolrSearchFulltextSearching("testSolrQueryParameterStrength"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("systemtest", "/");
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
        query = new CmsSolrQuery(cms, null);
        query.setText("Testfile Intranet");
        result = index.search(cms, query);
        assertEquals(2, result.getNumFound());

        query = new CmsSolrQuery();
        query.setLocales(Collections.singletonList(Locale.GERMAN));
        query.setText("Testfile Intranet");
        result = index.search(cms, query);
        assertEquals(2, result.getNumFound());

        query = new CmsSolrQuery();
        query.setLocales(Collections.singletonList(Locale.ENGLISH));
        query.setText("Testfile Intranet");
        result = index.search(cms, query);
        assertEquals(2, result.getNumFound());

        query = new CmsSolrQuery();
        query.setLocales(Collections.singletonList(Locale.FRENCH));
        query.setText("Testfile Intranet");
        result = index.search(cms, query);
        assertEquals(1, result.getNumFound());

        query = new CmsSolrQuery();
        List<Locale> l = Collections.emptyList();
        query.setLocales(l);
        query.setText("Testfile Intranet");
        result = index.search(cms, query);
        assertEquals(4, result.getNumFound());
    }

    /**
     * @throws Throwable
     */
    public void testSolrQueryDefaults() throws Throwable {

        // test default query
        String defaultQuery = "q=*:*&fl=*,score&qt=dismax&rows=10";
        CmsSolrQuery query = new CmsSolrQuery();
        assertEquals(defaultQuery, query.toString());

        // test creating default query by String
        query = new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(defaultQuery));
        assertEquals(defaultQuery, query.toString());

        // test creating default query by String
        String defaultContextQuery = "q=*:*&fl=*,score&qt=dismax&rows=10&fq=con_locales:en&fq=parent-folders:/sites/default/";
        query = new CmsSolrQuery(getCmsObject(), null);
        assertEquals(defaultContextQuery, query.toString());

        // test creating default context query by String
        query = new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(defaultContextQuery));
        assertEquals(defaultContextQuery, query.toString());

        // test creating default context query by context and String
        query = new CmsSolrQuery(getCmsObject(), CmsRequestUtil.createParameterMap(defaultContextQuery));
        assertEquals(defaultContextQuery, query.toString());
    }

    /**
     * @throws Throwable
     */
    public void testSolrQueryParameterStrength() throws Throwable {

        String defaultContextQuery = "q=*:*&fl=*,score&qt=dismax&rows=10&fq=con_locales:en&fq=parent-folders:/sites/default/";
        String modifiedContextQuery = "q=*:*&fl=*,score&qt=dismax&rows=10&fq=con_locales:en&fq=parent-folders:/";
        String modifiedLocales = "q=*:*&fl=*,score&qt=dismax&rows=10&fq=con_locales:(de OR fr)&fq=parent-folders:/";

        // members should be stronger than request context
        CmsSolrQuery query = new CmsSolrQuery(getCmsObject(), null);
        assertEquals(defaultContextQuery, query.toString());
        query.setSearchRoots("/");
        assertEquals(modifiedContextQuery, query.toString());
        query.setLocales(Locale.GERMAN, Locale.FRENCH, Locale.ENGLISH);
        query.setLocales(Locale.GERMAN, Locale.FRENCH);
        assertEquals(modifiedLocales, query.toString());

        // parameters should be stronger than request context
        query = new CmsSolrQuery(getCmsObject(), CmsRequestUtil.createParameterMap("fq=parent-folders:/"));
        assertEquals(modifiedContextQuery, query.toString());

        // parameters should be stronger than request context and members
        query = new CmsSolrQuery(
            getCmsObject(),
            CmsRequestUtil.createParameterMap("q=test&fq=parent-folders:/&fq=con_locales:fr&fl=content_fr&rows=50&qt=dismax&fq=type:v8news"));
        query.setText("test");
        query.setTextSearchFields("pla");
        query.setLocales(Locale.GERMAN);
        query.setFields("pla,plub");
        query.setRows(new Integer(1000));
        query.setQueryType("lucene");
        query.setResourceTypes("article");
        String ex = "q=test&fl=content_fr,path,type,id&qt=dismax&rows=50&fq=parent-folders:/&fq=con_locales:fr&fq=type:v8news";
        assertEquals(ex, query.toString());
    }
}
