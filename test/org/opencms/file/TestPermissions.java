/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestPermissions.java,v $
 * Date   : $Date: 2004/08/23 15:37:02 $
 * Version: $Revision: 1.5 $
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
 
package org.opencms.file;

import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for VFS permissions.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 */
public class TestPermissions extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestPermissions(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        suite.setName(TestPermissions.class.getName());
                
        suite.addTest(new TestPermissions("testVisiblePermission"));
        suite.addTest(new TestPermissions("testVisiblePermissionForFolder"));
        suite.addTest(new TestPermissions("testFilterForFolder"));
        
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
     * Test the resource filter files in a folder.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testFilterForFolder() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing resource filer for the files in a folder");

        String folder = "/types";
        // read only "image" resources
        List resultList;
        // resources in folder only method
        resultList = cms.getResourcesInFolder(folder, CmsResourceFilter.requireType(CmsResourceTypeImage.C_RESOURCE_TYPE_ID));        
        if (resultList.size() != 1) {
            fail("There is only 1 image resource in the folder, not " + resultList.size());
        }
        // files in folder only method
        resultList = cms.getFilesInFolder(folder, CmsResourceFilter.requireType(CmsResourceTypeImage.C_RESOURCE_TYPE_ID));        
        if (resultList.size() != 1) {
            fail("There is only 1 image resource in the folder, not " + resultList.size());
        }            
        // subtree method
        resultList = cms.readResources(folder, CmsResourceFilter.requireType(CmsResourceTypeImage.C_RESOURCE_TYPE_ID));        
        if (resultList.size() != 1) {
            fail("There is only 1 image resource in the folder, not " + resultList.size());
        }           
    }
    
    /**
     * Test the visible permisssions on a list of files in a folder.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testVisiblePermissionForFolder() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing visible permissions on a list of files in a folder");
        
        String folder = "/types";

        // apply permissions to folder
        cms.lockResource(folder);
        // modify the resource permissions for the tests
        // remove all "Users" group permissions 
        cms.chacc(folder, I_CmsPrincipal.C_PRINCIPAL_GROUP, OpenCms.getDefaultUsers().getGroupUsers(), 0, 0, I_CmsConstants.C_ACCESSFLAGS_OVERWRITE + I_CmsConstants.C_ACCESSFLAGS_INHERIT);
        // allow only read for user "test1"
        cms.chacc(folder, I_CmsPrincipal.C_PRINCIPAL_USER, "test1", CmsPermissionSet.PERMISSION_READ, 0, I_CmsConstants.C_ACCESSFLAGS_OVERWRITE + I_CmsConstants.C_ACCESSFLAGS_INHERIT);
        // allow read and visible for user "test2"
        cms.chacc(folder, I_CmsPrincipal.C_PRINCIPAL_USER, "test2", CmsPermissionSet.PERMISSION_READ + CmsPermissionSet.PERMISSION_VIEW, 0, I_CmsConstants.C_ACCESSFLAGS_OVERWRITE + I_CmsConstants.C_ACCESSFLAGS_INHERIT);
        cms.unlockResource(folder); 
        
        List resultList;
        
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        // read excluding invisible resources
        resultList = cms.readResources(folder, CmsResourceFilter.ONLY_VISIBLE);
        if (resultList.size() > 0) {
            fail("Was able to read " + resultList.size() + " invisible resources in a folder with filter excluding invisible resources");
        }
        // read again now inclusing invisible resources
        resultList = cms.readResources(folder, CmsResourceFilter.ALL);        
        if (resultList.size() != 6) {
            fail("There should be 6 visible resource in the folder, not " + resultList.size());
        }        
        
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        resultList = cms.readResources(folder, CmsResourceFilter.ONLY_VISIBLE);        
        if (resultList.size() != 6) {
            fail("There should be 6 visible resource in the folder, not " + resultList.size());
        }
    }
    
    /**
     * Test the visible permisssions.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testVisiblePermission() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing visible permissions on a file");
        
        String resource = "index.html";
        CmsResource res = cms.readResource(resource);
        
        cms.lockResource(resource);
        // modify the resource permissions for the tests
        // remove all "Users" group permissions 
        cms.chacc(resource, I_CmsPrincipal.C_PRINCIPAL_GROUP, OpenCms.getDefaultUsers().getGroupUsers(), 0, 0, I_CmsConstants.C_ACCESSFLAGS_OVERWRITE);
        // allow only read for user "test1"
        cms.chacc(resource, I_CmsPrincipal.C_PRINCIPAL_USER, "test1", CmsPermissionSet.PERMISSION_READ, 0, I_CmsConstants.C_ACCESSFLAGS_OVERWRITE);
        // allow read and visible for user "test2"
        cms.chacc(resource, I_CmsPrincipal.C_PRINCIPAL_USER, "test2", CmsPermissionSet.PERMISSION_READ + CmsPermissionSet.PERMISSION_VIEW, 0, I_CmsConstants.C_ACCESSFLAGS_OVERWRITE);
        cms.unlockResource(resource);
        
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        if (! cms.hasPermissions(res, new CmsPermissionSet(CmsPermissionSet.PERMISSION_VIEW, 0), true, CmsResourceFilter.ALL)) {
            fail("Visible permission checked but should have been ignored");
        }
        if (cms.hasPermissions(res, new CmsPermissionSet(CmsPermissionSet.PERMISSION_VIEW, 0), true, CmsResourceFilter.ONLY_VISIBLE)) {
            fail("Visible permission not checked");
        }            
        
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        if (! cms.hasPermissions(res, new CmsPermissionSet(CmsPermissionSet.PERMISSION_VIEW, 0), true, CmsResourceFilter.ALL)) {
            fail("Visible permission checked but should be ignored");
        }
        if (! cms.hasPermissions(res, new CmsPermissionSet(CmsPermissionSet.PERMISSION_VIEW, 0), true, CmsResourceFilter.ONLY_VISIBLE)) {
            fail("Visible permission not detected");
        }
    }  
}
