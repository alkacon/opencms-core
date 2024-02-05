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

package org.opencms.file.history;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.Messages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.util.CmsRequestUtil;

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Resource init handler that loads historical versions of resources.<p>
 *
 * @since 6.9.1
 */
public class CmsHistoryResourceHandler implements I_CmsResourceInit {

    /** Constant for the historical version request attribute name. */
    public static final String ATTRIBUTE_NAME = "org.opencms.file.history.CmsHistoryResourceHandler";

    /** The historical version handler path. */
    public static final String HISTORY_HANDLER = "/system/config/showversion";

    /** Request parameter name for the version number. */
    public static final String PARAM_VERSION = "version";

    /** Constant for the offline project version. */
    public static final int PROJECT_OFFLINE_VERSION = Integer.MAX_VALUE;

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHistoryResourceHandler.class);

    /**
     * Returns the historical version of a resource,
     * if the given request is displaying a history version.<p>
     *
     * @param req the request to check
     *
     * @return the historical resource if the given request is displaying an historical version
     */
    public static I_CmsHistoryResource getHistoryResource(ServletRequest req) {

        return (I_CmsHistoryResource)req.getAttribute(ATTRIBUTE_NAME);
    }

    /**
     * Appends the <code>version</code> parameter to the URI if needed.<p>
     *
     * @param uri the resource URI
     * @param req the current request
     *
     * @return the same URI, with additional parameters in case of a historical request
     */
    public static String getHistoryResourceURI(String uri, ServletRequest req) {

        String histUri = uri;
        if (CmsHistoryResourceHandler.isHistoryRequest(req)) {
            String version = req.getParameter(CmsHistoryResourceHandler.PARAM_VERSION);
            histUri = CmsRequestUtil.appendParameter(uri, CmsHistoryResourceHandler.PARAM_VERSION, version);
        }
        return histUri;
    }

    /**
     * Returns the correct resource for the given URI, taken into account historical versions
     * marked by the <code>version</code> parameter.<p>
     *
     * @param cms the current CMS context
     * @param resourceUri the resource URI
     *
     * @return the resource, which can be an instance of {@link org.opencms.file.history.I_CmsHistoryResource}
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsResource getResourceWithHistory(CmsObject cms, String resourceUri) throws CmsException {

        CmsResource resource = null;

        if (resourceUri.contains(CmsRequestUtil.URL_DELIMITER)) {
            int pos = resourceUri.indexOf(CmsRequestUtil.URL_DELIMITER);
            Map<String, String[]> params = CmsRequestUtil.createParameterMap(resourceUri.substring(pos));
            if (params.containsKey(CmsHistoryResourceHandler.PARAM_VERSION)) {
                int version = Integer.parseInt(params.get(CmsHistoryResourceHandler.PARAM_VERSION)[0]);
                String sitemapPath = resourceUri.substring(0, pos);
                resource = cms.readResource(sitemapPath);
                resource = (CmsResource)cms.readResource(resource.getStructureId(), version);
            }
        }
        if (resource == null) {
            resource = cms.readResource(resourceUri);
        }
        return resource;
    }

    /**
     * Returns <code>true</code> if the given request is displaying an historical version.<p>
     *
     * @param req the request to check
     *
     * @return <code>true</code> if the given request is displaying a historical version
     */
    public static boolean isHistoryRequest(ServletRequest req) {

        return (null != req.getAttribute(ATTRIBUTE_NAME));
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

        // we only have to check for history resources if the handler was called
        // during a real request and NOT during a dummy-request while doing
        // a static export
        if (req != null) {
            String uri = cms.getRequestContext().getUri();
            // check if the resource starts with the HISTORY_HANDLER
            if (uri.startsWith(HISTORY_HANDLER)) {
                String version = req.getParameter(PARAM_VERSION);

                // only do something if the resource was not found and there was a "versionid" parameter included
                if ((resource == null) && (version != null)) {

                    // test if the current user is allowed to read historical versions of resources
                    // this can be done by trying to read the history handler resource
                    if (cms.existsResource(HISTORY_HANDLER)) {
                        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
                        try {
                            // we now must switch to the root site to read the history resource
                            cms.getRequestContext().setSiteRoot("/");

                            // extract the "real" resourcename
                            uri = uri.substring(HISTORY_HANDLER.length(), uri.length());
                            int id = Integer.valueOf(version).intValue();
                            if (id == CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION) {
                                resource = new CmsHistoryFile(cms.readFile(uri, CmsResourceFilter.IGNORE_EXPIRATION));
                            } else {
                                // get the current resource
                                CmsResource currRes = cms.readResource(uri, CmsResourceFilter.IGNORE_EXPIRATION);
                                // get the historical version of the resource
                                CmsHistoryFile hisRes = (CmsHistoryFile)cms.readResource(
                                    cms.readResource(uri, CmsResourceFilter.IGNORE_EXPIRATION).getStructureId(),
                                    id);

                                // the resource root path is not changed after the resource is moved or renamed
                                // change the resource root path to current root path, so e.g. properties can be read if necessary
                                if (!currRes.getRootPath().equals(hisRes.getRootPath())) {

                                    resource = new CmsHistoryFile(
                                        hisRes.getPublishTag(),
                                        hisRes.getStructureId(),
                                        hisRes.getResourceId(),
                                        currRes.getRootPath(),
                                        hisRes.getTypeId(),
                                        hisRes.getFlags(),
                                        hisRes.getProjectLastModified(),
                                        hisRes.getState(),
                                        hisRes.getDateCreated(),
                                        hisRes.getUserCreated(),
                                        hisRes.getDateLastModified(),
                                        hisRes.getUserLastModified(),
                                        hisRes.getDateReleased(),
                                        hisRes.getDateExpired(),
                                        hisRes.getLength(),
                                        hisRes.getDateContent(),
                                        hisRes.getVersion(),
                                        hisRes.getParentId(),
                                        null,
                                        hisRes.getResourceVersion(),
                                        hisRes.getStructureVersion());
                                } else {
                                    resource = hisRes;
                                }
                                if (resource instanceof CmsHistoryFile) {
                                    try {
                                        resource = cms.readFile(resource);
                                    } catch (Exception e) {
                                        LOG.info(e.getLocalizedMessage(), e);
                                    }
                                }
                            }
                            if (res != null) {
                                // store a request attribute to indicate that this is in fact a historical version
                                req.setAttribute(ATTRIBUTE_NAME, resource);
                            }
                        } catch (CmsException e) {
                            if (LOG.isErrorEnabled()) {
                                LOG.error(Messages.get().getBundle().key(Messages.ERR_HISTORYRESOURCE_2, uri, version));
                            }
                            throw new CmsResourceInitException(
                                Messages.get().container(Messages.ERR_SHOWVERSION_2, uri, version),
                                e);
                        } finally {
                            // restore the siteroot and modify the uri to the one of the correct resource
                            cms.getRequestContext().setSiteRoot(storedSiteRoot);
                            if (resource != null) {
                                // resource may be null in case of an error
                                cms.getRequestContext().setUri(cms.getSitePath(resource));
                            }
                        }
                    }
                }
            }
        }
        return resource;
    }
}