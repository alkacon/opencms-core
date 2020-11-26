/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.properties;

import org.opencms.ade.properties.shared.I_CmsAdePropertiesConstants;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.main.OpenCms;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Action element class for opening the ADE properties dialog.<p>
 */
public class CmsPropertiesActionElement extends CmsGwtActionElement {

    /** The OpenCms module name. */
    public static final String CMS_MODULE_NAME = "org.opencms.ade.properties";

    /** The GWT module name. */
    public static final String GWT_MODULE_NAME = CmsCoreData.ModuleKey.properties.name();

    /**
     * Creates a new instance.<p>
     *
     * @param context the current page context
     * @param req the request
     * @param res the response
     */
    public CmsPropertiesActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#export()
     */
    @Override
    public String export() throws Exception {

        String resourcePath = getRequest().getParameter(I_CmsAdePropertiesConstants.PARAM_RESOURCE);
        CmsObject cms = getCmsObject();
        CmsResource resource = cms.readResource(resourcePath, CmsResourceFilter.IGNORE_EXPIRATION);
        StringBuffer buffer = new StringBuffer();
        buffer.append(exportMeta(I_CmsAdePropertiesConstants.META_RESOURCE, resource.getStructureId().toString()));
        buffer.append(
            exportMeta(
                I_CmsAdePropertiesConstants.META_BACKLINK,
                OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                    cms,
                    "/system/workplace/views/explorer/explorer_files.jsp")));
        return buffer.toString();
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#exportAll()
     */
    @Override
    public String exportAll() throws Exception {

        StringBuffer buffer = new StringBuffer();
        buffer.append(super.export());
        buffer.append(export());
        buffer.append(exportModuleScriptTag(GWT_MODULE_NAME));
        return buffer.toString();
    }

}
