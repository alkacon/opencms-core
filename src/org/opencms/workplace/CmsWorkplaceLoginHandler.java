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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
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

package org.opencms.workplace;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.ui.login.CmsLoginHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Resource init handler that loads the login form with the right parameters.<p>
 *
 * The login uri must have following format:<br>
 * <code>/${CONTEXT}/${SERVLET}/system/login/${OU_PATH}</code><p>
 *
 * for example:<br>
 * <code>/opencms/opencms/system/login/intranet/marketing</code><p>
 *
 * @since 6.5.6
 */
public class CmsWorkplaceLoginHandler implements I_CmsResourceInit {

    /** The login handler path. */
    public static final String LOGIN_HANDLER = "/system/login";

    /** The login form path. */
    public static final String LOGIN_FORM = "/system/login/index.html";

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(
        CmsResource resource,
        CmsObject cms,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsResourceInitException {

        if (resource != null) {
            return resource;
        }

        String uri = cms.getRequestContext().getUri();
        // check if the resource starts with the LOGIN_HANDLER
        if (!uri.startsWith(LOGIN_HANDLER)) {
            return resource;
        }
        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            // we now must switch to the root site to read the resource
            cms.getRequestContext().setSiteRoot("/");
            // read the resource
            resource = cms.readResource(LOGIN_FORM);
        } catch (CmsException e) {
            throw new CmsResourceInitException(e.getMessageContainer(), e);
        } finally {
            // restore the siteroot
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
            // resource may be null in case of an error
            if (resource != null) {
                // modify the uri to the one of the real resource
                cms.getRequestContext().setUri(cms.getSitePath(resource));
            }
        }
        // get the ou path
        String ou = uri.substring(LOGIN_HANDLER.length());
        if (!ou.startsWith(CmsOrganizationalUnit.SEPARATOR)) {
            ou = CmsOrganizationalUnit.SEPARATOR + ou;
        }
        if (!ou.endsWith(CmsOrganizationalUnit.SEPARATOR)) {
            ou += CmsOrganizationalUnit.SEPARATOR;
        }
        req.setAttribute(CmsLoginHelper.PARAM_PREDEF_OUFQN, ou);
        return resource;
    }
}