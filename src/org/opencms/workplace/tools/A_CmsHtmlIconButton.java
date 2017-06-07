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

package org.opencms.workplace.tools;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.io.File;

/**
 * Default skeleton for an html icon button.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsHtmlIconButton implements I_CmsHtmlIconButton {

    /** Constant for an empty message. */
    public static final CmsMessageContainer EMPTY_MESSAGE = Messages.get().container(Messages.GUI_EMPTY_MESSAGE_0);

    /** unique id. */
    protected String m_id;

    /** Enabled flag. */
    private boolean m_enabled = true;

    /** Help text or description. */
    private CmsMessageContainer m_helpText;

    /** Path to the icon. */
    private String m_iconPath;

    /** Display name. */
    private CmsMessageContainer m_name;

    /** Visibility flag. */
    private boolean m_visible = true;

    /**
     * Default Constructor.<p>
     *
     * @param id the id
     */
    public A_CmsHtmlIconButton(String id) {

        m_id = id;
    }

    /**
     * Full Constructor.<p>
     *
     * @param id the id
     * @param name the name
     * @param helpText the help text
     * @param iconPath the path to the icon
     * @param enabled if enabled or not
     */
    public A_CmsHtmlIconButton(
        String id,
        CmsMessageContainer name,
        CmsMessageContainer helpText,
        String iconPath,
        boolean enabled) {

        this(id);
        setName(name);
        setHelpText(helpText);
        setIconPath(iconPath);
        setEnabled(enabled);
    }

    /**
     * Generates a default html code for icon buttons.<p>
     *
     * @param style the style of the button
     * @param id the id
     * @param name the name
     * @param helpText the help text
     * @param enabled if enabled or not
     * @param iconPath the path to the icon
     * @param confirmationMessage the optional confirmation message
     * @param onClick the js code to execute
     *
     * @return html code
     */
    public static String defaultButtonHtml(
        CmsHtmlIconButtonStyleEnum style,
        String id,
        String name,
        String helpText,
        boolean enabled,
        String iconPath,
        String confirmationMessage,
        String onClick) {

        return defaultButtonHtml(
            style,
            id,
            id,
            name,
            helpText,
            enabled,
            iconPath,
            confirmationMessage,
            onClick,
            false,
            null);
    }

    /**
     * Generates a default html code where several buttons can have the same help text.<p>
     *
     * @param style the style of the button
     * @param id the id
     * @param helpId the id of the helptext div tag
     * @param name the name, if empty only the icon is displayed
     * @param helpText the help text, if empty no mouse events are generated
     * @param enabled if enabled or not, if not set be sure to take an according helptext
     * @param iconPath the path to the icon, if empty only the name is displayed
     * @param confirmationMessage the confirmation message
     * @param onClick the js code to execute, if empty no link is generated
     * @param singleHelp if set, no helptext is written, you have to use the defaultHelpHtml() method later
     * @param rightHtml optional html code that should come direct after the button
     *
     * @return html code
     */
    public static String defaultButtonHtml(
        CmsHtmlIconButtonStyleEnum style,
        String id,
        String helpId,
        String name,
        String helpText,
        boolean enabled,
        String iconPath,
        String confirmationMessage,
        String onClick,
        boolean singleHelp,
        String rightHtml) {

        StringBuffer html = new StringBuffer(1024);
        if (style == CmsHtmlIconButtonStyleEnum.BIG_ICON_TEXT) {
            html.append("<div class='bigLink' id='img");
            html.append(id);
            html.append("'>\n");
        }
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
        if (style == CmsHtmlIconButtonStyleEnum.SMALL_ICON_ONLY) {
            html.append(" title='");
            html.append(name);
            html.append("'");
        }

        html.append(">");
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
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
                html.append(" alt='");
                html.append(helpText);
                html.append("'");
                html.append(" title='");
                html.append(helpText);
                html.append("'");
            }
            html.append(">");
            if (style == CmsHtmlIconButtonStyleEnum.BIG_ICON_TEXT) {
                html.append("<br>");
            }
        }
        if ((style != CmsHtmlIconButtonStyleEnum.SMALL_ICON_ONLY) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(iconPath)
                && (style != CmsHtmlIconButtonStyleEnum.BIG_ICON_TEXT)) {
                html.append("&nbsp;");
            }
            if (enabled) {
                if (style != CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT) {
                    html.append("<a href='#'>");
                } else {
                    html.append("<a href='#' style='white-space: nowrap;'>");
                }
            }
            html.append(name);
            if (enabled) {
                html.append("</a>");
            }

            // doesn't work in new dialog for the radio button cols
            // couldn't find a place where this is needed
            // if (style != CmsHtmlIconButtonStyleEnum.BIG_ICON_TEXT && name.length() > 1) {
            //  html.append("&nbsp;");
            // }
        }
        html.append("</span>");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(rightHtml)) {
            html.append(rightHtml);
        }
        if (style == CmsHtmlIconButtonStyleEnum.BIG_ICON_TEXT) {
            html.append("</div>\n");
        }
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
     * Generates html for the helptext when having one helptext for several buttons.<p>
     *
     * @param helpId the id of the help text
     * @param helpText the help text
     *
     * @return html code
     */
    public static String defaultHelpHtml(String helpId, String helpText) {

        StringBuffer html = new StringBuffer(1024);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
            html.append("<div class='help' id='help");
            html.append(helpId);
            html.append("' onMouseOut=\"hMH('");
            html.append(helpId);
            html.append("');\">");
            html.append(helpText);
            html.append("</div>\n");
        }
        return html.toString();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getHelpText()
     */
    public CmsMessageContainer getHelpText() {

        if (m_helpText == null) {
            m_helpText = EMPTY_MESSAGE;
        }
        return m_helpText;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getIconPath()
     */
    public String getIconPath() {

        return m_iconPath;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getName()
     */
    public CmsMessageContainer getName() {

        return m_name;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isEnabled()
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isVisible()
     */
    public boolean isVisible() {

        return m_visible;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_enabled = enabled;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#setHelpText(org.opencms.i18n.CmsMessageContainer)
     */
    public void setHelpText(CmsMessageContainer helpText) {

        if (helpText == null) {
            helpText = EMPTY_MESSAGE;
        }
        m_helpText = helpText;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#setIconPath(java.lang.String)
     */
    public void setIconPath(String iconPath) {

        m_iconPath = iconPath;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#setName(org.opencms.i18n.CmsMessageContainer)
     */
    public void setName(CmsMessageContainer name) {

        if (name == null) {
            name = EMPTY_MESSAGE;
        }
        m_name = name;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#setVisible(boolean)
     */
    public void setVisible(boolean visible) {

        m_visible = visible;
    }
}