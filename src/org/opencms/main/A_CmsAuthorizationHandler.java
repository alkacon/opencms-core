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

package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.security.I_CmsAuthorizationHandler;
import org.opencms.util.CmsUUID;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Abstract class to grant the needed access to the session manager.<p>
 *
 * @since 6.5.4
 */
public abstract class A_CmsAuthorizationHandler implements I_CmsAuthorizationHandler {

    /** The static log object for this class. */
    protected static final Log LOG = CmsLog.getLog(A_CmsAuthorizationHandler.class);

    /** Additional parameters. */
    protected Map<String, String> m_parameters;

    /**
     * @see org.opencms.security.I_CmsAuthorizationHandler#setParameters(java.util.Map)
     */
    public void setParameters(Map<String, String> parameters) {

        m_parameters = parameters;
    }

    /**
     * Initializes a new cms object from the session data of the request.<p>
     *
     * If no session data is found, <code>null</code> is returned.<p>
     *
     * @param request the request
     *
     * @return the new initialized cms object
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsObject initCmsObjectFromSession(HttpServletRequest request) throws CmsException {

        // try to get an OpenCms user session info object for this request
        return OpenCmsCore.getInstance().initCmsObjectFromSession(request);
    }

    /**
     * Registers the current session with OpenCms.<p>
     *
     * @param request the current request
     * @param cms the cms object to register
     *
     * @return the updated cms context
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsObject registerSession(HttpServletRequest request, CmsObject cms) throws CmsException {

        // update the request context
        cms = OpenCmsCore.getInstance().updateContext(request, cms);

        CmsUser user = cms.getRequestContext().getCurrentUser();
        if (!user.isGuestUser() && !OpenCms.getDefaultUsers().isUserExport(user.getName())) {
            // create the session info object, only for 'real' users
            CmsSessionInfo sessionInfo = new CmsSessionInfo(
                cms.getRequestContext(),
                new CmsUUID(),
                request.getSession().getMaxInactiveInterval());
            // register the updated cms object in the session manager
            OpenCmsCore.getInstance().getSessionManager().addSessionInfo(sessionInfo);
        }
        // return the updated cms object
        return cms;
    }
}