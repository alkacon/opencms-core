/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.main;

import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test case for {@link CmsSystemInfo}.
 * <p>
 * 
 * @since 6.0.0
 */
public class TestCmsSystemInfo extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSystemInfo(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsSystemInfo.class.getName());

        suite.addTest(new TestCmsSystemInfo("testGetConfigurationFileRfsPath"));
        suite.addTest(new TestCmsSystemInfo("testGetAbsoluteRfsPathRelativeToWebApplication"));
        suite.addTest(new TestCmsSystemInfo("testGetAbsoluteRfsPathRelativeToWebInf"));
        suite.addTest(new TestCmsSystemInfo("testOpenCmsVersionPropertiesFile"));
        suite.addTest(new TestCmsSystemInfo("testOpenCmsVersionAndBuildNumber"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms(null, "/sites/default/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests {@link CmsSystemInfo#getAbsoluteRfsPathRelativeToWebApplication(String)}.
     */
    public void testGetAbsoluteRfsPathRelativeToWebApplication() {

        CmsSystemInfo sysinfo = OpenCms.getSystemInfo();
        assertNotNull(sysinfo);
        String path;
        File file;

        path = sysinfo.getAbsoluteRfsPathRelativeToWebApplication("");
        assertNotNull(path);
        path = path.trim();
        assertEquals(true, path.length() != 0);
        file = new File(path).getAbsoluteFile();
        assertEquals(true, file.exists());
        assertEquals(true, file.isDirectory());

        path = sysinfo.getAbsoluteRfsPathRelativeToWebApplication("WEB-INF");
        assertNotNull(path);
        path = path.trim();
        assertEquals(true, path.length() != 0);
        file = new File(path).getAbsoluteFile();
        assertEquals(true, file.exists());
        assertEquals(true, file.isDirectory());

        path = sysinfo.getAbsoluteRfsPathRelativeToWebApplication("WEB-INF/config/opencms.properties");
        assertNotNull(path);
        path = path.trim();
        assertEquals(true, path.length() != 0);
        file = new File(path).getAbsoluteFile();
        assertEquals(file.getAbsolutePath() + " does not exist.", true, file.exists());
        assertEquals(true, file.isFile());

    }

    /**
     * Tests {@link CmsSystemInfo#getAbsoluteRfsPathRelativeToWebInf(String)}.
     */
    public void testGetAbsoluteRfsPathRelativeToWebInf() {

        CmsSystemInfo sysinfo = OpenCms.getSystemInfo();
        assertNotNull(sysinfo);
        String path;
        File file;

        path = sysinfo.getAbsoluteRfsPathRelativeToWebInf("");
        assertNotNull(path);
        path = path.trim();
        assertEquals(true, path.length() != 0);
        file = new File(path).getAbsoluteFile();
        assertEquals(true, file.exists());
        assertEquals(true, file.isDirectory());

        path = sysinfo.getAbsoluteRfsPathRelativeToWebInf(CmsSystemInfo.FOLDER_CONFIG_DEFAULT);
        assertNotNull(path);
        path = path.trim();
        assertEquals(true, path.length() != 0);
        file = new File(path).getAbsoluteFile();
        assertEquals(path + " does not exist.", true, file.exists());
        assertEquals(true, file.isDirectory());

        path = sysinfo.getAbsoluteRfsPathRelativeToWebInf(CmsSystemInfo.FOLDER_CONFIG_DEFAULT
            + CmsSearchConfiguration.DEFAULT_XML_FILE_NAME);
        assertNotNull(path);
        path = path.trim();
        assertEquals(true, path.length() != 0);
        file = new File(path).getAbsoluteFile();
        assertEquals(file.getAbsolutePath() + " does not exist.", true, file.exists());
        assertEquals(true, file.isFile());
    }

    /**
     * Tests {@link CmsSystemInfo#getConfigurationFileRfsPath()}.
     */
    public void testGetConfigurationFileRfsPath() {

        CmsSystemInfo sysinfo = OpenCms.getSystemInfo();
        assertNotNull(sysinfo);
        String path;
        File file;

        path = sysinfo.getConfigurationFileRfsPath();
        assertNotNull(path);
        path = path.trim();
        assertEquals(true, path.length() != 0);
        file = new File(path).getAbsoluteFile();
        assertEquals(true, file.exists());
        assertEquals(true, file.isFile());
    }

    /**
     * Tests the OpenCms version and build number.
     */
    public void testOpenCmsVersionAndBuildNumber() {

        String version = OpenCms.getSystemInfo().getVersionNumber();
        // make sure to bump this up with every major version number, or else the test would fail ;)
        String expectedVersion = "9.4";

        assertTrue("OpenCms Version number not set correctly, expected prefix ["
            + expectedVersion
            + "] but was ["
            + version.substring(0, expectedVersion.length())
            + "]"
            + version.substring(expectedVersion.length()), version.startsWith(expectedVersion));

        String versionId = OpenCms.getSystemInfo().getVersionId();

        // here we distinguish the test between build done manually 
        // and a dynamic build done by a CI system like Jenkins

        if (versionId.startsWith("Manual")) {
            // assume this is a manual build triggered outside of a CI system

            assertEquals("Unexpected version ID '" + versionId + "'", "Manual build", versionId);

            Map<String, CmsSystemInfo.BuildInfoItem> info = OpenCms.getSystemInfo().getBuildInfo();
            CmsSystemInfo.BuildInfoItem value;

            value = info.get("build.number");
            assertEquals("(not set)", value.getValue());
            assertEquals("Build Number", value.getNiceName());

            value = info.get("build.date");
            assertEquals("(not set)", value.getValue());
            assertEquals("Build Date", value.getNiceName());

            value = info.get("build.info");
            assertEquals("Static version file", value.getValue());
            assertEquals("build.info", value.getNiceName());
        } else if (versionId.startsWith("Release")
            || versionId.startsWith("Nightly")
            || versionId.startsWith("Milestone")) {
            // assume a build triggered by the Jenkins CI system

            Map<String, CmsSystemInfo.BuildInfoItem> info = OpenCms.getSystemInfo().getBuildInfo();
            // make sure we have the required values set
            assertNotNull("build.number not set", info.get("build.number"));
            assertNotNull("build.date not set", info.get("build.date"));
            assertNotNull("build.type not set", info.get("build.type"));
            assertNotNull("build.system not set", info.get("build.system"));
            assertNotNull("build.gitid not set", info.get("build.gitid"));
            assertNotNull("build.gitbranch not set", info.get("build.gitbranch"));

            assertEquals("Expected keys do not match", "build.number", info.get("build.number").getKeyName());
            assertEquals("Expected keys do not match", "build.date", info.get("build.date").getKeyName());
            assertEquals("Expected keys do not match", "build.system", info.get("build.system").getKeyName());

            // the git commit ID should be 7 chars long
            assertTrue(info.get("build.gitid").getValue().length() == 7);
            // the build system name is hard coded
            assertEquals("Jenkins", info.get("build.system").getValue());
        } else {
            fail("No valid version information for test cases found, version id is '"
                + OpenCms.getSystemInfo().getVersionId()
                + "'\n\nThis indicates manual unexpected changes in 'src/org/opencms/main/version.properties'");
        }
    }

    /**
     * Test to make sure that the version info file is available.
     */
    public void testOpenCmsVersionPropertiesFile() {

        try {
            CmsFileUtil.readFile("org/opencms/main/version.properties");
        } catch (IOException e) {
            // unable to read the file, so we fail
            fail("version.properties file not available");
        }
    }
}
