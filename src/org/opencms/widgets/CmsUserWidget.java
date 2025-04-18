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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Provides a OpenCms User selection widget, for use on a widget dialog.<p>
 *
 * @since 6.0.0
 */
public class CmsUserWidget extends CmsSelectWidget {

    /** Configuration parameter to set the flags of the users to display, optional. */
    public static final String CONFIGURATION_FLAGS = "flags";

    /** Configuration parameter to set the group of users to display, optional. */
    public static final String CONFIGURATION_GROUP = "group";

    private static final Log LOG = CmsLog.getLog(CmsUserWidget.class);

    /** The the flags used in the popup window. */
    private Integer m_flags;

    /** The the group used in the popup window. */
    private String m_groupName;

    /**
     * Creates a new user selection widget.<p>
     */
    public CmsUserWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new user selection widget with the parameters to configure the popup window behaviour.<p>
     *
     * @param flags the group flags to restrict the group selection, can be <code>null</code>
     * @param groupName the group to restrict the user selection, can be <code>null</code>
     */
    public CmsUserWidget(Integer flags, String groupName) {

        m_flags = flags;
        m_groupName = groupName;
    }

    /**
     * Creates a new user selection widget with the given configuration.<p>
     *
     * @param configuration the configuration to use
     */
    public CmsUserWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getConfiguration()
     */
    @Override
    public String getConfiguration() {

        StringBuffer result = new StringBuffer(8);

        // append flags to configuration
        if (m_flags != null) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append(CONFIGURATION_FLAGS);
            result.append("=");
            result.append(m_flags);
        }
        // append group to configuration
        if (m_groupName != null) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append(CONFIGURATION_GROUP);
            result.append("=");
            result.append(m_groupName);
        }

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/userselector.js"));
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(128);

        result.append("<td class=\"xmlTd\">");
        result.append(
            "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\"><tr><td style=\"width: 100%;\">");
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
        result.append(
            "<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

        StringBuffer buttonJs = new StringBuffer(8);
        buttonJs.append("javascript:openUserWin('");
        buttonJs.append(OpenCms.getSystemInfo().getOpenCmsContext());
        buttonJs.append("/system/workplace/commons/user_selection.jsp");
        buttonJs.append("','EDITOR',  '");
        buttonJs.append(id);
        buttonJs.append("', document, ");
        if (m_flags != null) {
            buttonJs.append("'");
            buttonJs.append(m_flags);
            buttonJs.append("'");
        } else {
            buttonJs.append("null");
        }
        buttonJs.append(", ");
        if (m_groupName != null) {
            buttonJs.append("'");
            buttonJs.append(m_groupName);
            buttonJs.append("'");
        } else {
            buttonJs.append("null");
        }
        buttonJs.append(");");

        result.append(
            widgetDialog.button(
                buttonJs.toString(),
                null,
                "user",
                org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_SEARCH_0,
                widgetDialog.getButtonStyle()));
        result.append("</tr></table>");
        result.append("</td></tr></table>");

        result.append("</td>");

        return result.toString();
    }

    /**
     * Returns the flags, or <code>null</code> if all.<p>
     *
     * @return the flags, or <code>null</code> if all
     */
    public Integer getFlags() {

        return m_flags;
    }

    /**
     * Returns the group name, or <code>null</code> if all.<p>
     *
     * @return the group name, or <code>null</code> if all
     */
    public String getGroupName() {

        return m_groupName;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    public String getWidgetName() {

        return CmsSelectWidget.class.getName();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsUserWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        m_groupName = null;
        m_flags = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            int flagsIndex = configuration.indexOf(CONFIGURATION_FLAGS);
            if (flagsIndex != -1) {
                // user is given
                String flags = configuration.substring(CONFIGURATION_FLAGS.length() + 1);
                if (flags.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    flags = flags.substring(0, flags.indexOf('|'));
                }
                try {
                    m_flags = Integer.valueOf(flags);
                } catch (Throwable t) {
                    // invalid flags
                }
            }
            int groupIndex = configuration.indexOf(CONFIGURATION_GROUP);
            if (groupIndex != -1) {
                // group is given
                String group = configuration.substring(CONFIGURATION_GROUP.length() + 1);
                if (group.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    group = group.substring(0, group.indexOf('|'));
                }
                m_groupName = group;
            }
        }
        super.setConfiguration(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsSelectWidget#parseSelectOptions(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected List<CmsSelectWidgetOption> parseSelectOptions(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        List<CmsSelectWidgetOption> options = new ArrayList<>();
        options.add(new CmsSelectWidgetOption("", true, ""));
        try {
            List<CmsUser> users;
            if (m_groupName != null) {
                users = cms.getUsersOfGroup(m_groupName);
            } else {
                users = OpenCms.getOrgUnitManager().getUsers(cms, "/", true);
            }
            for (CmsUser user : users) {
                CmsSelectWidgetOption option = new CmsSelectWidgetOption(user.getName(), false, user.getFullName());
                options.add(option);
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return options;
    }
}
