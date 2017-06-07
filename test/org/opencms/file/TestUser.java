/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

import org.opencms.importexport.CmsExport;
import org.opencms.importexport.CmsExportParameters;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule.ExportMode;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for OpenCms user object.<p>
 */
public class TestUser extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestUser(String arg0) {

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
        suite.setName(TestUser.class.getName());

        suite.addTest(new TestUser("testUserCreation"));
        suite.addTest(new TestUser("testUserInfo"));
        suite.addTest(new TestUser("testUserExport"));
        suite.addTest(new TestUser("testUserSelfManagement"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/", false);
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Test user creation.<p>
     *
     * @throws Throwable if something goes wrong
     */
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
    public void testUserInfo() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing operations with additional user info");

        Map map = new HashMap();
        map.put("one", new Integer(1));
        map.put("two", new Long(2));
        map.put("true", Boolean.TRUE);

        CmsUser user = cms.createUser("test", "test", "test", null);
        user.setAdditionalInfo("map", map);
        user.setAdditionalInfo("int", new Integer(2));
        user.setAdditionalInfo("boolean", Boolean.TRUE);
        user.setAdditionalInfo("double", new Double(45.23));

        cms.writeUser(user);
        user = cms.readUser("test");
        map = (Map)user.getAdditionalInfo("map");
        assertEquals(new Integer(1), map.get("one"));
        assertEquals(new Long(2), map.get("two"));
        assertEquals(Boolean.TRUE, map.get("true"));
        assertEquals(new Integer(2), user.getAdditionalInfo("int"));
        assertEquals(Boolean.TRUE, user.getAdditionalInfo("boolean"));
        assertEquals(new Double(45.23), user.getAdditionalInfo("double"));
    }

    /**
     * Test user creation.<p>
     *
     * @throws Throwable if something goes wrong
     */
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
}
