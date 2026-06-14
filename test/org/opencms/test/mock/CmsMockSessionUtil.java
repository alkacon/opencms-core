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

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;

/**
 * Convenience helper to pre-authenticate a {@link CmsMockHttpServletRequest} for an already
 * logged-in {@link CmsObject}, so tests can call authenticated request handler endpoints without
 * driving the full login endpoint first.<p>
 *
 * It registers the user with the real {@link org.opencms.main.CmsSessionManager}, which writes the
 * OpenCms session id and the client token into the request session exactly like a real login; a
 * later {@link org.opencms.main.CmsSessionManager#getSessionInfo(javax.servlet.http.HttpServletRequest)}
 * for the same request then resolves the user.<p>
 *
 * @see CmsMockHttpServletRequest
 */
public final class CmsMockSessionUtil {

    /**
     * Hides the public constructor.
     */
    private CmsMockSessionUtil() {

        // utility class
    }

    /**
     * Pre-authenticates the given request for the given logged-in CMS context.<p>
     *
     * The CMS object must belong to a real (non-guest) user; the request keeps the resulting session
     * so reusing it for the next handler call is recognized as the same logged-in client.<p>
     *
     * @param request the mock request to authenticate
     * @param cms the logged-in CMS context whose user the session should carry
     */
    public static void authenticate(CmsMockHttpServletRequest request, CmsObject cms) {

        if (cms.getRequestContext().getCurrentUser().isGuestUser()) {
            throw new IllegalArgumentException("Cannot authenticate a request for the guest user.");
        }
        // ensure a session exists, then let the session manager write the session id and client token
        request.getSession(true);
        OpenCms.getSessionManager().updateSessionInfo(cms, request);
    }
}
