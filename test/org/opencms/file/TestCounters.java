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

import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.framework.Test;

/**
 * Unit tests for the counters.<p>
 */
public class TestCounters extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCounters(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestCounters.class, "systemtest", "/");
    }

    /**
     * Tests reading a single counter.<p>
     *
     * @throws Exception when an error occurs
     */
    public void testReadCounter() throws Exception {

        CmsObject cms = getCmsObject();
        for (int i = 0; i < 10; i++) {
            int counter1 = cms.incrementCounter("type1");
            assertEquals(i, counter1);
        }
    }

    /**
     * Tests reading two counters in interleaved order.<p>
     *
     * @throws Exception when an error occurs
     */
    public void testReadCountersInterleaved() throws Exception {

        CmsObject cms = getCmsObject();
        for (int i = 0; i < 10; i++) {
            int counter2 = cms.incrementCounter("type2");
            int counter3 = cms.incrementCounter("type3");
            assertEquals(i, counter2);
            assertEquals(i, counter3);
        }
    }
}
