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

import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.CmsSearchResource;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Date;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.solr.common.SolrInputDocument;

/**
 * Tests the Solr field mapping.<p>
 * 
 * @since 8.5.0
 */
public class TestSolrFieldMapping extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestSolrFieldMapping(String arg0) {

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
        suite.setName(TestSolrFieldMapping.class.getName());
        suite.addTest(new TestSolrFieldMapping("testAppinfoSolrField"));

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
     * Tests the Solr field configuration that can be done in the XSD of an XML content.<p>
     * 
     * '@see /sites/default/xmlcontent/article.xsd'
     * 
     * @throws Throwable if something goes wrong
     */
    public void testAppinfoSolrField() throws Throwable {

        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        OpenCms.getSearchManager().rebuildIndex(AllSolrTests.SOLR_OFFLINE, report);

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllSolrTests.SOLR_OFFLINE);
        CmsSolrQuery squery = new CmsSolrQuery(getCmsObject(), "path:/sites/default/xmlcontent/article_0001.html");
        CmsSolrResultList results = index.search(getCmsObject(), squery);

        // Test the result count
        AllSolrTests.printResults(getCmsObject(), results, false);
        assertEquals(1, results.size());

        // Test if the result contains the expected resource 
        CmsSearchResource res = results.get(0);
        assertEquals("/sites/default/xmlcontent/article_0001.html", res.getRootPath());

        // Test multiple language field
        String fieldValue = res.getField("ahtml_en");
        assertNotNull(fieldValue);
        fieldValue = res.getField("ahtml_de");
        assertNotNull(fieldValue);

        // Test the contents of the copy fields
        fieldValue = res.getField("test_text_de");
        assertNotNull(fieldValue);
        assertEquals(true, fieldValue.contains("Alkacon Software German"));
        fieldValue = res.getField("test_text_en");
        assertNotNull(fieldValue);
        assertEquals(true, fieldValue.contains("Alkacon Software German"));

        // Test locale restricted field
        fieldValue = res.getField("aauthor_de");
        assertNotNull(fieldValue);
        assertEquals(true, fieldValue.equals("Alkacon Software German"));
        fieldValue = res.getField("aauthor_en");
        assertNull(fieldValue);

        // Test source field
        Date dateValue = res.getDateField("arelease_en_dt");
        assertNotNull(dateValue);
        assertEquals(true, "1308210520000".equals(new Long(dateValue.getTime()).toString()));
        dateValue = res.getDateField("arelease_de_dt");
        assertNotNull(dateValue);
        assertEquals(true, "1308210420000".equals(new Long(dateValue.getTime()).toString()));

        // Test the boost is not available
        // the boost for a field can only be set for "SolrInputDocument"s
        // fields of documents that are returned as query result "SolrDocument"s
        // never have a boost
        float boost = ((SolrInputDocument)res.getDocument().getDocument()).getField("ahtml_en").getBoost();
        assertEquals(true, 1.0F == boost);

        // test the 'content' mapping
        fieldValue = res.getField("ateaser_en");
        assertNotNull(fieldValue);
        assertEquals(true, fieldValue.contains("OpenCms Alkacon This is the article 1 text"));

        // test the property mapping
        fieldValue = res.getField("atitle_en");
        assertNotNull(fieldValue);
        assertEquals(true, fieldValue.contains("Alkacon Software English"));
        fieldValue = res.getField("atitle_de");
        assertNotNull(fieldValue);
        // properties are not localized
        assertEquals(false, fieldValue.contains("Alkacon Software German"));
        // but the field value is localized
        assertEquals(true, fieldValue.contains(">>GermanSearchEgg1<<"));

        // test the 'property-search' mapping
        fieldValue = res.getField("ateaser_en");
        assertNotNull(fieldValue);
        assertEquals(true, fieldValue.contains("Cologne is a nice city"));

        // test the 'item' mapping with default
        fieldValue = res.getField("ateaser_en");
        assertNotNull(fieldValue);
        assertEquals(true, fieldValue.contains("/sites/default/index.html"));
        fieldValue = res.getField("ateaser_de");
        assertNotNull(fieldValue);
        assertEquals(true, fieldValue.contains("Homepage n.a."));

        // test the 'dynamic' mapping with 'class' attribute

        // test the 'attribute' mapping
        fieldValue = res.getField("ateaser_en");
        assertNotNull(fieldValue);
        // This is the Lucene optimized String representaion of the date
        assertEquals(true, fieldValue.contains("20110616074840000"));

        // test 'default' value for the whole field
        fieldValue = res.getField("ahomepage_de");
        assertNotNull(fieldValue);
        assertEquals(true, fieldValue.equals("Homepage n.a."));
        fieldValue = res.getField("ahomepage_en");
        assertEquals(true, fieldValue.contains("/sites/default/index.html"));

    }
}
