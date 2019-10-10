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

package org.opencms.jsp.userdata;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Impelemnts user data download links for the user data request feature.<p>
 *
 * Download links have the form .../userdatarequest/[id]?auth=[authcode] . If the stored user data request with the given id
 * exists, and its auth code matches the auth code given as a parameter, the download for the user data will be started, otherwise
 * HTTP 404 will be returned.
 */
public class CmsUserDataResourceHandler implements I_CmsResourceInit {

    /** The URL prefix which this handler should handle. */
    public static final String PREFIX = "/userdatarequest/";

    /** True if an instance has been created. */
    private static boolean m_initialized;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserDataResourceHandler.class);

    /**
     * Creates a new instance.
     */
    public CmsUserDataResourceHandler() {

        m_initialized = true;
    }

    /**
     * Returns true if an instance has been created.
     *
     * @return true if an instance has been created
     */
    public static boolean isInitialized() {

        return m_initialized;
    }

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(
        CmsResource resource,
        CmsObject cms,
        HttpServletRequest req,
        HttpServletResponse res)
    throws CmsResourceInitException {

        if ((resource != null) || (req == null) || (res == null)) {
            return resource;
        }

        CmsUserDataRequestManager manager = OpenCms.getUserDataRequestManager();
        if (manager == null) {
            return null;
        }

        String uri = cms.getRequestContext().getUri();
        if (!uri.startsWith(PREFIX)) {
            return null;
        }
        String infoStr = uri.substring(PREFIX.length());
        String key = CmsFileUtil.removeTrailingSeparator(infoStr);
        if (key.indexOf("/") >= 0) {
            return null;
        }

        CmsUserDataRequestInfo requestInfo = OpenCms.getUserDataRequestManager().getRequestStore().load(infoStr).orElse(
            null);
        if (requestInfo == null) {
            return null;
        }

        String auth = req.getParameter(CmsJspUserDataRequestBean.PARAM_AUTH);
        if (requestInfo.isExpired() || !requestInfo.checkAuthCode(auth)) {
            return null;
        }

        String info = requestInfo.getInfoHtml();
        if (CmsStringUtil.isEmpty(info)) {
            LOG.info("Invalid user data request object.");
            return null;
        }

        res.setCharacterEncoding("UTF-8");
        res.addHeader("Content-Disposition", "attachment; filename=\"userdata.html\"");
        try {
            String html = "<html><body>" + requestInfo.getInfoHtml() + "</body></html>";
            res.getOutputStream().write(html.getBytes("UTF-8"));
            res.setStatus(200);
            CmsResourceInitException e = new CmsResourceInitException(CmsUserDataResourceHandler.class);
            e.setClearErrors(true);
            throw e;
        } catch (CmsResourceInitException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return null;

    }

}
