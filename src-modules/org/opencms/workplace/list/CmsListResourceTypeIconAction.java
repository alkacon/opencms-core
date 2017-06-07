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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.list;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.io.File;

/**
 * Displays an icon action for the resource type.<p>
 *
 * @since 6.0.0
 */
public class CmsListResourceTypeIconAction extends CmsListExplorerDirectAction {

    /**
     * Default Constructor.<p>
     *
     * @param id the unique id
     */
    public CmsListResourceTypeIconAction(String id) {

        super(id);
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getHelpText()
     */
    @Override
    public CmsMessageContainer getHelpText() {

        if ((super.getHelpText() == null) || super.getHelpText().equals(EMPTY_MESSAGE)) {
            return Messages.get().container(Messages.GUI_EXPLORER_LIST_ACTION_RES_HELP_0);
        }
        return super.getHelpText();
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
     */
    @Override
    public String getIconPath() {

        return getResourceUtil().getIconPathExplorer();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getName()
     */
    @Override
    public CmsMessageContainer getName() {

        if (super.getName() == null) {
            return new CmsMessageContainer(null, getResourceUtil().getResourceTypeName());
        }
        return super.getName();
    }

    /**
     * @see org.opencms.workplace.list.CmsListExplorerDirectAction#defButtonHtml(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    protected String defButtonHtml(
        String id,
        String helpId,
        String name,
        String helpText,
        boolean enabled,
        String iconPath,
        String confirmationMessage,
        String onClick,
        boolean singleHelp) {

        StringBuffer html = new StringBuffer(1024);
        html.append("\t<span class=\"link");
        if (enabled) {
            html.append("\"");
        } else {
            html.append(" linkdisabled\"");
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
            if (!singleHelp) {
                html.append(" onMouseOver=\"sMH('");
                html.append(id);
                html.append("');\" onMouseOut=\"hMH('");
                html.append(id);
                html.append("');\"");
            } else {
                html.append(" onMouseOver=\"sMHS('");
                html.append(id);
                html.append("', '");
                html.append(helpId);
                html.append("');\" onMouseOut=\"hMH('");
                html.append(id);
                html.append("', '");
                html.append(helpId);
                html.append("');\"");
            }
        }
        if (enabled && CmsStringUtil.isNotEmptyOrWhitespaceOnly(onClick)) {
            html.append(" onClick=\"");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(confirmationMessage)) {
                html.append("if (confirm('" + CmsStringUtil.escapeJavaScript(confirmationMessage) + "')) {");
            }
            html.append(onClick);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(confirmationMessage)) {
                html.append(" }");
            }
            html.append("\"");
        }
        html.append(" title='");
        html.append(name);
        html.append("'");
        html.append(" style='display: block; width: 20px; height: 20px;'>");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(iconPath)) {
            html.append("<img src='");
            html.append(CmsWorkplace.getSkinUri());
            if (!enabled) {
                StringBuffer icon = new StringBuffer(128);
                int pos = iconPath.lastIndexOf('.');
                if (pos < 0) {
                    pos = iconPath.length();
                }
                icon.append(iconPath.substring(0, pos));
                icon.append("_disabled");
                icon.append(iconPath.substring(pos));
                String resourcesRoot = OpenCms.getSystemInfo().getWebApplicationRfsPath() + "resources/";
                File test = new File(resourcesRoot + icon.toString());
                if (test.exists()) {
                    html.append(icon);
                } else {
                    html.append(iconPath);
                }
            } else {
                html.append(iconPath);
            }
            html.append("'");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
                html.append(" alt='");
                html.append(name);
                html.append("'");
                html.append(" title='");
                html.append(name);
                html.append("'");
            }
            html.append(" style='width: 16px; height: 16px;");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getResourceUtil().getStyleSiblings())) {
                html.append(getResourceUtil().getStyleSiblings());
            }
            html.append("' >");
        }
        html.append("</span>\n");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText) && !singleHelp) {
            html.append("<div class='help' id='help");
            html.append(helpId);
            html.append("' onMouseOver=\"sMH('");
            html.append(id);
            html.append("');\" onMouseOut=\"hMH('");
            html.append(id);
            html.append("');\">");
            html.append(helpText);
            html.append("</div>\n");
        }
        return html.toString();
    }
}
