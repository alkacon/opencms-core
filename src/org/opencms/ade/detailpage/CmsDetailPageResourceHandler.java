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

package org.opencms.ade.detailpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;

import org.apache.commons.logging.Log;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Resource init handler for detail-pages.<p>
 *
 * @since 8.0.0
 */
public class CmsDetailPageResourceHandler implements I_CmsResourceInit {

    /** The attribute containing the detail content resource. */
    public static final String ATTR_DETAIL_CONTENT_RESOURCE = "__opencms_detail_content_resource";

    /** The attribute containing the detail function page resource. */
    public static final String ATTR_DETAIL_FUNCTION_PAGE = "__opencms_detail_function_page";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDetailPageResourceHandler.class);

    /**
     * Default constructor.<p>
     */
    public CmsDetailPageResourceHandler() {

        // empty
    }

    /**
     * Returns the detail function page resource, if available.<p>
     *
     * @param req the current request
     *
     * @return the detail function page resource
     */
    public static CmsResource getDetailFunctionPage(ServletRequest req) {

        return (CmsResource)req.getAttribute(ATTR_DETAIL_FUNCTION_PAGE);
    }

    /**
     * Returns the current detail content UUID, or <code>null</code> if this is not a request to a content detail page.<p>
     *
     * @param req the current request
     *
     * @return the current detail content UUID, or <code>null</code> if this is not a request to a content detail page
     */
    public static CmsUUID getDetailId(ServletRequest req) {

        CmsResource res = getDetailResource(req);
        return res == null ? null : res.getStructureId();
    }

    /**
     * Returns the current detail content resource, or <code>null</code> if this is not a request to a content detail page.<p>
     *
     * @param req the current request
     *
     * @return the current detail content resource, or <code>null</code> if this is not a request to a content detail page
     */
    public static CmsResource getDetailResource(ServletRequest req) {

        return (CmsResource)req.getAttribute(ATTR_DETAIL_CONTENT_RESOURCE);
    }

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(
        CmsResource resource,
        CmsObject cms,
        HttpServletRequest req,
        HttpServletResponse res)
    throws CmsResourceInitException, CmsSecurityException {

        // check if the resource was already found or the path starts with '/system/'
        boolean abort = (resource != null) || cms.getRequestContext().getUri().startsWith(CmsWorkplace.VFS_PATH_SYSTEM);
        if (abort) {
            // skip in all cases above
            return resource;
        }
        try {
            CmsDetailResolution resolution = CmsDetailPageUtil.resolveDetail(
                cms,
                cms.getRequestContext().getUri(),
                (page, detailRes) -> isValidDetailPage(cms, page, detailRes));
            if (resolution == null) {
                return null;
            }
            if (res != null) {
                // response will be null if this run through the init handler is only for determining the locale
                boolean isFunction = resolution.isFunctionDetail();
                CmsResource detailResource = isFunction ? resolution.getFunctionPage() : resolution.getDetailContent();
                req.setAttribute(isFunction ? ATTR_DETAIL_FUNCTION_PAGE : ATTR_DETAIL_CONTENT_RESOURCE, detailResource);
                cms.getRequestContext().setDetailResource(detailResource);
            }
            // set the resource path
            cms.getRequestContext().setUri(cms.getSitePath(resolution.getDetailPage()));
            return resolution.getDetailPage();
        } catch (CmsPermissionViolationException e) {
            // trigger the permission denied handler
            throw e;
        } catch (CmsResourceInitException e) {
            throw e;
        } catch (CmsVfsResourceNotFoundException e) {
            return null;
        } catch (Throwable e) {
            String uri = cms.getRequestContext().getUri();
            CmsMessageContainer msg = Messages.get().container(Messages.ERR_RESCOURCE_NOT_FOUND_1, uri);
            if (LOG.isWarnEnabled()) {
                LOG.warn(msg.key(), e);
            }
            throw new CmsResourceInitException(msg, e);
        }
    }

    /**
     * Checks whether the given detail page is valid for the given resource.<p>
     *
     * @param cms the CMS context
     * @param page the detail page
     * @param detailRes the detail resource
     *
     * @return true if the given detail page is valid
     */
    protected boolean isValidDetailPage(CmsObject cms, CmsResource page, CmsResource detailRes) {

        return OpenCms.getADEManager().getDetailPageHandler().isValidDetailPage(cms, page, detailRes);

    }
}
