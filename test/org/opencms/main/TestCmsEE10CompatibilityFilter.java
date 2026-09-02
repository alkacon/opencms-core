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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexRequest;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.test.mock.CmsMockHttpServletRequest;
import org.opencms.test.mock.CmsMockHttpServletResponse;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * Unit tests for the {@link CmsEE10CompatibilityFilter}.<p>
 */
public class TestCmsEE10CompatibilityFilter extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests that the request passed down the filter chain reports the current parameters of the flex request,
     * even if the servlet container wrapper around the flex request only holds a stale snapshot of them.<p>
     *
     * Jetty (EE10) wraps the flex request for includes and forwards, and its wrapper reads the parameters of the
     * flex request only once. Parameters OpenCms adds to the flex request afterwards (includes, cms:addparams)
     * must still be visible to the JSP.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testParametersAreReadFromFlexRequest() throws Exception {

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

        // a container wrapper which keeps a snapshot of the parameters of the request it wraps
        final Map<String, String[]> snapshot = new HashMap<String, String[]>(flexRequest.getParameterMap());
        HttpServletRequest containerWrapper = new HttpServletRequestWrapper(flexRequest) {

            @Override
            public String getParameter(String name) {

                String[] values = snapshot.get(name);
                return (values == null) ? null : values[0];
            }

            @Override
            public Map<String, String[]> getParameterMap() {

                return Collections.unmodifiableMap(snapshot);
            }

            @Override
            public Enumeration<String> getParameterNames() {

                return Collections.enumeration(snapshot.keySet());
            }

            @Override
            public String[] getParameterValues(String name) {

                return snapshot.get(name);
            }
        };
        // this is what cms:addparams or an include does after the container has wrapped the flex request
        flexRequest.addParameterMap(Collections.singletonMap("noscriptList", new String[] {"true"}));
        assertNull(containerWrapper.getParameter("noscriptList"), "the container wrapper must hold a stale snapshot");

        final ServletRequest[] chainRequest = new ServletRequest[1];
        new CmsEE10CompatibilityFilter().doFilter(containerWrapper, response, (req, res) -> chainRequest[0] = req);

        ServletRequest filtered = chainRequest[0];
        assertEquals("true", filtered.getParameter("noscriptList"), "getParameter must read the flex request");
        assertEquals(
            "true",
            filtered.getParameterMap().get("noscriptList")[0],
            "getParameterMap must read the flex request");
        assertEquals(
            "true",
            filtered.getParameterValues("noscriptList")[0],
            "getParameterValues must read the flex request");
        assertTrue(
            Collections.list(filtered.getParameterNames()).contains("noscriptList"),
            "getParameterNames must read the flex request");
        assertSame(
            controller,
            CmsFlexController.getController(filtered),
            "attributes must still be resolved through the container wrapper");

        // a flex request that is not wrapped by the container is passed on unchanged
        new CmsEE10CompatibilityFilter().doFilter(flexRequest, response, (req, res) -> chainRequest[0] = req);
        assertSame(flexRequest, chainRequest[0], "an unwrapped flex request must not be wrapped by the filter");
    }
}
