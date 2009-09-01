/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsDefaultFormatterHelper.java,v $
 * Date   : $Date: 2009/09/01 13:15:26 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Helper bean to implement default formatters that differentiate between new and existing elements.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.6 
 */
public class CmsDefaultFormatterHelper extends CmsJspActionElement {

    /** The element's resource. */
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
     * Returns the configured folder for new elements.<p> 
     * 
     * @return the configured folder for new elements
     * 
     * @throws CmsException if something goes wrong
     */
    public String getNewFolder() throws CmsException {

        if (!isNew()) {
            return "";
        }
        CmsObject cms = getCmsObject();
        CmsResource cntPageRes = cms.readResource(getRequest().getParameter(CmsADEServer.PARAMETER_URL));
        CmsContainerPageBean cntPage = CmsContainerPageCache.getInstance().getCache(
            cms,
            cntPageRes,
            cms.getRequestContext().getLocale());
        CmsElementCreator ec = new CmsElementCreator(cms, cntPage.getNewConfig());
        CmsTypeConfigurationItem tc = ec.getConfiguration().get(getType());
        return tc.getFolder();
    }

    /**
     * Returns the element's path.<p>
     * 
     * @return the element's path
     * 
     * @throws CmsException if something goes wrong
     */
    public String getPath() throws CmsException {

        return getCmsObject().getSitePath(getResource());
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
            m_resource = getCmsObject().readResource(getRequest().getParameter(I_CmsResourceLoader.PARAMETER_ELEMENT));
        }
        return m_resource;
    }

    /**
     * Returns the element's type name.<p>
     * 
     * @return the element's type name
     * 
     * @throws CmsException if something goes wrong
     */
    public String getType() throws CmsException {

        return OpenCms.getResourceManager().getResourceType(getResource()).getTypeName();
    }

    /**
     * Returns the element's type localized name.<p>
     * 
     * @return the element's type localized name
     * 
     * @throws CmsException if something goes wrong
     */
    public String getTypeName() throws CmsException {

        return org.opencms.workplace.CmsWorkplaceMessages.getResourceName(
            getCmsObject().getRequestContext().getLocale(),
            getType());
    }

    /**
     * Checks if this element is a new or existing element, depending on the configuration file.<p>
     * 
     * @return <code>true</code> if this element is a new element, depending on the configuration file
     * 
     * @throws CmsException if something goes wrong
     */
    public boolean isNew() throws CmsException {

        return getPath().startsWith("/system/modules/org.opencms.workplace.ade.demo/config/");
    }
}
