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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsFunctionReference;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

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
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
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
        String path = cms.getRequestContext().getUri();
        path = CmsFileUtil.removeTrailingSeparator(path);
        try {
            cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsSecurityException e) {
            // It may happen that a path is both an existing VFS path and a valid detail page link.
            // If this is the case, and the user has insufficient permissions to read the resource at the path,
            // no resource should be displayed, even if the user would have access to the detail page.
            return null;
        } catch (CmsException e) {
            // ignore
        }
        String detailName = CmsResource.getName(path);
        try {
            CmsUUID detailId = cms.readIdForUrlName(detailName);

            if (detailId != null) {
                // check existence / permissions
                CmsResource detailRes = null;
                CmsPermissionViolationException permissionDenied = null;
                try {
                    detailRes = cms.readResource(detailId, CmsResourceFilter.ignoreExpirationOffline(cms));
                } catch (CmsPermissionViolationException e) {
                    // we postpone the decision what to do with a permission violation until later (see below)
                    permissionDenied = e;
                }
                String detailPagePath = CmsResource.getFolderPath(path);
                CmsResource detailPage = cms.readDefaultFile(detailPagePath);
                if (permissionDenied != null) {
                    // If we got a permission violation while reading the detail content, we only want to rethrow it if the rest
                    // of the URL is actually plausibly a detail page. Otherwise, we return null, which will usually cause a HTTP
                    // 404 response status. This is to prevent broken links which accidentally end with a restricted detail content's
                    // mapped URL name from triggering a HTTP 401 status. E.g. https://server.com/nonexistent-page/secret, where
                    // there is no "nonexistent-page" folder and "secret" is the mapped URL name of a restricted content.
                    if ((detailPage != null) && OpenCms.getADEManager().isDetailPage(cms, detailPage)) {
                        throw permissionDenied;
                    } else {
                        LOG.debug(
                            "Swallowing CmsPermissionViolationException for detail content because the page ["
                                + detailPagePath
                                + "] is not a detail page.\nDefault file: "
                                + detailPage
                                + "\n",
                            permissionDenied);
                        return null;
                    }
                }
                if (!isValidDetailPage(cms, detailPage, detailRes)) {
                    return null;
                }
                if (res != null) {
                    // response will be null if this run through the init handler is only for determining the locale
                    req.setAttribute(ATTR_DETAIL_CONTENT_RESOURCE, detailRes);
                    cms.getRequestContext().setDetailResource(detailRes);
                }
                // set the resource path
                cms.getRequestContext().setUri(cms.getSitePath(detailPage));
                return detailPage;
            } else {
                CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(
                    cms,
                    cms.getRequestContext().addSiteRoot(path));
                // check if the detail name matches any named function
                for (CmsFunctionReference ref : configData.getFunctionReferences()) {
                    if (detailName.equals(ref.getName()) && (ref.getFunctionDefaultPageId() != null)) {
                        CmsResource detailPage = cms.readDefaultFile(CmsResource.getFolderPath(path));
                        if (OpenCms.getADEManager().isDetailPage(cms, detailPage)) {
                            if (res != null) {
                                // response will be null if this run through the init handler is only for determining the locale
                                CmsResource functionDefaultPage = cms.readResource(ref.getFunctionDefaultPageId());
                                req.setAttribute(ATTR_DETAIL_FUNCTION_PAGE, functionDefaultPage);
                                cms.getRequestContext().setDetailResource(functionDefaultPage);
                            }
                            // set the resource path
                            cms.getRequestContext().setUri(cms.getSitePath(detailPage));
                            return detailPage;
                        } else {
                            return null;
                        }

                    }
                }
            }
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

        return null;
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
