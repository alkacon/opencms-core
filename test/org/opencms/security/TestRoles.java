/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/security/TestRoles.java,v $
 * Date   : $Date: 2005/06/23 11:11:58 $
 * Version: $Revision: 1.3 $
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
 
package org.opencms.security;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Iterator;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OpenCms system roles.<p>
 */
public class TestRoles extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestRoles(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * Setup is done without importing vfs data.
     * 
     * @return the test suite
     */
    public static Test suite() {
        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        
        TestSuite suite = new TestSuite();
        suite.setName(TestRoles.class.getName());

        suite.addTest(new TestRoles("testRoleExceptionMessages"));
        
        TestSetup wrapper = new TestSetup(suite) {
            
            protected void setUp() {
                setupOpenCms(null, null, false);
            }
            
            protected void tearDown() {
                removeOpenCms();
            }
        };
        
        return wrapper;
    } 
    
    /**
     * Tests if all keys in the system roles exception messages can be resolved.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testRoleExceptionMessages() throws Exception {
        
        echo("Testing role exception messages");
        CmsObject cms = getCmsObject();     
        
        String message;
        
        // check the system roles
        Iterator i = CmsRole.getSystemRoles().iterator();
        while (i.hasNext()) {
            CmsRole role = (CmsRole)i.next();
            CmsRoleViolationException ex = role.createRoleViolationException(cms.getRequestContext());
            message = ex.getMessage();
            System.out.println(message);
            // check if a key could not be resolved
            assertFalse(message.indexOf(CmsMessages.C_UNKNOWN_KEY_EXTENSION) >= 0);
            // very simple check if message still containes unresolved '{n}'
            assertFalse(message.indexOf('{') >= 0);
        }
        
        // check a user defined role
        String roleName = "MY_VERY_SPECIAL_ROLE";
        CmsRole myRole = new CmsRole(roleName, OpenCms.getDefaultUsers().getGroupAdministrators(), new CmsRole[0]);
        message = myRole.createRoleViolationException(cms.getRequestContext()).getMessage();
        
        System.out.println(message);
        // check if a key could not be resolved
        assertFalse(message.indexOf(CmsMessages.C_UNKNOWN_KEY_EXTENSION) >= 0);
        // very simple check if message still containes unresolved '{n}'
        assertFalse(message.indexOf('{') >= 0);        
    }    
}
