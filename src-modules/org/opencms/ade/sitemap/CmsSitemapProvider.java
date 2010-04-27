/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/Attic/CmsSitemapProvider.java,v $
 * Date   : $Date: 2010/04/27 12:43:24 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.sitemap;

import org.opencms.ade.sitemap.shared.I_CmsSitemapProviderConstants;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.I_CmsCoreProvider;
import org.opencms.i18n.CmsEncoder;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.explorer.CmsResourceUtil;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Convenience class to provide sitemap server-side data to the client.<p> 
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.client.CmsSitemapProvider
 */
public final class CmsSitemapProvider implements I_CmsSitemapProviderConstants, I_CmsCoreProvider {

    /** Internal instance. */
    private static CmsSitemapProvider INSTANCE;

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapProvider.class);

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private CmsSitemapProvider() {

        // hide the constructor
    }

    /**
     * Returns the client message instance.<p>
     * 
     * @return the client message instance
     */
    public static CmsSitemapProvider get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsSitemapProvider();
        }
        return INSTANCE;
    }

    /**
     * @see org.opencms.gwt.I_CmsCoreProvider#export(javax.servlet.http.HttpServletRequest)
     */
    public String export(HttpServletRequest request) {

        StringBuffer sb = new StringBuffer();
        sb.append(DICT_NAME.replace('.', '_')).append("=").append(getData(request).toString()).append(";");
        sb.append(ClientMessages.get().export(request));
        return sb.toString();
    }

    /**
     * @see org.opencms.gwt.I_CmsCoreProvider#exportAll(javax.servlet.http.HttpServletRequest)
     */
    public String exportAll(HttpServletRequest request) {

        StringBuffer sb = new StringBuffer();
        sb.append(org.opencms.gwt.CmsCoreProvider.get().export(request));
        sb.append(org.opencms.ade.publish.CmsPublishProvider.get().export(request));
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
            keys.put(KEY_EDIT, getNoEditReason(cms, request));
            keys.put(KEY_TOOLBAR, isDisplayToolbar(request));
            String sitemapUri = getSitemapURI(cms, request);
            keys.put(KEY_URI_SITEMAP, sitemapUri);
            keys.put(KEY_TYPE_CNTPAGE, OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeXmlContainerPage.getStaticTypeName()).getTypeId());
            CmsResource parentSitemap = OpenCms.getSitemapManager().getParentSitemap(
                cms,
                CmsHistoryResourceHandler.getResourceWithHistory(cms, sitemapUri));
            if (parentSitemap != null) {
                keys.put(KEY_URI_PARENT, cms.getSitePath(parentSitemap));
            }
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
     * Returns the reason why you are not allowed to edit the current resource.<p>
     * 
     * @param cms the current cms object
     * @param request the current request to get the default locale from 
     * 
     * @return an empty string if editable, the reason if not
     * 
     * @throws CmsException if something goes wrong
     */
    private String getNoEditReason(CmsObject cms, HttpServletRequest request) throws CmsException {

        CmsResourceUtil resUtil = new CmsResourceUtil(cms, getResource(cms, request));
        return CmsEncoder.escapeHtml(resUtil.getNoEditReason(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)));
    }

    /**
     * Returns the current resource, taken into account historical requests.<p>
     * 
     * @param cms the current cms object
     * @param request the current request to get the default locale from 
     * 
     * @return the current resource
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsResource getResource(CmsObject cms, HttpServletRequest request) throws CmsException {

        CmsResource resource = (CmsResource)CmsHistoryResourceHandler.getHistoryResource(request);
        if (resource == null) {
            resource = cms.readResource(cms.getRequestContext().getUri());
        }
        return resource;
    }

    /**
     * Returns the sitemap URI, taken into account history requests.<p>
     * 
     * @param cms the current cms object
     * @param request the current request to get the default locale from 
     * 
     * @return the sitemap URI
     */
    private String getSitemapURI(CmsObject cms, HttpServletRequest request) {

        return CmsHistoryResourceHandler.getHistoryResourceURI(cms.getRequestContext().getUri(), request);
    }

    /**
     * Checks if the toolbar should be displayed.<p>
     * 
     * @param request the current request to get the default locale from 
     * 
     * @return <code>true</code> if the toolbar should be displayed
     */
    private boolean isDisplayToolbar(HttpServletRequest request) {

        // display the toolbar by default
        boolean displayToolbar = true;
        if (CmsHistoryResourceHandler.isHistoryRequest(request)) {
            // we do not want to display the toolbar in case of an historical request
            displayToolbar = false;
        }
        return displayToolbar;
    }
}
