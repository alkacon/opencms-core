/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/search/TestCmsSearchAdvancedFeatures.java,v $
 * Date   : $Date: 2005/03/24 17:38:21 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for advanced search features.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class TestCmsSearchAdvancedFeatures extends OpenCmsTestCase {

    /** Name of the index used for testing. */
    public static final String INDEX_OFFLINE = "Offline project (VFS)";
    
    /** The index used for testing. */
    public static final String INDEX_ONLINE = "Online project (VFS)";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSearchAdvancedFeatures(String arg0) {

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
        suite.setName(TestCmsSearchAdvancedFeatures.class.getName());
        
        suite.addTest(new TestCmsSearchAdvancedFeatures("testSortSearchResults"));

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
     * Tests sorting of search results.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSortSearchResults() throws Exception {
        
        CmsObject cms = getCmsObject();
        echo("Testing sorting of search results");        
        
        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List searchResult;               
        String query = "OpenCms";
                
        // update the search index used
        OpenCms.getSearchManager().updateIndex(INDEX_OFFLINE, new CmsShellReport());       
        
        searchBean.init(cms);
        searchBean.setIndex(INDEX_OFFLINE);                        
        searchBean.setQuery(query);
                
        // first run is default sort order
        searchResult = searchBean.getSearchResult();        
        Iterator i = searchResult.iterator();
        System.out.println("Result sorted by relevance:");       
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(CmsStringUtil.padRight(res.getPath(), 50));            
            System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));               
            System.out.print(CmsDateUtil.getHeaderDate(res.getDateLastModified().getTime()));               
            System.out.println("  score: " + res.getScore());               
        }
        
        // second run use Title sort order
        String lastTitle = null;
        searchBean.setSortOrder(CmsSearch.SORT_TITLE);
        searchResult = searchBean.getSearchResult();        
        i = searchResult.iterator();
        System.out.println("Result sorted by title:");       
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(CmsStringUtil.padRight(res.getPath(), 50));            
            System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));               
            System.out.print(CmsDateUtil.getHeaderDate(res.getDateLastModified().getTime()));               
            System.out.println("  score: " + res.getScore());
            if (lastTitle != null) {
                // make sure result is sorted correctly
                assertTrue(lastTitle.compareTo(res.getTitle()) <= 0);
            }
            lastTitle = res.getTitle();
        }
        
        // third run use date last modified
        long lastTime = 0;
        searchBean.setSortOrder(CmsSearch.SORT_DATE_LASTMODIFIED);
        searchResult = searchBean.getSearchResult();        
        i = searchResult.iterator();
        System.out.println("Result sorted by date last modified:");       
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(CmsStringUtil.padRight(res.getPath(), 50));            
            System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));               
            System.out.print(CmsDateUtil.getHeaderDate(res.getDateLastModified().getTime()));               
            System.out.println("  score: " + res.getScore());
            if (lastTime > 0) {
                // make sure result is sorted correctly
                assertTrue(lastTime >= res.getDateLastModified().getTime());
                assertTrue(res.getScore() <= 100);
            }
            lastTime = res.getDateLastModified().getTime();
        }
    }
    
}