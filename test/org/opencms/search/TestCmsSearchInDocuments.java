/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/search/TestCmsSearchInDocuments.java,v $
 * Date   : $Date: 2005/03/23 22:09:06 $
 * Version: $Revision: 1.2 $
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
import org.opencms.file.CmsProperty;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for searching in extracted document text.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class TestCmsSearchInDocuments extends OpenCmsTestCase {

    /** Name of the index used for testing. */
    public static final String INDEX_OFFLINE = "Offline project (VFS)";
    
    /** The index used for testing. */
    public static final String INDEX_ONLINE = "Online project (VFS)";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSearchInDocuments(String arg0) {

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
        suite.setName(TestCmsSearchInDocuments.class.getName());

        suite.addTest(new TestCmsSearchInDocuments("testSearchIndexGeneration"));
        suite.addTest(new TestCmsSearchInDocuments("testSearchInDocuments"));
        suite.addTest(new TestCmsSearchInDocuments("testSearchBoost"));
        
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
     * Imports the documents for the test cases in the VFS an generates the index.<p>
     * 
     * Please note: This method need to be called first in this test suite, the
     * other methods depend on the index generated here.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSearchIndexGeneration() throws Exception {
        
        CmsObject cms = getCmsObject();
        echo("Testing search index generation with different resource types");

        // create test folder
        cms.createResource("/search/", CmsResourceTypeFolder.C_RESOURCE_TYPE_ID, null, null);
        cms.unlockResource("/search/");

        // import the sample documents to the VFS
        importTestResource(cms, 
            "org/opencms/search/extractors/test1.pdf", "/search/test1.pdf", 
            CmsResourceTypeBinary.getStaticTypeId(), 
            Collections.EMPTY_LIST);        
        importTestResource(cms, 
            "org/opencms/search/extractors/test1.doc", "/search/test1.doc", 
            CmsResourceTypeBinary.getStaticTypeId(), 
            Collections.EMPTY_LIST);
        importTestResource(cms, 
            "org/opencms/search/extractors/test1.rtf", "/search/test1.rtf", 
            CmsResourceTypeBinary.getStaticTypeId(), 
            Collections.EMPTY_LIST);
        importTestResource(cms, 
            "org/opencms/search/extractors/test1.xls", "/search/test1.xls", 
            CmsResourceTypeBinary.getStaticTypeId(), 
            Collections.EMPTY_LIST);
        importTestResource(cms, 
            "org/opencms/search/extractors/test1.ppt", "/search/test1.ppt", 
            CmsResourceTypeBinary.getStaticTypeId(), 
            Collections.EMPTY_LIST);

        // HTML page is encoded using UTF-8
        List properties = new ArrayList();
        properties.add(new CmsProperty(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, "UTF-8", null, true));       
        importTestResource(cms, 
            "org/opencms/search/extractors/test1.html", "/search/test1.html", 
            CmsResourceTypePlain.getStaticTypeId(), 
            properties);

       
        assertTrue(cms.existsResource("/search/test1.pdf"));
        assertTrue(cms.existsResource("/search/test1.html"));
        assertTrue(cms.existsResource("/search/test1.doc"));
        assertTrue(cms.existsResource("/search/test1.rtf"));
        assertTrue(cms.existsResource("/search/test1.xls"));
        assertTrue(cms.existsResource("/search/test1.ppt"));
        
        // publish the project
        cms.publishProject(new CmsShellReport());
    
        // update the search indexes
        OpenCms.getSearchManager().updateIndex(new CmsShellReport());
       
        // check the online project
        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
        
        assertTrue(cms.existsResource("/search/test1.pdf"));
        assertTrue(cms.existsResource("/search/test1.html"));
        assertTrue(cms.existsResource("/search/test1.doc"));
        assertTrue(cms.existsResource("/search/test1.rtf"));
        assertTrue(cms.existsResource("/search/test1.xls"));
        assertTrue(cms.existsResource("/search/test1.ppt"));        
    }
    
    /**
     * Tests searching in the VFS for specific Strings that are placed in 
     * various document formats.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSearchInDocuments() throws Exception {
        
        CmsObject cms = getCmsObject();
        echo("Testing searching in different (complex) document types");
        
        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List searchResult;               

        // count depend on the number of documents indexed
        int expected = 6;
        
        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/search/");

        searchBean.setQuery("Alkacon Software");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());
        
        searchBean.setQuery("The OpenCms experts");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());
        
        searchBean.setQuery("Some content here.");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());
        
        searchBean.setQuery("Some content there.");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());
        
        searchBean.setQuery("Some content on a second sheet.");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());

        searchBean.setQuery("Some content on the third sheet.");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());
        
        searchBean.setQuery("‰ˆ¸ƒ÷‹ﬂ");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());
    }
    
    /**
     * Tests search boosting.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSearchBoost() throws Exception {
        
        CmsObject cms = getCmsObject();
        echo("Testing search boosting");        
        
        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List searchResult;               

        // count depend on the number of documents indexed
        int expected = 6;
        
        searchBean.init(cms);
        searchBean.setIndex(INDEX_OFFLINE);
        searchBean.setSearchRoot("/search/");
        
        searchBean.setQuery("Alkacon Software");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());
        
        Iterator i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.println(res.getPath());            
        }
        
        CmsSearchResult res1 = (CmsSearchResult)searchResult.get(searchResult.size()-1);
        CmsSearchResult res2 = (CmsSearchResult)searchResult.get(searchResult.size()-2);
        
        String path1 = cms.getRequestContext().removeSiteRoot(res1.getPath());
        String path2 = cms.getRequestContext().removeSiteRoot(res2.getPath());
        
        CmsProperty maxBoost = new CmsProperty(I_CmsConstants.C_PROPERTY_SEARCH_PRIORITY, CmsSearchIndex.SEARCH_BOOST_MAX_VALUE, null, true);
        CmsProperty highBoost = new CmsProperty(I_CmsConstants.C_PROPERTY_SEARCH_PRIORITY, CmsSearchIndex.SEARCH_BOOST_HIGH_VALUE, null, true);

        
        cms.lockResource(path1);
        cms.writePropertyObject(path1, maxBoost);
        cms.unlockResource(path1);
        cms.lockResource(path2);
        cms.writePropertyObject(path2, highBoost);
        cms.unlockResource(path2);      
        
        // update the search indexes
        OpenCms.getSearchManager().updateIndex(new CmsShellReport());        
        
        // perform the same search again in the online index - must be same result as before
        searchBean.setIndex(INDEX_ONLINE);        
        searchBean.setQuery("Alkacon Software");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());
        
        assertEquals(expected, searchResult.size());
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.println(res.getPath());            
        }
        
        assertTrue(((CmsSearchResult)searchResult.get(searchResult.size()-1)).getPath().equals(res1.getPath()));
        assertTrue(((CmsSearchResult)searchResult.get(searchResult.size()-2)).getPath().equals(res2.getPath()));
        
        // now the search in the offline index - the boosted docs should now be on top
        searchBean.setIndex(INDEX_OFFLINE);        
        searchBean.setQuery("Alkacon Software");
        searchResult = searchBean.getSearchResult();
        
        assertEquals(expected, searchResult.size());
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.println(res.getPath());            
        }
        
        // ensure boosted results are on top
        assertTrue(((CmsSearchResult)searchResult.get(0)).getPath().equals(res1.getPath()));
        assertTrue(((CmsSearchResult)searchResult.get(1)).getPath().equals(res2.getPath()));
    }
}