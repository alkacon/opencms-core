/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestDeletion.java,v $
 * Date   : $Date: 2005/09/16 13:11:14 $
 * Version: $Revision: 1.6.2.1 $
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

import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for VFS permissions.<p>
 * 
 * @author Alexander Kandzior 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.6.2.1 $
 */
public class TestDeletion extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestDeletion(String arg0) {

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
        suite.setName(TestDeletion.class.getName());

        suite.addTest(new TestDeletion("testGroupDeletion"));
        suite.addTest(new TestDeletion("testAdvancedGroupDeletion"));
        suite.addTest(new TestDeletion("testUserDeletion"));

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
     * Tests user group deletion.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testGroupDeletion() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing user group deletion");

        String groupname = "deleteGroup";

        List expected = cms.getGroups();

        // create group
        cms.createGroup(groupname, "deleteMe", I_CmsPrincipal.FLAG_ENABLED, "Users");

        // now delete the group again
        cms.deleteGroup(groupname);

        List actual = cms.getGroups();

        assertEquals(expected, actual);
    }

    /**
     * Tests an advanced group deletion.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testAdvancedGroupDeletion() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing advanced group deletion");

        // create test group
        cms.createGroup("Testgroup", "A test group", 0, null);
        CmsGroup testGroup = cms.readGroup("Testgroup");
        // set a child
        cms.setParentGroup(OpenCms.getDefaultUsers().getGroupUsers(), testGroup.getName());
        // create test user
        cms.createUser("testuser1", "test1", "A test user 1", null);
        cms.addUserToGroup("testuser1", "Testgroup");
        CmsUser testUser1 = cms.readUser("testuser1");

        // login in offline project
        CmsProject offline = cms.readProject("Offline");
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(offline);

        // change permission of a resource
        String resName = "/folder1/subfolder11/subsubfolder111/text.txt";
        cms.lockResource(resName);
        cms.chacc(resName, I_CmsPrincipal.PRINCIPAL_USER, testUser1.getName(), "+r+w+v+i");
        cms.chacc(resName, I_CmsPrincipal.PRINCIPAL_GROUP, testGroup.getName(), "+r+v+i");
        cms.unlockResource(resName);
        cms.publishProject();

        // create a 2nd test group
        cms.createGroup("testgroup2", "A test group 2", 0, null);
        CmsGroup testGroup2 = cms.readGroup("testgroup2");

        // remember group data
        List childs = cms.getChild(testGroup.getName());
        List users = cms.getUsersOfGroup(testGroup.getName());
        
        // delete the test group
        cms.deleteGroup(testGroup.getId(), testGroup2.getId());
        
        // check ace for the resource
        boolean found = false;
        Iterator it = cms.getAccessControlEntries(resName, false).iterator();
        while (it.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)it.next();
            if (ace.getPrincipal().equals(testGroup2.getId())) {
                CmsAccessControlEntry newAce = new CmsAccessControlEntry(
                    cms.readResource(resName).getResourceId(),
                    testGroup2.getId(),
                    "+r+v+i");
                newAce.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_GROUP);
                assertTrue(newAce.equals(ace));
                found = true;
            }
        }
        assertTrue(found);
        
        // check group data
        assertEquals(childs, cms.getChild(testGroup2.getName()));
        assertEquals(users, cms.getUsersOfGroup(testGroup2.getName()));

        // restore the previous state
        cms.deleteUser(testUser1.getId());
        cms.setParentGroup(OpenCms.getDefaultUsers().getGroupUsers(), null);
        cms.deleteGroup(testGroup2.getName());
    }
    
    /**
     * Tests user deletion.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testUserDeletion() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing user deletion");

        cms.createGroup("Testgroup", "A test group", 0, null);
        CmsGroup testGroup = cms.readGroup("Testgroup");
        cms.createUser("testuser1", "test1", "A test user 1", null);
        cms.addUserToGroup("testuser1", "Testgroup");
        CmsUser testUser1 = cms.readUser("testuser1");

        cms.lockResource("/");
        cms.chacc("/", I_CmsPrincipal.PRINCIPAL_USER, testUser1.getName(), "+r+w+v+i+c");
        cms.unlockResource("/");
        cms.publishProject();
        
        cms.loginUser(testUser1.getName(), "test1");
        CmsProject offline = cms.readProject("Offline");
        cms.getRequestContext().setCurrentProject(offline);

        String resName = "/myfile.txt";
        cms.createResource(resName, CmsResourceTypePlain.getStaticTypeId(), "my content".getBytes(), null);
        cms.lockResource(resName);
        cms.chacc(resName, I_CmsPrincipal.PRINCIPAL_USER, testUser1.getName(), "+r+w+v+i");
        cms.chacc(resName, I_CmsPrincipal.PRINCIPAL_GROUP, testGroup.getName(), "+r+v+i");
        cms.unlockResource(resName);

        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(offline);
        cms.publishProject();

        String resName2 = "/folder1/subfolder11/subsubfolder111/text.txt";
        cms.lockResource(resName2);
        cms.chacc(resName2, I_CmsPrincipal.PRINCIPAL_USER, testUser1.getName(), "+r+w+v+i");
        cms.chacc(resName2, I_CmsPrincipal.PRINCIPAL_GROUP, testGroup.getName(), "+r+v+i");
        cms.unlockResource(resName2);
        cms.publishProject();

        cms.createUser("testuser2", "test2", "A test user 2", null);
        cms.addUserToGroup("testuser2", "Testgroup");
        CmsUser testUser2 = cms.readUser("testuser2");

        int state = cms.readResource(resName).getState();
        cms.deleteUser(testUser1.getId(), testUser2.getId());

        // check attributes
        CmsResource res = cms.readResource(resName);
        assertEquals(res.getUserCreated(), testUser2.getId());
        assertEquals(res.getUserLastModified(), testUser2.getId());
        assertEquals(res.getState(), state);

        // check ace for first resource
        boolean found = false;
        Iterator it = cms.getAccessControlEntries(resName, false).iterator();
        while (it.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)it.next();
            if (ace.getPrincipal().equals(testUser2.getId())) {
                CmsAccessControlEntry newAce = new CmsAccessControlEntry(
                    cms.readResource(resName).getResourceId(),
                    testUser2.getId(),
                    "+r+w+v+i");
                newAce.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_USER);
                assertTrue(newAce.equals(ace));
                found = true;
            }
        }
        assertTrue(found);

        // check ace for second resource
        found = false;
        it = cms.getAccessControlEntries(resName2, false).iterator();
        while (it.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)it.next();
            if (ace.getPrincipal().equals(testUser2.getId())) {
                CmsAccessControlEntry newAce = new CmsAccessControlEntry(
                    cms.readResource(resName2).getResourceId(),
                    testUser2.getId(),
                    "+r+w+v+i");
                newAce.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_USER);
                assertTrue(newAce.equals(ace));
                found = true;
            }
        }
        assertTrue(found);

        // restore the previous state
        cms.lockResource(resName);
        cms.deleteResource(resName, CmsResource.DELETE_REMOVE_SIBLINGS);
        cms.unlockResource(resName);
        cms.publishProject();
        cms.deleteUser(testUser2.getId());
        cms.deleteGroup(testGroup.getName());
    }
}
