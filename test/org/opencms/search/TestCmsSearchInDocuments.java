/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/search/TestCmsSearchInDocuments.java,v $
 * Date   : $Date: 2005/03/24 10:25:26 $
 * Version: $Revision: 1.3 $
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
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.search.documents.I_CmsDocumentFactory;
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
 * @version $Revision: 1.3 $
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
        suite.addTest(new TestCmsSearchInDocuments("testSearchBoostInMeta"));
        
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
        
        Iterator i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(res.getPath() + " ");            
            System.out.println(res.getScore());               
        }
        assertEquals(expected, searchResult.size());
        
        CmsSearchResult res1 = (CmsSearchResult)searchResult.get(searchResult.size()-1);
        CmsSearchResult res2 = (CmsSearchResult)searchResult.get(searchResult.size()-2);
        CmsSearchResult res3 = (CmsSearchResult)searchResult.get(0);
        
        String path1 = cms.getRequestContext().removeSiteRoot(res1.getPath());
        String path2 = cms.getRequestContext().removeSiteRoot(res2.getPath());
        String path3 = cms.getRequestContext().removeSiteRoot(res3.getPath());
        
        CmsProperty maxBoost = new CmsProperty(I_CmsConstants.C_PROPERTY_SEARCH_PRIORITY, I_CmsDocumentFactory.SEARCH_PRIORITY_MAX_VALUE, null, true);
        CmsProperty highBoost = new CmsProperty(I_CmsConstants.C_PROPERTY_SEARCH_PRIORITY, I_CmsDocumentFactory.SEARCH_PRIORITY_HIGH_VALUE, null, true);
        CmsProperty lowBoost = new CmsProperty(I_CmsConstants.C_PROPERTY_SEARCH_PRIORITY, I_CmsDocumentFactory.SEARCH_PRIORITY_LOW_VALUE, null, true);
        
        cms.lockResource(path1);
        cms.writePropertyObject(path1, maxBoost);
        cms.unlockResource(path1);
        cms.lockResource(path2);
        cms.writePropertyObject(path2, highBoost);
        cms.unlockResource(path2);      
        cms.lockResource(path3);
        cms.writePropertyObject(path3, lowBoost);
        cms.unlockResource(path3);      
        
        // update the search indexes
        OpenCms.getSearchManager().updateIndex(new CmsShellReport());        
        
        // perform the same search again in the online index - must be same result as before
        searchBean.setIndex(INDEX_ONLINE);        
        searchBean.setQuery("Alkacon Software");
        searchResult = searchBean.getSearchResult();
        
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(res.getPath() + " ");            
            System.out.println(res.getScore());                
        }
        assertEquals(expected, searchResult.size());
        
        assertEquals(res1.getPath(), ((CmsSearchResult)searchResult.get(searchResult.size()-1)).getPath());
        assertEquals(res2.getPath(), ((CmsSearchResult)searchResult.get(searchResult.size()-2)).getPath());
        assertEquals(res3.getPath(), ((CmsSearchResult)searchResult.get(0)).getPath());
        
        // now the search in the offline index - the boosted docs should now be on top
        searchBean.setIndex(INDEX_OFFLINE);        
        searchBean.setQuery("Alkacon Software");
        searchResult = searchBean.getSearchResult();
        
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(res.getPath() + " ");            
            System.out.println(res.getScore());            
        }
        assertEquals(expected, searchResult.size());
        
        // ensure boosted results are on top
        assertEquals(res1.getPath(), ((CmsSearchResult)searchResult.get(0)).getPath());
        assertEquals(res2.getPath(), ((CmsSearchResult)searchResult.get(1)).getPath());
        // low boosted document should be on last position
        assertEquals(res3.getPath(), ((CmsSearchResult)searchResult.get(searchResult.size()-1)).getPath());
    }
    
    /**
     * Tests search boosting when seachrching in meta information only.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSearchBoostInMeta() throws Exception {
        
        CmsObject cms = getCmsObject();
        echo("Testing search boosting in meta information");        
        
        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List searchResult;               

        // count depend on the number of documents indexed
        int expected = 6;
        
        String path = "/search/";
        String query = "OpenCms by Alkacon";
        
        searchBean.init(cms);
        searchBean.setIndex(INDEX_OFFLINE);
        searchBean.setSearchRoot(path);
        
        searchBean.setQuery(query);
        // ensure only meta information is searched
        searchBean.setField(new String[]{I_CmsDocumentFactory.DOC_META});
        searchResult = searchBean.getSearchResult();
        // since no resource has any description, no results should be found
        Iterator i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(res.getPath() + " ");            
            System.out.println(res.getScore());            
        }        
        assertEquals(0, searchResult.size());
        
        CmsProperty descripion = new CmsProperty(I_CmsConstants.C_PROPERTY_DESCRIPTION, query, null, true);
        CmsProperty delete = new CmsProperty(I_CmsConstants.C_PROPERTY_SEARCH_PRIORITY, CmsProperty.C_DELETE_VALUE, CmsProperty.C_DELETE_VALUE);
        
        List resources = cms.getFilesInFolder(path);

        i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String sitePath = cms.getSitePath(res);
            System.out.print(sitePath);      
            cms.lockResource(sitePath);
            cms.writePropertyObject(sitePath, descripion);
            // delete potential "search.priority" setting from earlier tests
            cms.writePropertyObject(sitePath, delete);
            cms.unlockResource(sitePath);
        }        
        assertEquals(expected, resources.size());        
        
        // update the search indexes
        OpenCms.getSearchManager().updateIndex(new CmsShellReport());        
        
        // perform the same search again in the online index - must be same result as before
        searchBean.setIndex(INDEX_ONLINE);        
        searchBean.setQuery(query);
        searchResult = searchBean.getSearchResult();
        assertEquals(0, searchResult.size());
        
        // now the search in the offline index - documents should now be found
        searchBean.setIndex(INDEX_OFFLINE);        
        searchBean.setQuery(query);
        List firstSearchResult = searchBean.getSearchResult();
        
        i = firstSearchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(res.getPath() + " ");            
            System.out.println(res.getScore());            
        }        
        assertEquals(expected, firstSearchResult.size());
        
        CmsSearchResult res1 = (CmsSearchResult)firstSearchResult.get(firstSearchResult.size()-1);
        CmsSearchResult res2 = (CmsSearchResult)firstSearchResult.get(firstSearchResult.size()-2);
        CmsSearchResult res3 = (CmsSearchResult)firstSearchResult.get(0);
        
        String path1 = cms.getRequestContext().removeSiteRoot(res1.getPath());
        String path2 = cms.getRequestContext().removeSiteRoot(res2.getPath());
        String path3 = cms.getRequestContext().removeSiteRoot(res3.getPath());
        
        CmsProperty maxBoost = new CmsProperty(I_CmsConstants.C_PROPERTY_SEARCH_PRIORITY, I_CmsDocumentFactory.SEARCH_PRIORITY_MAX_VALUE, null, true);
        CmsProperty highBoost = new CmsProperty(I_CmsConstants.C_PROPERTY_SEARCH_PRIORITY, I_CmsDocumentFactory.SEARCH_PRIORITY_HIGH_VALUE, null, true);
        CmsProperty lowBoost = new CmsProperty(I_CmsConstants.C_PROPERTY_SEARCH_PRIORITY, I_CmsDocumentFactory.SEARCH_PRIORITY_LOW_VALUE, null, true);
        
        cms.lockResource(path1);
        cms.writePropertyObject(path1, maxBoost);
        cms.unlockResource(path1);
        cms.lockResource(path2);
        cms.writePropertyObject(path2, highBoost);
        cms.unlockResource(path2);      
        cms.lockResource(path3);
        cms.writePropertyObject(path3, lowBoost);
        cms.unlockResource(path3);      
        
        // update the search indexes
        OpenCms.getSearchManager().updateIndex(new CmsShellReport());        
        
        // perform the same search again in the online index - must be same result as before
        searchBean.setIndex(INDEX_ONLINE);        
        searchBean.setQuery(query);
        searchResult = searchBean.getSearchResult();
        assertEquals(0, searchResult.size());

        // just output the first seach result again, just for convenient comparison on the console
        i = firstSearchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(res.getPath() + " ");            
            System.out.println(res.getScore());            
        }        
        
        // now the search in the offline index - the boosted docs should now be on top
        searchBean.setIndex(INDEX_OFFLINE);        
        searchBean.setQuery(query);
        searchResult = searchBean.getSearchResult();
        
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(res.getPath() + " ");            
            System.out.println(res.getScore());            
        }
        assertEquals(expected, searchResult.size());
        
        // ensure boosted results are on top
        assertEquals(res1.getPath(), ((CmsSearchResult)searchResult.get(0)).getPath());
        assertEquals(res2.getPath(), ((CmsSearchResult)searchResult.get(1)).getPath());
        // low boosted document should be on last position
        assertEquals(res3.getPath(), ((CmsSearchResult)searchResult.get(searchResult.size()-1)).getPath());
    }
}