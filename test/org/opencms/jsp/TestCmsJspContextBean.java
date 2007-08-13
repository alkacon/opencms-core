/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/jsp/Attic/TestCmsJspContextBean.java,v $
 * Date   : $Date: 2007/08/13 16:30:19 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.util.CmsJspContextBean;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the <code>{@link CmsJspContextBean}</code>.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 7.0.2
 */
public class TestCmsJspContextBean extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsJspContextBean(String arg0) {

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
        suite.setName(TestCmsJspContextBean.class.getName());

        suite.addTest(new TestCmsJspContextBean("testReadResourceMap"));

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
     * Tests for the {@link CmsJspContextBean#getReadResource()} method.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testReadResourceMap() throws Exception {

        CmsObject cms = getCmsObject();
        CmsJspContextBean bean = new CmsJspContextBean(cms);

        Map readResource = bean.getReadResource();

        CmsResource res, dres;
        res = (CmsResource)readResource.get("/index.html");
        assertNotNull(res);
        dres = cms.readResource("/index.html");
        assertEquals(res, dres);

        res = (CmsResource)readResource.get("/idontexist.html");
        assertNull(res);
    }
}