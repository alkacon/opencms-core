/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/search/TestCmsSearchFields.java,v $
 * Date   : $Date: 2008/02/27 12:05:27 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for searching in special fields of extracted document text.<p>
 * 
 * @author Alexander Kandzior 
 * @version $Revision: 1.5 $
 */
public class TestCmsSearchFields extends OpenCmsTestCase {

    /** Name of the index used for testing. */
    public static final String INDEX_OFFLINE = "Offline project (VFS)";

    /** The index used for testing. */
    public static final String INDEX_ONLINE = "Online project (VFS)";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSearchFields(String arg0) {

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
        suite.setName(TestCmsSearchFields.class.getName());

        suite.addTest(new TestCmsSearchFields("testSearchInFields"));
        suite.addTest(new TestCmsSearchFields("testExcerptCreationFromFields"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
            }

            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests searching in non-standard fields for specific Strings that are placed in 
     * various document formats.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSearchInFields() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing searching in non-standard content fields");

        // the search index may not have been created so far
        OpenCms.getSearchManager().rebuildIndex(INDEX_ONLINE, new CmsShellReport(Locale.ENGLISH));

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List searchResult;

        // The following "non-standard" mapping is set in the "opencms-search.xml" for this test case:
        //
        // <field name="special" store="true" tokenize="true"> 
        //     <mapping type="element">special</mapping>
        //     <mapping type="xpath">Teaser[1]</mapping>
        //     <mapping type="xpath">Teaser[2]</mapping>
        //     <mapping type="xpath">Teaser[3]</mapping>
        //     <mapping type="property">NavText</mapping>
        //     <mapping type="property-search">search.special</mapping>
        // </field>    

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");
        searchBean.setQuery("Cologne");

        // search only "special" field
        searchBean.setField(new String[] {"special"});
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found searching in 'special' index field:");
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(7, searchResult.size());

        // search in the default fields (does not contain one result from a "NavText" property)
        System.out.println("\n\nResults found searching in standard index fields:");
        searchBean.setField(CmsSearchIndex.DOC_META_FIELDS);
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        TestCmsSearch.printResults(searchResult, cms);
        assertEquals(3, searchResult.size());
    }

    /**
     * Tests excerpt generation only from searched fields.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testExcerptCreationFromFields() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing excerpt generation only from searched fields");

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List searchResult;

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/");
        searchBean.setQuery("Cologne");
        // search only "special" field 
        // NOTE: This has NOT been included in excerpt generation in opencms-search.xml
        searchBean.setField(new String[] {"special"});

        // use the default setting for "excerpt only from searched", 
        // so an excerpt is generated for some results even though the searches "special" field has no excerpt at all
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found searching in 'special' index field, some excerpts should be available:");
        TestCmsSearch.printResults(searchResult, cms, true);
        Iterator i = searchResult.iterator();
        boolean excerptFound = false;
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            // not all results have excerpts, some are images 
            excerptFound |= CmsStringUtil.isNotEmpty(res.getExcerpt());
        }
        assertTrue(excerptFound);

        // now change the setting for "excerpt only from searched", use excerpt only from searched field 
        // since the "special" field has no excerpt, all excerpts must be empty
        searchBean.setExcerptOnlySearchedFields(true);
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found searching in 'special' index field, NO excerpts should be available:");
        TestCmsSearch.printResults(searchResult, cms, true);
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            assertTrue(CmsStringUtil.isEmpty(res.getExcerpt()));
        }

        // keep the setting for "excerpt only from searched", 
        // but now search also "content" field, some excerpts must be available again
        searchBean.setField(new String[] {"content", "special"});
        searchResult = searchBean.getSearchResult();
        assertNotNull(searchResult);
        System.out.println("\n\nResults found searching in 'content' AND 'special' index fields, some excerpts should be available from 'content':");
        TestCmsSearch.printResults(searchResult, cms, true);
        excerptFound = false;
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            // not all results have excerpts, some are images 
            excerptFound |= CmsStringUtil.isNotEmpty(res.getExcerpt());
        }
        assertTrue(excerptFound);
    }
}