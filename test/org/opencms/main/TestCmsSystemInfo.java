/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/main/TestCmsSystemInfo.java,v $
 * Date   : $Date: 2005/11/15 10:32:55 $
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

package org.opencms.main;

import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.io.File;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test case for {@link CmsSystemInfo}.
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.0.0
 */
public class TestCmsSystemInfo extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.
     * <p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSystemInfo(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.
     * <p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsSystemInfo.class.getName());

        suite.addTest(new TestCmsSystemInfo("testGetAbsoluteRfsPathRelativeToWebApplication"));
        suite.addTest(new TestCmsSystemInfo("testGetAbsoluteRfsPathRelativeToWebInf"));
        suite.addTest(new TestCmsSystemInfo("getConfigurationFileRfsPath"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms(null, "/sites/default/");
            }

            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests {@link CmsSystemInfo#getConfigurationFileRfsPath()}.
     * <p>
     * 
     */
    public void getConfigurationFileRfsPath() {

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
     * Tests {@link CmsSystemInfo#getAbsoluteRfsPathRelativeToWebApplication(String)}.
     * <p>
     * 
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

        path = sysinfo.getAbsoluteRfsPathRelativeToWebApplication("WEB-INF/log/opencms.log");
        assertNotNull(path);
        path = path.trim();
        assertEquals(true, path.length() != 0);
        file = new File(path).getAbsoluteFile();
        assertEquals(file.getAbsolutePath() + " does not exist.", true, file.exists());
        assertEquals(true, file.isFile());

    }

    /**
     * Tests {@link CmsSystemInfo#getAbsoluteRfsPathRelativeToWebInf(String)}.
     * <p>
     * 
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

        path = sysinfo.getAbsoluteRfsPathRelativeToWebInf("config");
        assertNotNull(path);
        path = path.trim();
        assertEquals(true, path.length() != 0);
        file = new File(path).getAbsoluteFile();
        assertEquals(path + " does not exist.", true, file.exists());
        assertEquals(true, file.isDirectory());

        path = sysinfo.getAbsoluteRfsPathRelativeToWebInf("config/opencms-search.xml");
        assertNotNull(path);
        path = path.trim();
        assertEquals(true, path.length() != 0);
        file = new File(path).getAbsoluteFile();
        assertEquals(file.getAbsolutePath() + " does not exist.", true, file.exists());
        assertEquals(true, file.isFile());
    }
}
