/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ade.containerpage;

import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.main.OpenCms;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Action element for container-page editor includes.<p>
 *
 * @since 8.0.0
 */
public class CmsContainerpageActionElement extends CmsGwtActionElement {

    /** The OpenCms module name. */
    public static final String CMS_MODULE_NAME = "org.opencms.ade.containerpage";

    /** The GWT module name. */
    public static final String GWT_MODULE_NAME = CmsCoreData.ModuleKey.containerpage.name();

    /** The current container page data. */
    private CmsCntPageData m_cntPageData;

    /**
     * Constructor.<p>
     *
     * @param context the JSP page context object
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsContainerpageActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

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
        String prefetchedData = exportDictionary(
            CmsCntPageData.DICT_NAME,
            I_CmsContainerpageService.class.getMethod("prefetch"),
            getCntPageData());
        sb.append(prefetchedData);
        sb.append(exportModuleScriptTag(GWT_MODULE_NAME));
        sb.append("<script type=\"text/javascript\" src=\"");
        sb.append(
            OpenCms.getLinkManager().substituteLinkForRootPath(
                getCmsObject(),
                "/system/workplace/editors/tinymce/opencms_plugin.js"));
        sb.append("\"></script>\n<style type=\"text/css\">\n    html {\n        overflow-y: scroll;\n    }\n</style>");

        return sb.toString();
    }

    /**
     * Returns the needed server data for client-side usage.<p>
     *
     * @return the needed server data for client-side usage
     */
    public CmsCntPageData getCntPageData() {

        if (m_cntPageData == null) {
            try {
                m_cntPageData = CmsContainerpageService.prefetch(getRequest());
            } catch (@SuppressWarnings("unused") CmsRpcException e) {
                // ignore, should never happen, and it is already logged
            }
        }
        return m_cntPageData;
    }
}
