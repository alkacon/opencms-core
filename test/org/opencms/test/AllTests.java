/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/AllTests.java,v $
 * Date   : $Date: 2004/12/07 13:43:58 $
 * Version: $Revision: 1.15 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.test;

import org.opencms.util.CmsStringUtil;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.15 $
 * 
 * @since 5.0
 */
public final class AllTests {

    /** Path for the test.properties file. */
    public static String TEST_PROPERTIES_PATH = ClassLoader.getSystemResource("./").getPath() + "../test";

    /** Stopwatch for the time the test run. */
    private static long m_startTime;

    /**
     * Hide constructor to prevent generation of class instances.<p>
     */
    private AllTests() {

        // empty
    }

    /**
     * One-time initialization code.<p>
     */
    public static void oneTimeSetUp() {

        m_startTime = System.currentTimeMillis();
        System.out.println("Starting OpenCms test run...");
    }

    /** 
     * One-time cleanup code.<p>
     */
    public static void oneTimeTearDown() {

        long runTime = System.currentTimeMillis() - m_startTime;
        System.out.println("... OpenCms test run finished! (Total runtime: "
            + CmsStringUtil.formatRuntime(runTime)
            + ")");
    }

    /**
     * Creates the OpenCms JUnit test suite.<p>
     * 
     * @return the OpenCms JUnit test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite("OpenCms complete tests");

        suite.addTest(org.opencms.configuration.AllTests.suite());
        suite.addTest(org.opencms.file.AllTests.suite());
        suite.addTest(org.opencms.flex.AllTests.suite());
        suite.addTest(org.opencms.i18n.AllTests.suite());
        suite.addTest(org.opencms.importexport.AllTests.suite());
        suite.addTest(org.opencms.main.AllTests.suite());
        suite.addTest(org.opencms.module.AllTests.suite());
        suite.addTest(org.opencms.monitor.AllTests.suite());
        suite.addTest(org.opencms.scheduler.AllTests.suite());
        suite.addTest(org.opencms.search.AllTests.suite());
        suite.addTest(org.opencms.security.AllTests.suite());
        suite.addTest(org.opencms.setup.AllTests.suite());
        suite.addTest(org.opencms.staticexport.AllTests.suite());
        suite.addTest(org.opencms.synchronize.AllTests.suite());
        suite.addTest(org.opencms.util.AllTests.suite());
        suite.addTest(org.opencms.xml.AllTests.suite());
        suite.addTest(org.opencms.xml.content.AllTests.suite());
        suite.addTest(org.opencms.xml.page.AllTests.suite());

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                oneTimeSetUp();
            }

            protected void tearDown() {

                oneTimeTearDown();
            }
        };

        return wrapper;
    }
}