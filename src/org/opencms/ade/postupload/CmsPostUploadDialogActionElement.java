/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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

package org.opencms.ade.postupload;

import org.opencms.ade.postupload.shared.CmsPostUploadDialogBean;
import org.opencms.ade.postupload.shared.I_CmsDialogConstants;
import org.opencms.ade.postupload.shared.rpc.I_CmsPostUploadDialogService;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Action element class used by the upload hook JSP from the org.opencms.ade.postupload module.<p>
 */
public class CmsPostUploadDialogActionElement extends CmsGwtActionElement {

    /** The OpenCms module name. */
    public static final String CMS_MODULE_NAME = "org.opencms.ade.postupload";

    /** The GWT module name. */
    public static final String GWT_MODULE_NAME = CmsCoreData.ModuleKey.postupload.name();

    /** The dialog data. */
    private CmsPostUploadDialogBean m_dialogData;

    /** Flag to control if property configuration should be used. */
    private boolean m_useConfiguration;

    /** Flag to control if configured basic properties should be shown. */
    private boolean m_addBasicProperties;

    /**
     * Creates a new instance.<p>
     *
     * @param pageContext the current page context
     * @param request the servlet request
     * @param response the servlet response
     */
    public CmsPostUploadDialogActionElement(
        PageContext pageContext,
        HttpServletRequest request,
        HttpServletResponse response) {

        super(pageContext, request, response);
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
        sb.append(exportCloseLink());
        sb.append(exportDictionary(
            CmsPostUploadDialogBean.DICT_NAME,
            I_CmsPostUploadDialogService.class.getMethod("prefetch"),
            getDialogData()));
        sb.append(exportModuleScriptTag(GWT_MODULE_NAME));
        return sb.toString();
    }

    /**
     * Returns the needed server data for client-side usage.<p>
     *
     * @return the needed server data for client-side usage
     * @throws CmsRpcException if something goes wrong
     */
    public CmsPostUploadDialogBean getDialogData() throws CmsRpcException {

        if (m_dialogData == null) {
            m_dialogData = CmsPostUploadDialogService.prefetch(getRequest());
        }
        m_dialogData.setUsePropertyConfiguration(m_useConfiguration);
        m_dialogData.setAddBasicProperties(m_addBasicProperties);
        return m_dialogData;
    }

    /**
     * Set a flag, indicating if basic properties as configured in the sitemap are merged into the
     * properties shown on file upload.
     *
     * @param addBasicProperties flag, indicating if basic properties as configured in the sitemap should be added
     */
    public void setAddBasicProperties(final boolean addBasicProperties) {

        m_addBasicProperties = addBasicProperties;
    }

    /**
     * Enables / disables use of property configurations.<p>
     *
     * @param useConfiguration if true , use the property configurations
     */
    public void setUsePropertyConfiguration(boolean useConfiguration) {

        m_useConfiguration = useConfiguration;
    }

    /**
     * Returns a javascript tag that contains a variable deceleration that has the close link as value.<p>
     *
     * @return a javascript tag that contains a variable deceleration that has the close link as value
     */
    private String exportCloseLink() {

        String closeLink = null;
        if (getRequest().getAttribute(I_CmsDialogConstants.ATTR_CLOSE_LINK) != null) {
            closeLink = (String)getRequest().getAttribute(I_CmsDialogConstants.ATTR_CLOSE_LINK);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(closeLink)) {
            closeLink = CmsWorkplace.FILE_EXPLORER_FILELIST;
        }

        StringBuffer sb = new StringBuffer();
        // var closeLink = '/system/workplace/views/explorer/explorer_files.jsp';
        sb.append(wrapScript("var ", I_CmsDialogConstants.ATTR_CLOSE_LINK, " = \'", closeLink, "\';"));
        return sb.toString();
    }

}
