/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/A_CmsHtmlIconButton.java,v $
 * Date   : $Date: 2005/06/14 15:53:26 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

/**
 * Default skeleton for an html icon button.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.9 $
 * @since 5.7.3
 */
public abstract class A_CmsHtmlIconButton implements I_CmsHtmlIconButton {

    /** Constant for an empty message. */
    public static final CmsMessageContainer C_EMPTY_MESSAGE = Messages.get().container(Messages.GUI_EMPTY_MESSAGE_0);
    
    /** Enabled flag. */
    private boolean m_enabled = true;

    /** Help text or description. */
    private CmsMessageContainer m_helpText;

    /** Path to the icon. */
    private String m_iconPath;

    /** unique id. */
    private String m_id;

    /** Display name. */
    private CmsMessageContainer m_name;

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
     * Generates a default html code for big icon buttons.<p>
     * 
     * If the name is empty only the icon is displayed.<br>
     * If the iconPath is empty only the name is displayed.<br>
     * If the onClic is empty no link is generated.<br>
     * If the helptext is empty no mouse events are generated.<br>
     * If not enabled be sure to take an according helptext.
     * <p>
     * 
     * @param style the style of the button
     * @param id the id
     * @param name the name
     * @param helpText the help text
     * @param enabled if enabled or not
     * @param iconPath the path to the icon
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
        String onClick) {

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
            html.append(" onmouseover=\"sMH('");
            html.append(id);
            html.append("');\" onmouseout=\"hMH('");
            html.append(id);
            html.append("');\"");
        } 
        if (enabled && CmsStringUtil.isNotEmptyOrWhitespaceOnly(onClick)) {
            html.append(" onClick=\"");
            html.append(onClick);
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
            html.append(iconPath);
            html.append("'");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
                html.append(" alt='");
                html.append(name);
                html.append("'");
                html.append(" title='");
                html.append(name);
                html.append("'");
            }
            html.append(">");
            if (style == CmsHtmlIconButtonStyleEnum.BIG_ICON_TEXT) {
                html.append("<br>");
            }
        }
        if (style != CmsHtmlIconButtonStyleEnum.SMALL_ICON_ONLY && CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(iconPath) && style != CmsHtmlIconButtonStyleEnum.BIG_ICON_TEXT) {
                html.append("&nbsp;");
            }
            if (enabled) {
                html.append("<a href='#'>");
            }
            html.append(name);
            if (enabled) {
                html.append("</a>");
            }
        }
        html.append("</span>\n");
        if (style == CmsHtmlIconButtonStyleEnum.BIG_ICON_TEXT) {
            html.append("</div>\n");
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
            html.append("<div class='help' id='help");
            html.append(id);
            html.append("' onmouseover=\"sMH('");
            html.append(id);
            html.append("');\" onmouseout=\"hMH('");
            html.append(id);
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
            m_helpText = C_EMPTY_MESSAGE;
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
            helpText = C_EMPTY_MESSAGE;
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
            name = C_EMPTY_MESSAGE;
        }
        m_name = name;
    }
}