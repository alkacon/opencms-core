/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/configuration/TestSiteConfiguration.java,v $
 * Date   : $Date: 2005/06/27 23:22:20 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 
package org.opencms.configuration;

import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for site configuration.<p>
 * 
 * @author Armen Markarian 
 * 
 * @version $Revision: 1.8 $
 */
public class TestSiteConfiguration extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestSiteConfiguration(String arg0) {
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
        suite.setName(TestSiteConfiguration.class.getName());
             
        suite.addTest(new TestSiteConfiguration("testConfiguredSites"));
         
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
     * Tests the configured site settings.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testConfiguredSites() throws Throwable {
        echo("Testing Site Configuration"); 
        CmsSiteManager siteManager = OpenCms.getSiteManager();
        echo("Testing default Uri");
        assertEquals("/sites/default/", siteManager.getDefaultUri()); 
        echo("Testing workplace server");
        assertEquals("http://localhost:8080", siteManager.getWorkplaceServer());            
        CmsSite site = CmsSiteManager.getSite("/sites/default/folder1");
        if (site != null) {
            echo("Testing Site: '"+site.toString()+"'");
            CmsSiteMatcher matcher = site.getSiteMatcher();
            echo("Testing Server Protocol");
            assertEquals("http", matcher.getServerProtocol());
            echo("Testing Server Name");
            assertEquals("localhost", matcher.getServerName());
            echo("Testing Server Port");
            assertEquals(8081, matcher.getServerPort());   
        } else {
            fail("Test failed: site was null!");
        }                            
    }        
}

