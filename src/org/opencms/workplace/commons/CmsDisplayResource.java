/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsDisplayResource.java,v $
 * Date   : $Date: 2011/03/23 14:50:44 $
 * Version: $Revision: 1.30 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.30 $ 
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
        String resourceStr = getParamResource();
        if (CmsStringUtil.isNotEmpty(getParamVersion())) {
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
                    return;
                }
            }
        } else {

            // manual existsResource, need it for timewarp checking and saves three calls for existsResource
            CmsResource resource = null;
            try {
                resource = getCms().readResource(resourceStr, CmsResourceFilter.ALL);
            } catch (CmsVfsResourceNotFoundException e) {
                // ignore, resource will be null
            }

            if (resource != null) {
                if (resource.getState().isDeleted()) {
                    // resource has been deleted in offline project
                    throw new CmsVfsResourceNotFoundException(Messages.get().container(
                        Messages.ERR_RESOURCE_DELETED_2,
                        resourceStr,
                        getCms().getRequestContext().currentProject().getName()));
                }

                // check for release / expiration time window 
                autoTimeWarp(resource);

                // code for display resource after all tests for displayability (exists, not deleted)
                String url = getJsp().link(resourceStr);
                // if in online project
                if ((url.indexOf("://") < 0) && getCms().getRequestContext().currentProject().isOnlineProject()) {
                    String site = getCms().getRequestContext().getSiteRoot();
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(site)) {
                        site = OpenCms.getSiteManager().getDefaultUri();
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(site)) {
                            url = OpenCms.getSiteManager().getWorkplaceServer() + url;
                        } else if (OpenCms.getSiteManager().getSiteForSiteRoot(site) == null) {
                            url = OpenCms.getSiteManager().getWorkplaceServer() + url;
                        } else {
                            url = OpenCms.getSiteManager().getSiteForSiteRoot(site).getUrl() + url;
                        }
                    } else {
                        url = OpenCms.getSiteManager().getSiteForSiteRoot(site).getUrl() + url;
                    }
                    try {
                        CmsStaticExportManager manager = OpenCms.getStaticExportManager();
                        HttpURLConnection.setFollowRedirects(false);
                        // try to export it
                        URL exportUrl = new URL(manager.getExportUrl() + manager.getRfsName(getCms(), resourceStr));
                        HttpURLConnection urlcon = (HttpURLConnection)exportUrl.openConnection();
                        // setup the connection and request the resource
                        urlcon.setRequestMethod("GET");
                        urlcon.setRequestProperty(CmsRequestUtil.HEADER_OPENCMS_EXPORT, Boolean.TRUE.toString());
                        if (manager.getAcceptLanguageHeader() != null) {
                            urlcon.setRequestProperty(
                                CmsRequestUtil.HEADER_ACCEPT_LANGUAGE,
                                manager.getAcceptLanguageHeader());
                        } else {
                            urlcon.setRequestProperty(
                                CmsRequestUtil.HEADER_ACCEPT_LANGUAGE,
                                manager.getDefaultAcceptLanguageHeader());
                        }
                        if (manager.getAcceptCharsetHeader() != null) {
                            urlcon.setRequestProperty(
                                CmsRequestUtil.HEADER_ACCEPT_CHARSET,
                                manager.getAcceptCharsetHeader());
                        } else {
                            urlcon.setRequestProperty(
                                CmsRequestUtil.HEADER_ACCEPT_CHARSET,
                                manager.getDefaultAcceptCharsetHeader());
                        }
                        // now perform the request to export
                        urlcon.connect();
                        urlcon.getResponseCode();
                        urlcon.disconnect();

                    } catch (Exception e) {
                        // ignore
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(e.getLocalizedMessage(), e);
                        }
                    }
                }
                getJsp().getResponse().sendRedirect(url);
            } else {
                // resource does not exist, show error message
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_RESOURCE_DOES_NOT_EXIST_3,
                    resourceStr,
                    getCms().getRequestContext().currentProject().getName(),
                    getCms().getRequestContext().getSiteRoot()));
            }
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
                getSession().setAttribute(CmsContextInfo.ATTRIBUTE_REQUEST_TIME, new Long(timeWarp));
            }
        } else {
            // resource is not vaild in the time window set by the user,
            // report an error message
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_RESOURCE_OUTSIDE_TIMEWINDOW_1,
                getParamResource()));
        }
    }

    /**
     * @see org.opencms.workplace.CmsDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        fillParamValues(settings, request);
    }
}