/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/Attic/CmsContainerpageProvider.java,v $
 * Date   : $Date: 2010/05/03 07:53:47 $
 * Version: $Revision: 1.6 $
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

package org.opencms.ade.containerpage;

import org.opencms.ade.containerpage.shared.I_CmsContainerpageProviderConstants;
import org.opencms.ade.sitemap.CmsSitemapProvider;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.I_CmsCoreProvider;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.sitemap.CmsSitemapEntry;
import org.opencms.xml.sitemap.CmsXmlSitemap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Convenience class to provide server-side information to the client.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public final class CmsContainerpageProvider implements I_CmsContainerpageProviderConstants, I_CmsCoreProvider {

    /** The editor back-link URI. */
    private static final String BACKLINK_URI = "/system/modules/org.opencms.ade.containerpage/editor-backlink.html";

    /** The xml-content editor URI. */
    private static final String EDITOR_URI = "/system/workplace/editors/editor.jsp";

    /** Internal instance. */
    private static CmsContainerpageProvider INSTANCE;

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapProvider.class);

    /** The session cache. */
    private CmsADESessionCache m_sessionCache;

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private CmsContainerpageProvider() {

        // hide the constructor
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsContainerpageProvider get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsContainerpageProvider();
        }
        return INSTANCE;
    }

    /**
     * @see org.opencms.gwt.I_CmsCoreProvider#export(javax.servlet.http.HttpServletRequest)
     */
    public String export(HttpServletRequest request) {

        StringBuffer sb = new StringBuffer();
        sb.append(ClientMessages.get().export(request));
        sb.append(DICT_NAME.replace('.', '_')).append("=").append(getData(request).toString()).append(";");
        sb.append(KEY_CONTAINER_DATA).append("= new Array();");
        return sb.toString();
    }

    /**
     * @see org.opencms.gwt.I_CmsCoreProvider#exportAll(javax.servlet.http.HttpServletRequest)
     */
    public String exportAll(HttpServletRequest request) {

        StringBuffer sb = new StringBuffer();
        sb.append(org.opencms.gwt.CmsCoreProvider.get().export(request));
        sb.append(org.opencms.ade.publish.CmsPublishProvider.get().export(request));
        sb.append(org.opencms.ade.galleries.CmsGalleryProvider.get().export(request));
        sb.append(export(request));
        return sb.toString();
    }

    /**
     * @see org.opencms.gwt.I_CmsCoreProvider#getData(javax.servlet.http.HttpServletRequest)
     */
    public JSONObject getData(HttpServletRequest request) {

        CmsObject cms = CmsFlexController.getCmsObject(request);
        JSONObject keys = new JSONObject();
        try {
            CmsResource containerpage = getContainerpage(cms);
            keys.put(KEY_CURRENT_CONTAINERPAGE_URI, getContainerpageUri(cms, containerpage));
            keys.put(KEY_NO_EDIT_REASON, getNoEditReason(cms, containerpage));
            keys.put(KEY_REQUEST_PARAMS, getRequestParams(request));
            keys.put(KEY_SITEMAP_URI, getSitemapUri(cms, request));
            keys.put(KEY_EDITOR_URI, EDITOR_URI);
            keys.put(KEY_BACKLINK_URI, BACKLINK_URI);
            keys.put(KEY_TOOLBAR_VISIBLE, isToolbarVisible(cms, request));
        } catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
            try {
                keys.put("error", e.getLocalizedMessage());
            } catch (JSONException e1) {
                // ignore, should never happen
                LOG.error(e1.getLocalizedMessage(), e1);
            }
        }
        return keys;
    }

    /**
     * Returns the requested container-page resource.<p>
     * 
     * @param cms the current cms object
     * 
     * @return the container-page resource
     * 
     * @throws CmsException if the resource could not be read for any reason
     */
    private CmsResource getContainerpage(CmsObject cms) throws CmsException {

        String currentUri = cms.getRequestContext().getUri();
        CmsResource containerPage = cms.readResource(currentUri);
        if (!CmsResourceTypeXmlContainerPage.isContainerPage(containerPage)) {
            // container page is used as template
            String cntPagePath = cms.readPropertyObject(
                containerPage,
                CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS,
                true).getValue("");
            try {
                containerPage = cms.readResource(cntPagePath);
            } catch (CmsException e) {
                if (!LOG.isDebugEnabled()) {
                    LOG.warn(e.getLocalizedMessage());
                }
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return containerPage;
    }

    /**
     * Returns the URI for the given container-page.<p>
     * 
     * @param cms the current cms object
     * @param containerPage the container-page resource
     * 
     * @return the URI string
     */
    private String getContainerpageUri(CmsObject cms, CmsResource containerPage) {

        return cms.getSitePath(containerPage);
    }

    /**
     * Returns the no-edit reason for the given resource.<p>
     * 
     * @param cms the current cms object
     * @param containerPage the resource
     * 
     * @return the no-edit reason, empty if editing is allowed
     * 
     * @throws CmsException is something goes wrong
     */
    private String getNoEditReason(CmsObject cms, CmsResource containerPage) throws CmsException {

        String reason = new CmsResourceUtil(cms, containerPage).getNoEditReason(OpenCms.getWorkplaceManager().getWorkplaceLocale(
            cms));

        return CmsStringUtil.escapeJavaScript(reason);
    }

    /**
     * Returns the request-parameters for the given request.<p>
     * 
     * @param request the current request
     * 
     * @return the parameters
     */
    private String getRequestParams(HttpServletRequest request) {

        return CmsRequestUtil.encodeParams(request);
    }

    /**
     * Returns the session cache.<p>
     * 
     * @return the session cache
     */
    private CmsADESessionCache getSessionCache(CmsObject cms, HttpServletRequest request) {

        if (m_sessionCache == null) {
            m_sessionCache = (CmsADESessionCache)request.getSession().getAttribute(
                CmsADESessionCache.SESSION_ATTR_ADE_CACHE);
            if (m_sessionCache == null) {
                m_sessionCache = new CmsADESessionCache(cms);
                request.getSession().setAttribute(CmsADESessionCache.SESSION_ATTR_ADE_CACHE, m_sessionCache);
            }
        }
        return m_sessionCache;
    }

    /**
     * Returns the sitemap URI of this request.<p>
     * 
     * @param cms the current cms object
     * @param request the current request
     * 
     * @return the sitemap URI, empty if none available
     * 
     * @throws CmsException if something goes wrong
     */
    private String getSitemapUri(CmsObject cms, HttpServletRequest request) throws CmsException {

        CmsXmlSitemap sitemap = null;
        CmsSitemapEntry sitemapInfo = OpenCms.getSitemapManager().getRuntimeInfo(request);
        if (sitemapInfo != null) {
            sitemap = OpenCms.getSitemapManager().getSitemapForUri(cms, sitemapInfo.getSitePath(cms), false);
        }
        return (sitemap == null) ? "" : OpenCms.getLinkManager().substituteLink(cms, sitemap.getFile());
    }

    /**
     * Returns the tool-bar visibility.<p>
     * 
     * @param cms the current cms object
     * @param request the current request
     * 
     * @return <code>true</code> if the tool-bar is visible
     * 
     * @throws CmsException if something goes wrong
     */
    private boolean isToolbarVisible(CmsObject cms, HttpServletRequest request) {

        return getSessionCache(cms, request).isToolbarVisible();
    }
}
