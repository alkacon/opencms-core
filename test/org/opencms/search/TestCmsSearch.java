/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/search/TestCmsSearch.java,v $
 * Date   : $Date: 2005/02/17 12:46:01 $
 * Version: $Revision: 1.4 $
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
import org.opencms.report.I_CmsReport;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestCase;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the cms search indexer.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.4 $
 */
public class TestCmsSearch extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCmsSearch(String arg0) {
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
        suite.setName(TestCmsSearch.class.getName());
                
        suite.addTest(new TestCmsSearch("testCmsSearchIndexer"));
        suite.addTest(new TestCmsSearch("testCmsSearchXmlContent"));
        
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
     * Test the cms search indexer.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsSearchIndexer() throws Throwable {
        
        I_CmsReport report = new CmsShellReport();
        OpenCms.getSearchManager().updateIndex(report);
    }
    
    /**
     * Test the cms search indexer.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsSearchXmlContent() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing search for xml contents");
        
        CmsSearch cmsSearchBean = new CmsSearch();
        cmsSearchBean.init(cms);
        cmsSearchBean.setIndex("Offline project (VFS)");
        List results;
        
        cmsSearchBean.setQuery(">>SearchEgg1<<");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0001.html", ((CmsSearchResult)results.get(0)).getPath());
        
        cmsSearchBean.setQuery(">>SearchEgg2<<");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0002.html", ((CmsSearchResult)results.get(0)).getPath());
        
        cmsSearchBean.setQuery(">>SearchEgg3<<");
        results = cmsSearchBean.getSearchResult();
        assertEquals(1, results.size());
        assertEquals("/sites/default/xmlcontent/article_0003.html", ((CmsSearchResult)results.get(0)).getPath());
    }
}