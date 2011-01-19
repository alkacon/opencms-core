/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsDetailPageResourceHandler.java,v $
 * Date   : $Date: 2011/01/19 09:52:24 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Resource init handler for detail-pages.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public class CmsDetailPageResourceHandler implements I_CmsResourceInit {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDetailPageResourceHandler.class);

    /** The attribute containing the detail view content id. */
    public static final String ATTR_DETAIL_CONTENT_ID = "__detail_content_id";

    /**
     * Default constructor.<p>
     */
    public CmsDetailPageResourceHandler() {

        // empty
    }

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(CmsResource resource, CmsObject cms, HttpServletRequest req, HttpServletResponse res)
    throws CmsResourceInitException, CmsSecurityException {

        // check if the resource was already found
        boolean abort = (resource != null);
        // check if the resource comes from the root site
        abort |= CmsStringUtil.isEmptyOrWhitespaceOnly(cms.getRequestContext().getSiteRoot());
        // check if the resource comes from the /system/ folder
        abort |= cms.getRequestContext().getUri().startsWith(CmsWorkplace.VFS_PATH_SYSTEM);
        if (abort) {
            // skip in all cases above 
            return resource;
        }

        String path = cms.getRequestContext().getUri();
        if (path.endsWith("/") && (path.length() > 1)) {
            path = path.substring(0, path.length() - 1);
        }
        String detailName = CmsResource.getName(path);
        try {
            CmsUUID detailId = cms.readIdForUrlName(detailName);

            if (detailId != null) {
                // check existence / permissions
                cms.readResource(detailId);

                CmsResource detailPage = cms.readDefaultFile(CmsResource.getFolderPath(path));
                req.setAttribute(ATTR_DETAIL_CONTENT_ID, detailId);
                // set the resource path
                cms.getRequestContext().setUri(cms.getSitePath(detailPage));
                return detailPage;
            }
        } catch (CmsPermissionViolationException e) {
            // trigger the permission denied handler
            throw e;
        } catch (CmsResourceInitException e) {
            throw e;
        } catch (Throwable e) {
            String uri = cms.getRequestContext().getUri();
            CmsMessageContainer msg = Messages.get().container(Messages.ERR_SITEMAP_1, uri);
            LOG.error(msg.key(), e);
            throw new CmsResourceInitException(msg, e);
        }

        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * Returns the current detail content id if available.<p>
     * 
     * @param req the current request
     * 
     * @return the content id or <code>null</code> if not available
     */
    public static CmsUUID getDetailId(ServletRequest req) {

        return (CmsUUID)req.getAttribute(ATTR_DETAIL_CONTENT_ID);
    }

}
