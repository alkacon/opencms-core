/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsDefaultFormatterHelper.java,v $
 * Date   : $Date: 2009/11/12 12:47:21 $
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

import org.opencms.jsp.CmsJspActionElement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Helper bean to implement gallery default formatters.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.6
 * 
 */
public class CmsDefaultFormatterHelper extends CmsJspActionElement {

    private CmsGalleryItemBean m_galleryItem;

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

}
