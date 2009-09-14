/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsDefaultFormatterHelper.java,v $
 * Date   : $Date: 2009/09/14 13:59:35 $
 * Version: $Revision: 1.1.2.5 $
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
import org.opencms.workplace.CmsWorkplace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Helper bean to implement default formatters that differentiate between new and existing elements.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.5 $ 
 * 
 * @since 7.6 
 */
public class CmsDefaultFormatterHelper extends CmsJspActionElement {

    /** The element's resource. */
    private CmsResource m_resource;

    /** The resource type configuration. */
    private CmsTypeConfigurationItem m_resTypeConfig;

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
     * Returns the resource type icon path for the resource.<p>
     * 
     * @return the resource type icon path for the resource
     * 
     * @throws CmsException if something goes wrong 
     */
    public String getIconPath() throws CmsException {

        return CmsWorkplace.getResourceUri("filetypes/"
            + OpenCms.getWorkplaceManager().getExplorerTypeSetting(getType()).getIcon());
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
        return getResTypeConfig().getFolder();
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
     * Returns the resource type configuration.<p>
     * 
     * @return the resource type configuration
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsTypeConfigurationItem getResTypeConfig() throws CmsException {

        if (m_resTypeConfig == null) {
            CmsObject cms = getCmsObject();
            CmsResource cntPageRes = cms.readResource(getCmsObject().getRequestContext().getUri());
            CmsContainerPageBean cntPage = CmsContainerPageCache.getInstance().getCache(
                cms,
                cntPageRes,
                cms.getRequestContext().getLocale());
            if (cntPage.getResTypeConfig() != null) {
                CmsElementCreator ec = new CmsElementCreator(cms, cntPage.getResTypeConfig());
                m_resTypeConfig = ec.getConfiguration().get(getType());
            }
            if (m_resTypeConfig == null) {
                // missing configuration file
                m_resTypeConfig = new CmsTypeConfigurationItem("", "", "");
            }
        }
        return m_resTypeConfig;
    }

    /**
     * Returns the element's resource type name.<p>
     * 
     * @return the element's resource type name
     * 
     * @throws CmsException if something goes wrong
     */
    public String getType() throws CmsException {

        return OpenCms.getResourceManager().getResourceType(getResource()).getTypeName();
    }

    /**
     * Returns the element's resource type localized name.<p>
     * 
     * @return the element's resource type localized name
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

        return getPath().equals(getResTypeConfig().getSourceFile());
    }
}
