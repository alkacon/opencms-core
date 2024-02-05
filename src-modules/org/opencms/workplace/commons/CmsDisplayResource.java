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

package org.opencms.workplace.commons;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Shows a preview of the selected resource in the Explorer view.<p>
 *
 * This is required to get correct previews of statically exported pages
 * in the Online project.<p>
 *
 * The following file uses this class:
 * <ul>
 * <li>/commons/displayresource.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsDisplayResource extends CmsDialog {

    /** Request parameter name for versionid. */
    public static final String PARAM_VERSION = "version";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDisplayResource.class);

    /** The version number parameter. */
    private String m_paramVersion;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsDisplayResource(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDisplayResource(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the content of an historical resource.<p>
     *
     * @param cms a CmsObject
     * @param resource the name of the historical resource
     * @param version the version number of the historical resource
     *
     * @return the content of an historical resource
     */
    protected static byte[] getHistoricalResourceContent(CmsObject cms, String resource, String version) {

        if (CmsStringUtil.isNotEmpty(resource) && CmsStringUtil.isNotEmpty(version)) {
            // try to load the historical resource
            I_CmsHistoryResource res = null;
            String storedSiteRoot = cms.getRequestContext().getSiteRoot();
            try {
                cms.getRequestContext().setSiteRoot("/");
                res = cms.readResource(
                    cms.readResource(resource, CmsResourceFilter.ALL).getStructureId(),
                    Integer.parseInt(version));
            } catch (CmsException e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
                return "".getBytes();
            } finally {
                cms.getRequestContext().setSiteRoot(storedSiteRoot);
            }
            if (res.isFile()) {
                byte[] historyResourceContent = ((CmsFile)res).getContents();
                if ((historyResourceContent == null) || (historyResourceContent.length == 0)) {
                    try {
                        CmsFile file = cms.readFile((CmsResource)res);
                        historyResourceContent = file.getContents();
                    } catch (CmsException e) {
                        // ignore
                    }
                }
                historyResourceContent = CmsEncoder.changeEncoding(
                    historyResourceContent,
                    OpenCms.getSystemInfo().getDefaultEncoding(),
                    cms.getRequestContext().getEncoding());
                return historyResourceContent;
            }
        }
        return "".getBytes();
    }

    /**
     * Redirects to the specified file or shows an historical resource.<p>
     *
     * @throws Exception if redirection fails
     */
    public void actionShow() throws Exception {

        // try to load the historical resource
        if (CmsStringUtil.isNotEmpty(getParamVersion())) {
            showHistoricVersion();
        } else {
            String resourceStr = getParamResource();
            // trying to read the resource
            CmsResource resource = readResource(resourceStr);
            if (isDeleted(resource)) {
                // resource has been deleted in offline project
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_RESOURCE_DELETED_2,
                    resourceStr,
                    getCms().getRequestContext().getCurrentProject().getName()));
            }

            // check for release / expiration time window
            autoTimeWarp(resource);

            // code for display resource after all tests for displayability (exists, not deleted)
            //            if (CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
            //                // if we have a container page look for the first sitemap entry
            //                // and use that to display the container page
            //                List<CmsInternalSitemapEntry> entries = OpenCms.getSitemapManager().getEntriesForStructureId(
            //                    getJsp().getCmsObject(),
            //                    resource.getStructureId());
            //                if (!entries.isEmpty()) {
            //                    CmsInternalSitemapEntry entry = entries.get(0);
            //                    resourceStr = entry.getRootPath();
            //                }
            //            }
            if (OpenCms.getSiteManager().isSharedFolder(getCms().getRequestContext().getSiteRoot())) {
                if (!OpenCms.getSiteManager().startsWithShared(resourceStr)) {
                    resourceStr = CmsStringUtil.joinPaths(OpenCms.getSiteManager().getSharedFolder(), resourceStr);
                }
            }
            String url = getJsp().link(resourceStr);
            // if in online project
            if ((url.indexOf("://") < 0) && getCms().getRequestContext().getCurrentProject().isOnlineProject()) {
                url = prependSiteRoot(url);
            }
            getJsp().getResponse().sendRedirect(url);
        }
    }

    /**
     * Returns the version number parameter value.<p>
     *
     * @return the version number parameter value
     */
    public String getParamVersion() {

        return m_paramVersion;
    }

    /**
     * Sets the version number parameter value.<p>
     *
     * @param paramVersion the version number parameter value to set
     */
    public void setParamVersionid(String paramVersion) {

        m_paramVersion = paramVersion;
    }

    /**
     * Performs a timewarp for resources that are expired or not released yet to always allow a
     * preview of a page out of the workplace.<p>
     *
     * If the user has a configured timewarp (preferences dialog) a mandatory timewarp will lead to
     * an exception. One cannot auto timewarp with configured timewarp time.<p>
     *
     * @param resource the resource to show
     *
     * @throws CmsVfsResourceNotFoundException if a warp would be needed to show the resource but the user has a configured
     *      timewarp which disallows auto warping
     */
    protected void autoTimeWarp(CmsResource resource) throws CmsVfsResourceNotFoundException {

        long surfTime = getCms().getRequestContext().getRequestTime();
        if (resource.isReleasedAndNotExpired(surfTime)) {
            // resource is valid, no modification of time required
            return;
        }

        if (getSettings().getUserSettings().getTimeWarp() == CmsContextInfo.CURRENT_TIME) {
            // no time warp has been set, enable auto time warp
            long timeWarp;
            // will also work if ATTRIBUTE_REQUEST_TIME was CmsResource.DATE_RELEASED_EXPIRED_IGNORE
            if (resource.isExpired(surfTime)) {
                // do a time warp into the past
                timeWarp = resource.getDateExpired() - 1;
            } else if (!resource.isReleased(surfTime)) {
                // do a time warp into the future
                timeWarp = resource.getDateReleased() + 1;
            } else {
                // do no time warp
                timeWarp = CmsContextInfo.CURRENT_TIME;
            }
            if (timeWarp != CmsContextInfo.CURRENT_TIME) {
                // let's do the time warp again...
                getSession().setAttribute(CmsContextInfo.ATTRIBUTE_REQUEST_TIME, Long.valueOf(timeWarp));
            }
        } else {
            // resource is not vaild in the time window set by the user,
            // report an error message
            throw new CmsVfsResourceNotFoundException(
                Messages.get().container(Messages.ERR_RESOURCE_OUTSIDE_TIMEWINDOW_1, getParamResource()));
        }
    }

    /**
     * @see org.opencms.workplace.CmsDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        fillParamValues(settings, request);
    }

    /**
     * Returns if the given resource has the state "deleted".<p>
     *
     * @param resource the resource
     *
     * @return <code>true</code> if the resource is of state "deleted"
     */
    private boolean isDeleted(CmsResource resource) {

        return resource.getState().isDeleted();
    }

    /**
     * Prepends the site-root to the given URL.<p>
     *
     * @param url the URL
     *
     * @return the absolute URL
     */
    private String prependSiteRoot(String url) {

        String site = getCms().getRequestContext().getSiteRoot();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(site) || OpenCms.getSiteManager().isSharedFolder(site)) {
            site = OpenCms.getSiteManager().getDefaultUri();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(site)
                || (OpenCms.getSiteManager().getSiteForSiteRoot(site) == null)) {
                return OpenCms.getSiteManager().getWorkplaceServer() + url;
            } else {
                return OpenCms.getSiteManager().getSiteForSiteRoot(site).getUrl() + url;
            }
        }
        return OpenCms.getSiteManager().getSiteForSiteRoot(site).getUrl() + url;
    }

    /**
     * Reads the resource from the DB.<p>
     *
     * @param resourceName the resource name
     *
     * @return the resource
     *
     * @throws CmsException if the resource can not be read
     */
    private CmsResource readResource(String resourceName) throws CmsException {

        CmsResource resource = null;
        try {
            resource = getCms().readResource(resourceName, CmsResourceFilter.ALL);
        } catch (CmsVfsResourceNotFoundException e) {
            throw new CmsVfsResourceNotFoundException(
                Messages.get().container(
                    Messages.ERR_RESOURCE_DOES_NOT_EXIST_3,
                    resourceName,
                    getCms().getRequestContext().getCurrentProject().getName(),
                    getCms().getRequestContext().getSiteRoot()),
                e);
        }
        return resource;
    }

    /**
     * Displays the requested historic version of the resource.<p>
     */
    private void showHistoricVersion() {

        String resourceStr = getParamResource();
        byte[] result = getHistoricalResourceContent(getCms(), resourceStr, getParamVersion());
        if (result != null) {
            // get the top level response to change the content type
            String contentType = OpenCms.getResourceManager().getMimeType(
                resourceStr,
                getCms().getRequestContext().getEncoding());

            HttpServletResponse res = getJsp().getResponse();
            HttpServletRequest req = getJsp().getRequest();

            res.setHeader(
                CmsRequestUtil.HEADER_CONTENT_DISPOSITION,
                new StringBuffer("attachment; filename=\"").append(resourceStr).append("\"").toString());
            res.setContentLength(result.length);

            CmsFlexController controller = CmsFlexController.getController(req);
            res = controller.getTopResponse();
            res.setContentType(contentType);

            try {
                res.getOutputStream().write(result);
                res.getOutputStream().flush();
            } catch (IOException e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
            }
        }
    }
}