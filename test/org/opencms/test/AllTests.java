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

package org.opencms.test;

import org.opencms.util.CmsStringUtil;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * OpenCms main test suite, executes the individual test suites of all core packages.<p>
 *
 *
 * @since 6.0
 */
public final class AllTests {

    /** Path for the test.properties file. */
    public static final String TEST_PROPERTIES_PATH = OpenCmsTestProperties.getResourcePathFromClassloader("");

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
        System.out.println(
            "... OpenCms test run finished! (Total runtime: " + CmsStringUtil.formatRuntime(runTime) + ")");
    }

    /**
     * Creates the OpenCms JUnit test suite.<p>
     *
     * @return the OpenCms JUnit test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite("OpenCms complete tests");
        suite.addTest(org.opencms.test.TestTests.suite());
        suite.addTest(org.opencms.setup.AllTests.suite());
        suite.addTest(org.opencms.ade.configuration.AllTests.suite());
        suite.addTest(org.opencms.ade.containerpage.inherited.AllTests.suite());
        suite.addTest(org.opencms.ade.contenteditor.AllTests.suite());
        suite.addTest(org.opencms.ade.sitemap.AllTests.suite());
        suite.addTest(org.opencms.cache.AllTests.suite());
        suite.addTest(org.opencms.configuration.AllTests.suite());
        suite.addTest(org.opencms.crypto.AllTests.suite());
        suite.addTest(org.opencms.db.AllTests.suite());
        suite.addTest(org.opencms.file.AllTests.suite());
        suite.addTest(org.opencms.file.collectors.AllTests.suite());
        suite.addTest(org.opencms.file.types.AllTests.suite());
        suite.addTest(org.opencms.file.wrapper.AllTests.suite());
        suite.addTest(org.opencms.flex.AllTests.suite());
        suite.addTest(org.opencms.i18n.AllTests.suite());
        suite.addTest(org.opencms.importexport.AllTests.suite());
        suite.addTest(org.opencms.jsp.decorator.AllTests.suite());
        suite.addTest(org.opencms.jsp.search.AllTests.suite());
        suite.addTest(org.opencms.jsp.search.config.AllTests.suite());
        suite.addTest(org.opencms.jsp.search.config.parser.AllTests.suite());
        suite.addTest(org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.AllTests.suite());
        suite.addTest(org.opencms.jsp.util.AllTests.suite());
        suite.addTest(org.opencms.loader.AllTests.suite());
        suite.addTest(org.opencms.mail.AllTests.suite());
        suite.addTest(org.opencms.main.AllTests.suite());
        suite.addTest(org.opencms.module.AllTests.suite());
        suite.addTest(org.opencms.notification.AllTests.suite());
        suite.addTest(org.opencms.publish.AllTests.suite());
        suite.addTest(org.opencms.relations.AllTests.suite());
        suite.addTest(org.opencms.scheduler.AllTests.suite());
        suite.addTest(org.opencms.search.AllTests.suite());
        suite.addTest(org.opencms.search.extractors.AllTests.suite());
        suite.addTest(org.opencms.search.gallery.AllTests.suite());
        suite.addTest(org.opencms.search.solr.AllTests.suite());
        suite.addTest(org.opencms.security.AllTests.suite());
        suite.addTest(org.opencms.site.AllTests.suite());
        suite.addTest(org.opencms.staticexport.AllTests.suite());
        suite.addTest(org.opencms.synchronize.AllTests.suite());
        suite.addTest(org.opencms.ui.apps.AllTests.suite());
        suite.addTest(org.opencms.util.AllTests.suite());
        suite.addTest(org.opencms.widgets.AllTests.suite());
        suite.addTest(org.opencms.workplace.AllTests.suite());
        suite.addTest(org.opencms.xml.AllTests.suite());
        suite.addTest(org.opencms.xml.containerpage.AllTests.suite());
        suite.addTest(org.opencms.xml.content.AllTests.suite());
        suite.addTest(org.opencms.xml.page.AllTests.suite());
        suite.addTest(org.opencms.repository.AllTests.suite());
        suite.addTest(org.opencms.ugc.AllTests.suite());
       suite.addTest(org.opencms.ui.contextmenu.TestCmsContextMenu.suite());

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                oneTimeSetUp();
            }

            @Override
            protected void tearDown() {

                oneTimeTearDown();
            }
        };

        return wrapper;
    }
}