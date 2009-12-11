/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/sitemap/Attic/CmsSitemapActionElement.java,v $
 * Date   : $Date: 2009/12/11 08:30:11 $
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

package org.opencms.workplace.editors.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.json.JSONArray;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.galleries.CmsGallerySearchServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Sitemap server used for client/server communication.<p>
 * 
 * see jsp file <tt>/system/workplace/editors/sitemap/server.jsp</tt>.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 7.6
 */
public class CmsSitemapActionElement extends CmsJspActionElement {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapActionElement.class);

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsSitemapActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Returns the reason why you are not allowed to edit the current resource.<p>
     * 
     * @return an empty string if editable, the reason if not
     * 
     * @throws CmsException if something goes wrong
     */
    public String getNoEditReason() throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, getResource());
        return CmsEncoder.escapeHtml(resUtil.getNoEditReason(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)));
    }

    /**
     * Returns the sitemap URI, taken into account history requests.<p>
     * 
     * @return the sitemap URI
     */
    public String getSitemapURI() {

        return CmsHistoryResourceHandler.getHistoryResourceURI(
            getCmsObject().getRequestContext().getUri(),
            getRequest());
    }

    /**
     * Checks if the toolbar should be displayed.<p>
     * 
     * @return <code>true</code> if the toolbar should be displayed
     */
    public boolean isDisplayToolbar() {

        // display the toolbar by default
        boolean displayToolbar = true;
        if (CmsHistoryResourceHandler.isHistoryRequest(getRequest())) {
            // we do not want to display the toolbar in case of an historical request
            displayToolbar = false;
        }
        return displayToolbar;
    }

    /**
     * Returns the current resource, taken into account historical requests.<p>
     * 
     * @return the current resource
     * 
     * @throws CmsException if something goes wrong
     */
    private CmsResource getResource() throws CmsException {

        CmsResource resource = (CmsResource)CmsHistoryResourceHandler.getHistoryResource(getRequest());
        if (resource == null) {
            resource = getCmsObject().readResource(getCmsObject().getRequestContext().getUri());
        }
        return resource;
    }

    /**
     * Returns the URI for the gallery server JSP.<p>
     * 
     * @return the URI for the gallery server JSP
     */
    public String getGalleryServerUri() {

        CmsLinkManager linkMan = OpenCms.getLinkManager();
        String galleryServerUri = linkMan.substituteLink(getCmsObject(), CmsGallerySearchServer.ADVANCED_GALLERY_PATH);
        return galleryServerUri;
    }

    /**
     * Returns the string representation of the JSON array containing the searchable resource type id's.<p>
     * 
     * @return the resource type id's
     */
    public String getSearchableResourceTypeIds() {

        JSONArray types = new JSONArray();
        for (I_CmsResourceType type : CmsSitemapServer.getSearchableResourceTypes()) {
            types.put(type.getTypeId());
        }
        return types.toString();
    }

    /**
     * Returns script tags for resource type specific handling of the gallery preview.<p>
     * 
     * @return the script tags
     */
    public String getAdditionalGalleryJavascript() {

        return CmsGallerySearchServer.getAdditionalJavascriptForTypes(CmsSitemapServer.getSearchableResourceTypes());
    }

}
