/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsOrgUnitWidget.java,v $
 * Date   : $Date: 2011/03/23 14:50:14 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

/**
 * Provides a OpenCms orgaizational unit selection widget, for use on a widget dialog.<p>
 *
 * @author Raphael Schnuck
 * @author Michael Moossen
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.5.6 
 */
public class CmsOrgUnitWidget extends A_CmsWidget {

    /** Configuration parameter to set the role the current user must have in the selected ou, optional. */
    public static final String CONFIGURATION_ROLE = "role";

    /** The role used in the popup window. */
    private CmsRole m_role;

    /**
     * Creates a new organizational unit selection widget.<p>
     */
    public CmsOrgUnitWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new user selection widget with the parameters to configure the popup window behaviour.<p>
     * 
     * @param role the role to restrict the organizational unit selection, can be <code>null</code>
     */
    public CmsOrgUnitWidget(CmsRole role) {

        m_role = role;
    }

    /**
     * Creates a new organizational unit selection widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    public CmsOrgUnitWidget(String configuration) {

        super(configuration);

    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getConfiguration()
     */
    @Override
    public String getConfiguration() {

        StringBuffer result = new StringBuffer(8);

        // append flags to configuration
        if (m_role != null) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append(CONFIGURATION_ROLE);
            result.append("=");
            result.append(m_role.getGroupName());
        }

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/orgunitselector.js"));
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(128);

        result.append("<td class=\"xmlTd\">");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\"><tr><td style=\"width: 100%;\">");
        result.append("<input style=\"width: 99%;\" class=\"xmlInput");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" value=\"");
        result.append(param.getStringValue(cms));
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\"></td>");
        result.append(widgetDialog.dialogHorizontalSpacer(10));
        result.append("<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

        StringBuffer buttonJs = new StringBuffer(8);
        buttonJs.append("javascript:openOrgUnitWin('");
        buttonJs.append(OpenCms.getSystemInfo().getOpenCmsContext());
        buttonJs.append("/system/workplace/commons/orgunit_selection.jsp");
        buttonJs.append("','EDITOR',  '");
        buttonJs.append(id);
        buttonJs.append("', document, ");
        if (m_role != null) {
            buttonJs.append("'");
            buttonJs.append(m_role.getGroupName());
            buttonJs.append("'");
        } else {
            buttonJs.append("null");
        }
        buttonJs.append(");");

        result.append(widgetDialog.button(
            buttonJs.toString(),
            null,
            "orgunit",
            org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_SEARCH_0,
            widgetDialog.getButtonStyle()));
        result.append("</tr></table>");
        result.append("</td></tr></table>");

        result.append("</td>");

        return result.toString();
    }

    /**
     * Returns the role, or <code>null</code> if none.<p>
     *
     * @return the role, or <code>null</code> if none
     */
    public CmsRole getRole() {

        return m_role;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsOrgUnitWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        m_role = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            int roleIndex = configuration.indexOf(CONFIGURATION_ROLE);
            if (roleIndex != -1) {
                // role is given
                String groupName = configuration.substring(CONFIGURATION_ROLE.length() + 1);
                if (groupName.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    groupName = groupName.substring(0, groupName.indexOf('|'));
                }
                m_role = CmsRole.valueOfGroupName(groupName);
            }
        }
        super.setConfiguration(configuration);
    }
}
