/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestChflags.java,v $
 * Date   : $Date: 2005/06/23 10:47:25 $
 * Version: $Revision: 1.5 $
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

package org.opencms.file;

import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "chflags" method of the CmsObject.<p>
 * 
 * @author Thomas Weckert  
 * @version $Revision: 1.5 $
 * @since 6.0 alpha 2
 */
public class TestChflags extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestChflags(String arg0) {

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
        suite.setName(TestChflags.class.getName());

        suite.addTest(new TestChflags("testAddFlagInternal"));

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
     * Tests setting the "internal" flag on a resource.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testAddFlagInternal() throws Throwable {

        CmsObject cms = getCmsObject();

        echo("Tests setting the \"internal\" flag on a resource");
        addFlagInternal(this, cms);
    }

    /**
     * Tests setting the "internal" flag on a resource.<p>
     * 
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @throws Throwable if something goes wrong
     */
    public static void addFlagInternal(OpenCmsTestCase tc, CmsObject cms) throws Throwable {

        String resource1 = "/index.html";

        CmsResource resource = cms.readResource(resource1, CmsResourceFilter.ALL);
        tc.storeResources(cms, resource1);
        
        int existingFlags = resource.getFlags();
        int flags = existingFlags;
        long timestamp = System.currentTimeMillis();

        // the "internal" flag is not set
        assertEquals((existingFlags & I_CmsConstants.C_ACCESS_INTERNAL_READ), 0);

        // add the "internal" flag
        if ((flags & I_CmsConstants.C_ACCESS_INTERNAL_READ) == 0) {
            flags += I_CmsConstants.C_ACCESS_INTERNAL_READ;
        } else {
            fail("Resource " + resource1 + " has the \"internal\" flag already set!");
        }

        // change the flag
        cms.lockResource(resource1);
        cms.chflags(resource1, flags);
        cms.unlockResource(resource1);

        // check the status of the changed file
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_CHFLAGS);
        tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
        tc.assertState(cms, resource1, I_CmsConstants.C_STATE_CHANGED);
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
        tc.assertFlags(cms, resource1, I_CmsConstants.C_ACCESS_INTERNAL_READ);
        tc.assertProject(cms, resource1, cms.getRequestContext().currentProject());
    }

}