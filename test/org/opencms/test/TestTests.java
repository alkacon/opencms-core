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

package org.opencms.test;

import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

import junit.framework.Test;

public class TestTests extends OpenCmsTestCase {
    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestTests(String arg0) {

        super(arg0);
    }

    public static Test suite() {
        try {
            return generateTestSuite(TestTests.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void testErrorLog() {
        Log log = CmsLog.getLog("org.opencms.test.TestTest");
        try {
            log.error("logging an error should cause an exception to be thrown");
            fail();
        } catch (Exception e) {

        }
    }



}
