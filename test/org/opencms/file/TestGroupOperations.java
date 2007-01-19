/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestGroupOperations.java,v $
 * Date   : $Date: 2007/01/19 16:53:51 $
 * Version: $Revision: 1.6.8.1 $
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
 
package org.opencms.file;

import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for basic group operations without test import.<p>
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.6.8.1 $
 */
public class TestGroupOperations extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestGroupOperations(String arg0) {
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
        suite.setName(TestGroupOperations.class.getName());

        suite.addTest(new TestGroupOperations("testGetUsersOfGroup"));
        suite.addTest(new TestGroupOperations("testParentGroups"));
        
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
     * Tests the "getUsersOfGroup" method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testGetUsersOfGroup() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing testGetUsersOfGroup");
        
        List users = cms.getUsersOfGroup("Guests");
        assertEquals("/Export", ((CmsUser)users.get(0)).getName());
        assertEquals("/Guest", ((CmsUser)users.get(1)).getName());
    }

    /**
     * Tests the "getParentGroup" method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testParentGroups() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing the parent group mechanism");

        CmsGroup g1 = cms.createGroup("g1", "g1", 0, null);
        CmsGroup g2 = cms.createGroup("g2", "g2", 0, g1.getName());
        CmsGroup g3 = cms.createGroup("g3", "g3", 0, g1.getName());
        CmsGroup g4 = cms.createGroup("g4", "g4", 0, g2.getName());
        CmsGroup g5 = cms.createGroup("g5", "g5", 0, g2.getName());
        
        CmsUser u1 = cms.createUser("u1", "password", "u1", null);
        cms.addUserToGroup(u1.getName(), g1.getName());
        
        List g1Users = cms.getUsersOfGroup(g1.getName());
        assertEquals(1, g1Users.size());
        assertTrue(g1Users.contains(u1));
        List g2Users = cms.getUsersOfGroup(g2.getName());
        assertTrue(g2Users.isEmpty());
        List g3Users = cms.getUsersOfGroup(g3.getName());
        assertTrue(g3Users.isEmpty());
        List g4Users = cms.getUsersOfGroup(g4.getName());
        assertTrue(g4Users.isEmpty());
        List g5Users = cms.getUsersOfGroup(g5.getName());
        assertTrue(g5Users.isEmpty());
        List u1Groups = cms.getGroupsOfUser(u1.getName(), false);
        assertEquals(1, u1Groups.size());
        assertTrue(u1Groups.contains(g1));
        
        CmsUser u2 = cms.createUser("u2", "password", "u2", null);
        cms.addUserToGroup(u2.getName(), g2.getName());

        g1Users = cms.getUsersOfGroup(g1.getName());
        assertEquals(1, g1Users.size());
        assertTrue(g1Users.contains(u1));
        g2Users = cms.getUsersOfGroup(g2.getName());
        assertEquals(1, g2Users.size());
        assertTrue(g2Users.contains(u2));
        g3Users = cms.getUsersOfGroup(g3.getName());
        assertTrue(g3Users.isEmpty());
        g4Users = cms.getUsersOfGroup(g4.getName());
        assertTrue(g4Users.isEmpty());
        g5Users = cms.getUsersOfGroup(g5.getName());
        assertTrue(g5Users.isEmpty());
        u1Groups = cms.getGroupsOfUser(u1.getName(), false);
        assertEquals(1, u1Groups.size());
        assertTrue(u1Groups.contains(g1));
        List u2Groups = cms.getGroupsOfUser(u2.getName(), false);
        assertEquals(2, u2Groups.size());
        assertTrue(u2Groups.contains(g2));
        assertTrue(u2Groups.contains(g1));

        CmsUser u3 = cms.createUser("u3", "password", "u3", null);
        cms.addUserToGroup(u3.getName(), g3.getName());

        g1Users = cms.getUsersOfGroup(g1.getName());
        assertEquals(1, g1Users.size());
        assertTrue(g1Users.contains(u1));
        g2Users = cms.getUsersOfGroup(g2.getName());
        assertEquals(1, g2Users.size());
        assertTrue(g2Users.contains(u2));
        g3Users = cms.getUsersOfGroup(g3.getName());
        assertEquals(1, g3Users.size());
        assertTrue(g3Users.contains(u3));
        g4Users = cms.getUsersOfGroup(g4.getName());
        assertTrue(g4Users.isEmpty());
        g5Users = cms.getUsersOfGroup(g5.getName());
        assertTrue(g5Users.isEmpty());
        u1Groups = cms.getGroupsOfUser(u1.getName(), false);
        assertEquals(1, u1Groups.size());
        assertTrue(u1Groups.contains(g1));
        u2Groups = cms.getGroupsOfUser(u2.getName(), false);
        assertEquals(2, u2Groups.size());
        assertTrue(u2Groups.contains(g2));
        assertTrue(u2Groups.contains(g1));
        List u3Groups = cms.getGroupsOfUser(u3.getName(), false);
        assertEquals(2, u3Groups.size());
        assertTrue(u3Groups.contains(g3));
        assertTrue(u3Groups.contains(g1));

        CmsUser u4 = cms.createUser("u4", "password", "u4", null);
        cms.addUserToGroup(u4.getName(), g4.getName());

        g1Users = cms.getUsersOfGroup(g1.getName());
        assertEquals(1, g1Users.size());
        assertTrue(g1Users.contains(u1));
        g2Users = cms.getUsersOfGroup(g2.getName());
        assertEquals(1, g2Users.size());
        assertTrue(g2Users.contains(u2));
        g3Users = cms.getUsersOfGroup(g3.getName());
        assertEquals(1, g3Users.size());
        assertTrue(g3Users.contains(u3));
        g4Users = cms.getUsersOfGroup(g4.getName());
        assertEquals(1, g4Users.size());
        assertTrue(g4Users.contains(u4));
        g5Users = cms.getUsersOfGroup(g5.getName());
        assertTrue(g5Users.isEmpty());
        u1Groups = cms.getGroupsOfUser(u1.getName(), false);
        assertEquals(1, u1Groups.size());
        assertTrue(u1Groups.contains(g1));
        u2Groups = cms.getGroupsOfUser(u2.getName(), false);
        assertEquals(2, u2Groups.size());
        assertTrue(u2Groups.contains(g2));
        assertTrue(u2Groups.contains(g1));
        u3Groups = cms.getGroupsOfUser(u3.getName(), false);
        assertEquals(2, u3Groups.size());
        assertTrue(u3Groups.contains(g3));
        assertTrue(u3Groups.contains(g1));
        List u4Groups = cms.getGroupsOfUser(u4.getName(), false);
        assertEquals(3, u4Groups.size());
        assertTrue(u4Groups.contains(g4));
        assertTrue(u4Groups.contains(g2));
        assertTrue(u4Groups.contains(g1));

        CmsUser u5 = cms.createUser("u5", "password", "u5", null);
        cms.addUserToGroup(u5.getName(), g5.getName());
        
        g1Users = cms.getUsersOfGroup(g1.getName());
        assertEquals(1, g1Users.size());
        assertTrue(g1Users.contains(u1));
        g2Users = cms.getUsersOfGroup(g2.getName());
        assertEquals(1, g2Users.size());
        assertTrue(g2Users.contains(u2));
        g3Users = cms.getUsersOfGroup(g3.getName());
        assertEquals(1, g3Users.size());
        assertTrue(g3Users.contains(u3));
        g4Users = cms.getUsersOfGroup(g4.getName());
        assertEquals(1, g4Users.size());
        assertTrue(g4Users.contains(u4));
        g5Users = cms.getUsersOfGroup(g5.getName());
        assertEquals(1, g5Users.size());
        assertTrue(g5Users.contains(u5));
        u1Groups = cms.getGroupsOfUser(u1.getName(), false);
        assertEquals(1, u1Groups.size());
        assertTrue(u1Groups.contains(g1));
        u2Groups = cms.getGroupsOfUser(u2.getName(), false);
        assertEquals(2, u2Groups.size());
        assertTrue(u2Groups.contains(g2));
        assertTrue(u2Groups.contains(g1));
        u3Groups = cms.getGroupsOfUser(u3.getName(), false);
        assertEquals(2, u3Groups.size());
        assertTrue(u3Groups.contains(g3));
        assertTrue(u3Groups.contains(g1));
        u4Groups = cms.getGroupsOfUser(u4.getName(), false);
        assertEquals(3, u4Groups.size());
        assertTrue(u4Groups.contains(g4));
        assertTrue(u4Groups.contains(g2));
        assertTrue(u4Groups.contains(g1));
        List u5Groups = cms.getGroupsOfUser(u5.getName(), false);
        assertEquals(3, u5Groups.size());
        assertTrue(u5Groups.contains(g5));
        assertTrue(u5Groups.contains(g2));
        assertTrue(u5Groups.contains(g1));
    }
}