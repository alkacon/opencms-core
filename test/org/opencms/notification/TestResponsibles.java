/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/notification/TestResponsibles.java,v $
 * Date   : $Date: 2005/09/16 08:25:28 $
 * Version: $Revision: 1.1.2.1 $
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
 
package org.opencms.notification;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.HashSet;
import java.util.Set;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "readResponsibleUsers" method of the CmsObject.<p>
 * 
 * @author Jan Baudisch 
 * @version $Revision: 1.1.2.1 $
 */
public class TestResponsibles extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestResponsibles(String arg0) {
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
        suite.setName(TestResponsibles.class.getName());
                
        suite.addTest(new TestResponsibles("testResponsibles"));
               
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
     * Sets responsibles to a file and then tests the readResponsibleUsers method of CmsObject .<p>
     *  
     * @throws Throwable if something goes wrong
     */
    public void testResponsibles() throws Throwable {
        
        echo("Testing responsibles of resources");
        
        // create three users, two of them belonging to a group
        CmsObject cms = getCmsObject();
        CmsGroup tastycrats = cms.createGroup("tastycrats", "A test group", 0, null);
        CmsUser fry = cms.createUser("fry", "password", "First test user", null);
        CmsUser bender = cms.createUser("bender", "password", "Second test user, belonging to the tastycrats group.", null);
        CmsUser leela = cms.createUser("leela", "password", "Third test user, belonging to the tastycrats group.", null);
        CmsUser farnsworth = cms.createUser("farnsworth", "password", "Another test user, which is not responsible.", null);
        cms.addUserToGroup("bender", "tastycrats");
        cms.addUserToGroup("leela", "tastycrats");
         
        // make group and user responsible for the group
        String resource1 = "/folder1/index.html";
        CmsPermissionSet permissions = new CmsPermissionSet(CmsPermissionSet.PERMISSION_WRITE, CmsPermissionSet.PERMISSION_READ);
        cms.lockResource(resource1);
        cms.chacc(resource1, I_CmsPrincipal.PRINCIPAL_USER, fry.getName(), permissions.getAllowedPermissions(), permissions.getDeniedPermissions(), CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE);
        cms.chacc(resource1, I_CmsPrincipal.PRINCIPAL_GROUP, tastycrats.getName(), permissions.getAllowedPermissions(), permissions.getDeniedPermissions(), CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE);
        cms.chacc(resource1, I_CmsPrincipal.PRINCIPAL_USER, farnsworth.getName(), permissions.getAllowedPermissions(), permissions.getDeniedPermissions(), 0);
        cms.unlockResource(resource1);
        
        // check, if the three users are indeed responsible for the resource.
        Set responsibles = cms.readResponsibleUsers(cms.readResource(resource1));
        Set expectedResponsibles = new HashSet();
        expectedResponsibles.add(fry);
        expectedResponsibles.add(leela);
        expectedResponsibles.add(bender);
        assertEquals(responsibles, expectedResponsibles); 
    }
}