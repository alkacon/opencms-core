/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/main/TestOpenCmsSingleton.java,v $
 * Date   : $Date: 2004/07/07 18:02:12 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.test.OpenCmsTestCase;

import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test the static OpenCms singleton object.<p> 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class TestOpenCmsSingleton extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestOpenCmsSingleton(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new TestOpenCmsSingleton("testInitCmsObject"));
               
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
     * Test case for the initCmsObject methods.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testInitCmsObject() throws Exception {
        
        CmsObject cms;
        
        echo("Testing access to initCmsObject methods");
                
        // test creation of "Guest" user CmsObject
        cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        if (! cms.getRequestContext().currentUser().getName().equals(OpenCms.getDefaultUsers().getUserGuest())) {
            fail("'Guest' user could not be properly initialized!");
        }
        
        // test creation of "Export" user CmsObject
        cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
        if (! cms.getRequestContext().currentUser().getName().equals(OpenCms.getDefaultUsers().getUserExport())) {
            fail("'Export' user could not be properly initialized!");
        }        
        
        // test creation of "Admin" user CmsObject (this must fail)
        boolean gotException = false;
        cms = null;
        try {
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserAdmin());
        } catch (CmsException e) {
            gotException = true; 
        }
        if (! gotException) {
            fail("'Admin' user could be initialized without permission check (with username)!");
        }
        
        // create "Admin" context info
        CmsContextInfo contextInfo = new CmsContextInfo(OpenCms.getDefaultUsers().getUserAdmin());
        
        // test creation of "Admin" user CmsObject with 2nd option (this must also fail)
        gotException = false;
        cms = null;
        try {
            cms = OpenCms.initCmsObject(null, contextInfo);
        } catch (CmsException e) {
            gotException = true; 
        }
        if (! gotException) {
            fail("'Admin' user could be initialized without permission check (with context info)!");
        }
                
        // now test creation of "Admin" user with admin permissions
        // also check if created context is actually the context provided
        String siteRoot = "/sites/default";
        String requestedUri = "/index.html";
        String encoding = "US-ASCII";
        
        contextInfo.setSiteRoot(siteRoot);
        contextInfo.setRequestedUri(requestedUri);
        contextInfo.setLocale(Locale.CHINESE);
        contextInfo.setEncoding(encoding);
        try {
            cms = OpenCms.initCmsObject(getCmsObject(), contextInfo);
        } catch (CmsException e) {
            fail("'Admin' user creation with valid Admin context didn't work!");
        }
        if (! cms.getRequestContext().currentUser().getName().equals(OpenCms.getDefaultUsers().getUserAdmin())) {
            fail("'Admin' user could not be properly initialized with valid Admin context!");
        }   
        if (cms == getCmsObject()) {
            fail("'Admin' user Object is the same as creating instance, but must be a new Object!");
        }      
        
        if (! cms.getRequestContext().getSiteRoot().equals(siteRoot)) {
            fail("Site root in created context not as expected.");
        }
        if (! cms.getRequestContext().getUri().equals(requestedUri)) {
            fail("Requested uri in created context not as expected.");
        }        
        if (! cms.getRequestContext().getEncoding().equals(encoding)) {
            fail("Encoding in created context not as expected.");
        }
        if (! cms.getRequestContext().getLocale().equals(Locale.CHINESE)) {
            fail("Locale in created context not as expected.");
        }        
    }
}