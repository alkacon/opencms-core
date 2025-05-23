/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.editprovider;

import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.shared.CmsCoreData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Action element for edit provider includes.<p>
 *
 * @since 8.0.3
 */
public class CmsEditProviderActionElement extends CmsGwtActionElement {

    /** The OpenCms module name. */
    public static final String CMS_MODULE_NAME = "org.opencms.ade.editprovider";

    /** The GWT module name. */
    public static final String GWT_MODULE_NAME = CmsCoreData.ModuleKey.editprovider.name();

    /**
     * Constructor.<p>
     *
     * @param context the JSP page context object
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsEditProviderActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);

    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#export()
     */
    @Override
    public String export() throws Exception {

        return "";
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#exportAll()
     */
    @Override
    public String exportAll() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(super.export());
        sb.append(exportModuleScriptTag(GWT_MODULE_NAME));
        return sb.toString();
    }

}
