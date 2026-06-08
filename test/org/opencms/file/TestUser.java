/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.importexport.CmsExport;
import org.opencms.importexport.CmsExportParameters;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule.ExportMode;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.InvalidClassException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

import com.google.common.collect.Sets;

/**
 * Unit tests for OpenCms user object.<p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestUser extends OpenCmsTestRunner {

    static class Evil implements Serializable {

        private String m_text;

        public Evil(String text) {

            m_text = text;
        }

        @Override
        public String toString() {

            return getClass().getSimpleName() + "[" + m_text + "]";
        }
    }

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/", false);
    }

    /**
     * Tests the 'any groups' filter for user search.
     *
     * @throws Throwable
     */
    @Test
    @Order(6)
    public void testSearchByAnyGroups() throws Throwable {

        CmsObject cms = getCmsObject();
        CmsUserSearchParameters params = new CmsUserSearchParameters();
        CmsGroup searchGroup1 = cms.createGroup("searchGroup1", "test", 0, null);
        CmsGroup searchGroup2 = cms.createGroup("searchGroup2", "test", 0, null);
        CmsUser user1 = cms.createUser("ytest1", "password", "test", new HashMap<>());
        CmsUser user2 = cms.createUser("ytest2", "password", "test", new HashMap<>());
        CmsUser user3 = cms.createUser("ytest3", "password", "test", new HashMap<>());
        cms.addUserToGroup("ytest1", "searchGroup1");
        cms.addUserToGroup("ytest2", "searchGroup2");
        cms.addUserToGroup("ytest3", "searchGroup1");
        cms.addUserToGroup("ytest3", "searchGroup2");
        params.setAnyGroups(Arrays.asList(searchGroup1, searchGroup2));
        params.setPaging(9999, 1);
        List<CmsUser> users = OpenCms.getOrgUnitManager().searchUsers(cms, params);
        assertEquals(3, users.size(), "Wrong result set size");
        Set<String> userNames = users.stream().map(u -> u.getName()).collect(Collectors.toSet());
        assertEquals(new HashSet<>(Arrays.asList("ytest1", "ytest2", "ytest3")), userNames);

    }

    /**
     * Tests searching for user by email address.
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(5)
    public void testSearchByEmail() throws Throwable {

        CmsObject cms = getCmsObject();
        CmsUserSearchParameters params = new CmsUserSearchParameters();
        String email1 = "foo@foo.org";
        String email2 = "bar@bar.org";
        createUserWithEmail(cms, "xtest1", email1);
        createUserWithEmail(cms, "xtest2", email2);
        createUserWithEmail(cms, "xtest3", email1);
        createUserWithEmail(cms, "xtest4", email2);
        createUserWithEmail(cms, "xtest5", email1);
        params.setFilterEmail(email1);
        params.setPaging(9999, 1);
        List<CmsUser> users1 = OpenCms.getOrgUnitManager().searchUsers(cms, params);
        params.setFilterEmail(email2);
        List<CmsUser> users2 = OpenCms.getOrgUnitManager().searchUsers(cms, params);
        params.setFilterEmail("xyzzy@xyzzy.org");
        List<CmsUser> users3 = OpenCms.getOrgUnitManager().searchUsers(cms, params);

        assertEquals(
            Sets.newHashSet("xtest1", "xtest3", "xtest5"),
            users1.stream().map(user -> user.getName()).collect(Collectors.toSet()));
        assertEquals(
            Sets.newHashSet("xtest2", "xtest4"),
            users2.stream().map(user -> user.getName()).collect(Collectors.toSet()));
        assertEquals(0, users3.size());
    }

    /**
     * Tests serialization filtering.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(7)
    public void testSerializationFiltering() throws Exception {

        List<Object> list = new ArrayList<>();
        list.add(new CmsUUID());
        list.add(Long.valueOf(42));
        list.add(Boolean.TRUE);
        list.add("foo");
        list.add(new ArrayList<>());
        assertEquals(list, CmsDataTypeUtil.dataDeserialize(CmsDataTypeUtil.dataSerialize(list), "java.util.List"));

        list.add(new Evil("don't deserialize me"));
        byte[] serialized = CmsDataTypeUtil.dataSerialize(list);
        try {
            CmsDataTypeUtil.dataDeserialize(serialized, "java.util.List");
            fail("deserialization should have failed");
        } catch (InvalidClassException e) {
            // ignore, ok
        }

    }

    /**
     * Test user creation.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(1)
    public void testUserCreation() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing user creation");

        long time = System.currentTimeMillis();
        Thread.sleep(50);
        CmsUser user = cms.createUser("test123", "test123", "my description", null);
        Thread.sleep(50);
        assertTrue(time < user.getDateCreated());
        assertTrue(user.getDateCreated() < System.currentTimeMillis());
        assertEquals("my description", user.getDescription());
    }

    /**
     * Test import/export of additional user info.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(3)
    public void testUserExport() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing import/export of additional user info");

        String exportFileName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            OpenCms.getSystemInfo().getPackagesRfsPath() + File.separator + "userexport.zip");

        try {
            // export
            CmsUser before = cms.readUser("test");
            new CmsExport(cms, new CmsShellReport(Locale.ENGLISH)).exportData(
                new CmsExportParameters(
                    exportFileName,
                    null,
                    false,
                    true,
                    false,
                    Collections.EMPTY_LIST,
                    false,
                    false,
                    0,
                    false,
                    false,
                    ExportMode.DEFAULT));

            // delete
            cms.deleteUser("test");

            // import
            OpenCms.getImportExportManager().importData(
                cms,
                new CmsShellReport(Locale.ENGLISH),
                new CmsImportParameters(exportFileName, "/", true));
            CmsUser after = cms.readUser("test");

            // compare
            assertEquals(before.getAdditionalInfo(), after.getAdditionalInfo());
        } finally {
            deleteFile(exportFileName);
        }
    }

    /**
     * Test operations with additional user info.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(2)
    public void testUserInfo() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing operations with additional user info");

        Map map = new HashMap();
        map.put("one", Integer.valueOf(1));
        map.put("two", Long.valueOf(2));
        map.put("true", Boolean.TRUE);

        CmsUser user = cms.createUser("test", "test", "test", null);
        user.setAdditionalInfo("map", map);
        user.setAdditionalInfo("int", Integer.valueOf(2));
        user.setAdditionalInfo("boolean", Boolean.TRUE);
        user.setAdditionalInfo("double", Double.valueOf(45.23));

        cms.writeUser(user);
        user = cms.readUser("test");
        map = (Map)user.getAdditionalInfo("map");
        assertEquals(Integer.valueOf(1), map.get("one"));
        assertEquals(Long.valueOf(2), map.get("two"));
        assertEquals(Boolean.TRUE, map.get("true"));
        assertEquals(Integer.valueOf(2), user.getAdditionalInfo("int"));
        assertEquals(Boolean.TRUE, user.getAdditionalInfo("boolean"));
        assertEquals(Double.valueOf(45.23), user.getAdditionalInfo("double"));
    }

    /**
     * Test user creation.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(4)
    public void testUserSelfManagement() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing user creation");

        CmsUser userA = cms.createUser("userA", "test", "my description", null);
        assertFalse(userA.isManaged());

        CmsUser userB = cms.createUser("userB", "test", "my description", null);
        assertFalse(userB.isManaged());
        userB.setManaged(true);
        assertTrue(userB.isManaged());
        cms.writeUser(userB);

        // the admin should be able to change the pwd
        cms.setPassword(userA.getName(), "test2");
        cms.setPassword(userB.getName(), "test2");

        // login as userA
        cms.loginUser(userA.getName(), "test2");
        // he should be able to change his own pwd
        cms.setPassword(userA.getName(), "test2", "test3");

        // login as userB
        cms.loginUser(userB.getName(), "test2");
        // he should not be able to change his own pwd
        try {
            cms.setPassword(userB.getName(), "test2", "test3");
            fail("this user should not be able to change his own pwd");
        } catch (CmsDataAccessException e) {
            // ignore, ok
        }
    }

    /**
     * Creates a user with a given email address.
     *
     * @param cms the current CMS context
     * @param name the user name
     * @param email the user email address
     * @return the created user
     * @throws CmsException if something goes wrong
     */
    private CmsUser createUserWithEmail(CmsObject cms, String name, String email) throws CmsException {

        CmsUser user = cms.createUser(name, "password1!", "", null);
        user.setEmail(email);
        cms.writeUser(user);
        return user;

    }
}
