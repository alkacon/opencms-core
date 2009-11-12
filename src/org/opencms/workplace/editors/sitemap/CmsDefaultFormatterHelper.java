/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/sitemap/Attic/CmsDefaultFormatterHelper.java,v $
 * Date   : $Date: 2009/11/12 12:47:21 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.editors.sitemap;

import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.containerpage.CmsADEManager;
import org.opencms.xml.sitemap.CmsSiteEntryBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Helper bean to implement sitemap default formatters.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 7.9.2
 */
public class CmsDefaultFormatterHelper extends CmsJspActionElement {

    /** The current entry. */
    private CmsSiteEntryBean m_entry;

    /** The entry's resource. */
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
    public CmsSiteEntryBean getEntry() {

        if (m_entry == null) {
            m_entry = (CmsSiteEntryBean)getRequest().getAttribute(CmsADEManager.ATTR_CURRENT_ELEMENT);

        }
        return m_entry;
    }

    /**
     * Returns the resource type icon path for the resource.<p>
     * 
     * @return the resource type icon path for the resource
     * 
     * @throws CmsException if something goes wrong 
     */
    public String getIconPath() throws CmsException {

        return CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES
            + OpenCms.getWorkplaceManager().getExplorerTypeSetting(getType()).getIcon());
    }

    /**
     * Returns the entry's path.<p>
     * 
     * @return the entry's path
     * 
     * @throws CmsException if something goes wrong
     */
    public String getPath() throws CmsException {

        return getCmsObject().getSitePath(getResource());
    }

    /**
     * Returns the entry's resource.<p>
     * 
     * @return the entry's resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource getResource() throws CmsException {

        if (m_resource == null) {
            m_resource = getCmsObject().readResource(getEntry().getResourceId());
        }

        return m_resource;
    }

    /**
     * Returns the entry's resource type name.<p>
     * 
     * @return the entry's resource type name
     * 
     * @throws CmsException if something goes wrong
     */
    public String getType() throws CmsException {

        return OpenCms.getResourceManager().getResourceType(getResource()).getTypeName();
    }

    /**
     * Returns the entry's resource type localized name.<p>
     * 
     * @return the entry's resource type localized name
     * 
     * @throws CmsException if something goes wrong
     */
    public String getTypeName() throws CmsException {

        return org.opencms.workplace.CmsWorkplaceMessages.getResourceTypeName(
            getCmsObject().getRequestContext().getLocale(),
            getType());
    }
}
