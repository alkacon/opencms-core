/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestChacc.java,v $
 * Date   : $Date: 2004/08/10 15:42:43 $
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

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opencms.main.I_CmsConstants;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestResourceFilter;

/**
 * Unit test for the "chacc" method of the CmsObject.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.5 $
 */
public class TestChacc extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestChacc(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        suite.setName(TestChacc.class.getName());
                
        suite.addTest(new TestChacc("testChaccFileGroup"));
        suite.addTest(new TestChacc("testChaccFileUser"));
               
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
     * Test the chacc method on a file and a group.<p>
     * 
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to change permissions
     * @param group the group to change the permissions from
     * @param permissions the new permission set for this group
     * @param flags the flags for modifying the permission set
     * @throws Throwable if something goes wrong
     */
    public static void chaccFileGroup(OpenCmsTestCase tc, CmsObject cms, String resource1, CmsGroup group, CmsPermissionSet permissions, int flags) throws Throwable {            
       
        tc.storeResources(cms, resource1);
                
        cms.lockResource(resource1);
        cms.chacc(resource1, I_CmsPrincipal.C_PRINCIPAL_GROUP, group.getName(), permissions.getAllowedPermissions(), permissions.getDeniedPermissions(), flags);
        cms.unlockResource(resource1);
        
        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_CHACC);
        // test the ace of the new permission
        // add the group flag to the acl
        CmsResource res = cms.readResource(resource1, CmsResourceFilter.ALL);
 
        CmsAccessControlEntry ace = 
               new CmsAccessControlEntry(res.getResourceId(), group.getId(), 
                   permissions.getAllowedPermissions(), permissions.getDeniedPermissions(), 
                   flags + I_CmsConstants.C_ACCESSFLAGS_GROUP);      
        tc.assertAce(cms, resource1, ace);
         // test the acl with the permission set        
         int denied = permissions.getDeniedPermissions();
         if (flags == I_CmsConstants.C_ACCESSFLAGS_OVERWRITE) {
            denied = 0;
         }
        CmsPermissionSet permission = new CmsPermissionSet(permissions.getAllowedPermissions(), denied);
        tc.assertAcl(cms, resource1, group.getId(), permission);
    }
    
    /**
     * Test the chacc method on a file and a group.<p>
     * 
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to change permissions
     * @param group the group to change the permissions from
     * @param permissions the new permission set for this group
     * @param flags the flags for modifying the permission set
     * @throws Throwable if something goes wrong
     */
    public static void chaccFolderGroup(OpenCmsTestCase tc, CmsObject cms, String resource1, CmsGroup group, CmsPermissionSet permissions, int flags) throws Throwable {            
       
        tc.storeResources(cms, resource1);
                
        cms.lockResource(resource1);
        cms.chacc(resource1, I_CmsPrincipal.C_PRINCIPAL_GROUP, group.getName(), permissions.getAllowedPermissions(), permissions.getDeniedPermissions(), flags);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_CHACC);
        // test the ace of the new permission
        // add the group flag to the acl
        CmsResource res = cms.readResource(resource1, CmsResourceFilter.ALL);
 
        CmsAccessControlEntry ace = 
               new CmsAccessControlEntry(res.getResourceId(), group.getId(), 
                   permissions.getAllowedPermissions(), permissions.getDeniedPermissions(), 
                   flags + I_CmsConstants.C_ACCESSFLAGS_GROUP);      
        tc.assertAce(cms, resource1, ace);
         // test the acl with the permission set        
         int denied = permissions.getDeniedPermissions();
         if ((flags & I_CmsConstants.C_ACCESSFLAGS_OVERWRITE) > 0) {
            denied = 0;
         }
        CmsPermissionSet permission = new CmsPermissionSet(permissions.getAllowedPermissions(), denied);
        tc.assertAcl(cms, resource1, group.getId(), permission);
        
        // now check all the subresources in the folder, access must be modified as well
        List subresources = tc.getSubtree(cms, resource1);
        Iterator j = subresources.iterator();
        
        while (j.hasNext()) {
            CmsResource subRes = (CmsResource)j.next();
            String subResName = cms.getSitePath(subRes);
            // now evaluate the result
            tc.assertFilter(cms, subResName, OpenCmsTestResourceFilter.FILTER_CHACC);
            // test the ace of the new permission
            // add the group and the inherited flag to the acl
            ace = new CmsAccessControlEntry(res.getResourceId(), group.getId(), 
                      permissions.getAllowedPermissions(), permissions.getDeniedPermissions(), 
                      flags + I_CmsConstants.C_ACCESSFLAGS_GROUP + I_CmsConstants.C_ACCESSFLAGS_INHERITED); 
            tc.assertAce(cms, subResName, ace);
            
            // test the acl with the permission set     
           permission = new CmsPermissionSet(permissions.getAllowedPermissions(), denied);  
           tc.assertAcl(cms, resource1, subResName, group.getId(), permission);
            
        }  
    }

    /**
     * Test the chacc method on a file and a user.<p>
     * 
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to change permissions
     * @param user the user to change the permissions from
     * @param permissions the new permission set for this group
     * @param flags the flags for modifying the permission set
     * @throws Throwable if something goes wrong
     */
    public static void chaccFileUser(OpenCmsTestCase tc, CmsObject cms, String resource1, CmsUser user, CmsPermissionSet permissions, int flags) throws Throwable {            
       
        tc.storeResources(cms, resource1);
                
        cms.lockResource(resource1);
        cms.chacc(resource1, I_CmsPrincipal.C_PRINCIPAL_USER, user.getName(), permissions.getAllowedPermissions(), permissions.getDeniedPermissions(), flags);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_CHACC);
        // test the ace of the new permission
        // add the user flag to the acl
        CmsResource res = cms.readResource(resource1, CmsResourceFilter.ALL);
 
        CmsAccessControlEntry ace = 
               new CmsAccessControlEntry(res.getResourceId(), user.getId(), 
                   permissions.getAllowedPermissions(), permissions.getDeniedPermissions(), 
                   flags + I_CmsConstants.C_ACCESSFLAGS_USER);      
        tc.assertAce(cms, resource1, ace);
         // test the acl with the permission set
         int denied = permissions.getDeniedPermissions();
         if (flags == I_CmsConstants.C_ACCESSFLAGS_OVERWRITE) {
            denied = 0;
         }
        CmsPermissionSet permission = new CmsPermissionSet(permissions.getAllowedPermissions(), denied);
        tc.assertAcl(cms, resource1, user.getId(), permission);
    }
    
    /**
     * Test the chacc method on a file and a group.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testChaccFileGroup() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing chacc on a file and a group");
        chaccFileGroup(this, cms, "/index.html", cms.readGroup("Users"), new CmsPermissionSet(I_CmsConstants.C_ACCESS_READ, I_CmsConstants.C_ACCESS_WRITE), I_CmsConstants.C_ACCESSFLAGS_OVERWRITE);   
    }  
    
    /**
     * Test the chacc method on a file and a user.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testChaccFileUser() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing chacc on a file and a user");
        chaccFileUser(this, cms, "/folder1/index.html", cms.readUser("Guest"), new CmsPermissionSet(I_CmsConstants.C_ACCESS_WRITE, I_CmsConstants.C_ACCESS_READ), 0);   
    }  
    
    /**
     * Test the chacc method on a folder and a group.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testChaccFolderGroup() throws Throwable {
        //TODO: This test is not working correctly so far!
        CmsObject cms = getCmsObject();     
        echo("Testing chacc on a folder and a group");
        chaccFolderGroup(this, cms, "/folder2/", cms.readGroup("Guests"), new CmsPermissionSet(I_CmsConstants.C_ACCESS_WRITE, I_CmsConstants.C_ACCESS_READ), I_CmsConstants.C_ACCESSFLAGS_OVERWRITE + I_CmsConstants.C_ACCESSFLAGS_INHERIT);   
    }  
    
}