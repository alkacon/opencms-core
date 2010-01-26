/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsContainerPageFormatterHelper.java,v $
 * Date   : $Date: 2010/01/26 09:34:18 $
 * Version: $Revision: 1.1 $
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
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Helper bean to implement gallery preview container page formatters.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.9
 * 
 */
public class CmsContainerPageFormatterHelper extends CmsJspActionElement {

    /** The gallery item info bean. */
    private CmsGalleryItemBean m_galleryItem;

    /** The resource. */
    private CmsResource m_resource;

    /** The xml container page. */
    private CmsContainerPageBean m_containerPageBean;

    /**
     * Constructor, with parameters.
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsContainerPageFormatterHelper(PageContext context, HttpServletRequest req, HttpServletResponse res) {

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
                CmsGallerySearchServer.ReqParam.galleryitem.toString());

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
     * Returns the container page bean for this resource.<p>
     * 
     * @return the container page bean
     * @throws CmsException if something goes wrong unmarshalling the container page
     */
    public CmsContainerPageBean getContainerPage() throws CmsException {

        if (m_containerPageBean == null) {
            CmsXmlContainerPage page = CmsXmlContainerPageFactory.unmarshal(getCmsObject(), getResource());
            m_containerPageBean = page.getCntPage(getCmsObject(), getCmsObject().getRequestContext().getLocale());
        }
        return m_containerPageBean;
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

}
