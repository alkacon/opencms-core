/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsDefaultFormatterHelper.java,v $
 * Date   : $Date: 2009/11/17 07:42:26 $
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

package org.opencms.workplace.galleries;

import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Helper bean to implement gallery default formatters.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.6
 * 
 */
public class CmsDefaultFormatterHelper extends CmsJspActionElement {

    private CmsGalleryItemBean m_galleryItem;

    private CmsResource m_resource;

    /**
     * Constructor, with parameters.
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsDefaultFormatterHelper(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Returns the current site entry.<p>
     * 
     * @return the current site entry
     */
    public CmsGalleryItemBean getGalleryItem() {

        if (m_galleryItem == null) {
            m_galleryItem = (CmsGalleryItemBean)getRequest().getAttribute(
                CmsGallerySearchServer.ReqParam.GALLERYITEM.getName());

        }
        return m_galleryItem;
    }

    /**
     * Returns the element's resource.<p>
     * 
     * @return the element's resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource getResource() throws CmsException {

        if (m_resource == null) {
            if (getGalleryItem().getResource() != null) {
                m_resource = getGalleryItem().getResource();
            } else if (!CmsStringUtil.isEmptyOrWhitespaceOnly(getGalleryItem().getPath())) {
                m_resource = getCmsObject().readResource(getGalleryItem().getPath());
            }
        }
        return m_resource;
    }

    /**
     * Returns the resource type icon path for the resource.<p>
     * 
     * @return the resource type icon path for the resource
     * 
     * @throws CmsException if something goes wrong 
     */
    public String getIconPath() throws CmsException {

        if (getGalleryItem().getIcon() != null) {
            return getGalleryItem().getIcon();
        }
        return CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES
            + OpenCms.getWorkplaceManager().getExplorerTypeSetting(getType()).getIcon());
    }

    /**
     * Returns the element's path.<p>
     * 
     * @return the element's path
     * 
     * @throws CmsException if something goes wrong
     */
    public String getPath() throws CmsException {

        if (getGalleryItem().getPath() != null) {
            return getGalleryItem().getPath();
        }
        return getCmsObject().getSitePath(getResource());
    }

    /**
     * Returns the element's resource type name.<p>
     * 
     * @return the element's resource type name
     * 
     * @throws CmsException if something goes wrong
     */
    public String getType() throws CmsException {

        if (getGalleryItem().getTypeName() != null) {
            return getGalleryItem().getTypeName();
        }
        return OpenCms.getResourceManager().getResourceType(getResource()).getTypeName();
    }

    /**
     * Returns the element's resource type id.<p>
     * 
     * @return the element's resource type id
     * 
     * @throws CmsException if something goes wrong
     */
    public int getTypeId() throws CmsException {

        if (getGalleryItem().getTypeId() != -5) {
            return getGalleryItem().getTypeId();
        }
        return OpenCms.getResourceManager().getResourceType(getResource()).getTypeId();
    }

    /**
     * Returns the element's resource type localized name.<p>
     * 
     * @return the element's resource type localized name
     * 
     * @throws CmsException if something goes wrong
     */
    public String getTypeName() throws CmsException {

        return org.opencms.workplace.CmsWorkplaceMessages.getResourceTypeName(
            getCmsObject().getRequestContext().getLocale(),
            getType());
    }

    /**
     * Returns the element title.<p>
     * 
     * @return the title
     * @throws CmsException if something goes wrong
     */
    public String getTitle() throws CmsException {

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(getGalleryItem().getTitle())) {
            return getGalleryItem().getTitle();
        }
        return getResource().getName();
    }

    /**
     * Returns the elements sub-title.<p>
     * 
     * @return the sub-title or <code>null</code> if not available
     */
    public String getSubTitle() {

        return getGalleryItem().getSubtitle();
    }

}
