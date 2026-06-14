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

package org.opencms.test.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.file.CmsObject;
import org.opencms.json.JSONObject;
import org.opencms.main.I_CmsRequestHandler;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestSnapRunner;
import org.opencms.test.mock.CmsRequestHandlerTestDispatcher.Result;
import org.opencms.util.CmsFileUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Smoke test for the servlet request and response mocks, exercising the request body, the captured
 * response and the real session based login round-trip against the OpenCms session manager, using a
 * minimal inline request handler.<p>
 *
 * It mirrors the shape of an OpenCms request handler endpoint (such as the MCP handler's
 * <code>/session/whoami</code>) without depending on a concrete handler, so the mocks can be verified
 * inside opencms-core.<p>
 */
public class TestCmsRequestHandlerMock extends OpenCmsTestSnapRunner {

    /**
     * Minimal request handler that reports whether the request carries a valid logged-in session and
     * echoes a body value, like a tiny "whoami" endpoint.<p>
     */
    private static final class MockWhoamiHandler implements I_CmsRequestHandler {

        /**
         * @see org.opencms.main.I_CmsRequestHandler#getHandlerNames()
         */
        @Override
        public String[] getHandlerNames() {

            return new String[] {"Mock"};
        }

        /**
         * @see org.opencms.main.I_CmsRequestHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
         */
        @Override
        public void handle(HttpServletRequest req, HttpServletResponse res, String name) throws IOException {

            res.setContentType("application/json");
            try {
                byte[] bytes = CmsFileUtil.readFully(req.getInputStream(), false);
                JSONObject body = bytes.length == 0
                ? new JSONObject()
                : new JSONObject(new String(bytes, StandardCharsets.UTF_8));
                boolean loggedIn = (OpenCms.getSessionManager().getSessionInfo(req) != null)
                    && OpenCms.getSessionManager().hasValidClientToken(req);
                JSONObject result = new JSONObject();
                result.put("loggedIn", loggedIn);
                result.putOpt("echo", body.optString("ping", null));
                if (loggedIn) {
                    result.put("user", OpenCms.getSessionManager().getSessionInfo(req).getUserId().toString());
                }
                res.setStatus(loggedIn ? HttpServletResponse.SC_OK : HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write(result.toString());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    /** The request handler under test. */
    private final MockWhoamiHandler m_handler = new MockWhoamiHandler();

    /** The admin CMS context from the snapshot setup. */
    private CmsObject m_cms;

    /**
     * Sets up a fresh OpenCms instance from the snapshot before each test.<p>
     *
     * @param testInfo the JUnit test info object
     *
     * @throws Exception if something goes wrong
     */
    @BeforeEach
    public void setUp(TestInfo testInfo) throws Exception {

        m_cms = setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * A request pre-authenticated with {@link CmsMockSessionUtil} reaches the handler as a logged-in
     * client, and the request body and captured response work end to end.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testAuthenticatedRoundTrip() throws Exception {

        CmsMockHttpServletRequest req = new CmsMockHttpServletRequest().withMethod("POST").withPath(
            "/handleMock/whoami").withJsonBody("{\"ping\":\"pong\"}");
        CmsMockSessionUtil.authenticate(req, m_cms);

        Result result = CmsRequestHandlerTestDispatcher.dispatch(m_handler, req);

        assertEquals(HttpServletResponse.SC_OK, result.getStatus());
        JSONObject json = result.json();
        assertTrue(json.getBoolean("loggedIn"), "request should be recognized as logged in");
        assertEquals("pong", json.getString("echo"), "request body did not reach the handler");
        assertEquals(
            m_cms.getRequestContext().getCurrentUser().getId().toString(),
            json.getString("user"),
            "session carried the wrong user");
    }

    /**
     * A request without a session is rejected as not logged in.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testUnauthenticated() throws Exception {

        CmsMockHttpServletRequest req = new CmsMockHttpServletRequest().withMethod("GET").withPath(
            "/handleMock/whoami");

        Result result = CmsRequestHandlerTestDispatcher.dispatch(m_handler, req);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, result.getStatus());
        assertFalse(result.json().getBoolean("loggedIn"), "request without a session must not be logged in");
    }
}
