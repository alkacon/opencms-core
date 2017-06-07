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

import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.io.File;

/**
 * Displays a 16x16 icon from the explorer view in a list action.<p>
 *
 * @since 6.0.0
 */
public class CmsListExplorerDirectAction extends CmsListDirectAction {

    /** The current resource util object. */
    private CmsResourceUtil m_resourceUtil;

    /**
     * Default Constructor.<p>
     *
     * @param id the unique id
     */
    public CmsListExplorerDirectAction(String id) {

        super(id);
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#buttonHtml(org.opencms.workplace.CmsWorkplace)
     */
    @Override
    public String buttonHtml(CmsWorkplace wp) {

        if (!isVisible()) {
            return "";
        }
        return defButtonHtml(
            getId() + getItem().getId(),
            getId(),
            resolveName(wp.getLocale()),
            resolveHelpText(wp.getLocale()),
            isEnabled(),
            getIconPath(),
            null,
            resolveOnClic(wp.getLocale()),
            getColumnForTexts() == null);
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListDirectAction#setItem(org.opencms.workplace.list.CmsListItem)
     */
    @Override
    public void setItem(CmsListItem item) {

        m_resourceUtil = ((A_CmsListExplorerDialog)getWp()).getResourceUtil(item);
        super.setItem(item);
    }

    /**
     * Generates a default html code where several buttons can have the same help text.<p>
     *
     * the only diff to <code>{@link org.opencms.workplace.tools.A_CmsHtmlIconButton#defaultButtonHtml(org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum, String, String, String, boolean, String, String, String)}</code>
     * is that the icons are 16x16.<p>
     *
     * @param id the id
     * @param helpId the id of the helptext div tag
     * @param name the name, if empty only the icon is displayed
     * @param helpText the help text, if empty no mouse events are generated
     * @param enabled if enabled or not, if not set be sure to take an according helptext
     * @param iconPath the path to the icon, if empty only the name is displayed
     * @param onClick the js code to execute, if empty no link is generated
     * @param confirmationMessage the confirmation message
     * @param singleHelp if set, no helptext is written, you have to use the defaultHelpHtml() method later
     *
     * @return html code
     *
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#defaultButtonHtml(org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum, String, String, String, boolean, String, String, String)
     */
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
                icon.append(iconPath.substring(0, iconPath.lastIndexOf('.')));
                icon.append("_disabled");
                icon.append(iconPath.substring(iconPath.lastIndexOf('.')));
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
            html.append(" style='width: 16px; height: 16px;' >");
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

    /**
     * Returns the current result Util.<p>
     *
     * @return the current result Util
     */
    protected CmsResourceUtil getResourceUtil() {

        return m_resourceUtil;
    }
}