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

package org.opencms.flex;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.test.mock.CmsMockHttpServletRequest;
import org.opencms.test.mock.CmsMockHttpServletResponse;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * Unit tests for the {@link CmsFlexRequest}.<p>
 */
public class TestCmsFlexRequest extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests that a nested flex request inherits the parameters of the flex request it is created from,
     * even if that flex request is wrapped by the servlet container.<p>
     *
     * Servlet containers add their own request wrapper around the flex request for an include, and such
     * a wrapper does not necessarily report the parameters of the request it wraps. The parameters an
     * include adds to the current flex request must reach the included element regardless.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testNestedRequestInheritsParametersThroughContainerWrapper() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource resource = cms.readResource("/index.html");
        CmsMockHttpServletRequest request = new CmsMockHttpServletRequest();
        CmsMockHttpServletResponse response = new CmsMockHttpServletResponse();
        CmsFlexController controller = new CmsFlexController(
            cms,
            resource,
            OpenCms.getFlexCache(),
            request,
            response,
            false,
            true);
        CmsFlexController.setController(request, controller);
        CmsFlexRequest flexRequest = new CmsFlexRequest(request, controller);
        // this is what an include does with the parameters passed to it
        flexRequest.addParameterMap(Collections.singletonMap("noscriptList", new String[] {"true"}));

        // a container wrapper which does not report the parameters of the request it wraps
        HttpServletRequest containerWrapper = new HttpServletRequestWrapper(flexRequest) {

            @Override
            public Map<String, String[]> getParameterMap() {

                return Collections.emptyMap();
            }
        };

        CmsFlexRequest nestedRequest = new CmsFlexRequest(containerWrapper, controller);
        assertEquals(
            "true",
            nestedRequest.getParameter("noscriptList"),
            "nested flex request must inherit the parameters of the wrapped flex request");
    }
}
