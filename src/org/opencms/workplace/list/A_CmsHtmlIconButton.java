/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/Attic/A_CmsHtmlIconButton.java,v $
 * Date   : $Date: 2005/04/28 09:52:17 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.list;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

/**
 * Default skeleton for an html icon button.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.3 $
 * @since 5.7.3
 */
public abstract class A_CmsHtmlIconButton extends A_CmsHtmlButton implements I_CmsHtmlIconButton {

    /** Path to the icon. */
    private final String m_iconPath;

    /**
     * Default Constructor.<p>
     * 
     * @param id the id
     * @param name the name
     * @param helpText the help text
     * @param enabled if enabled or not
     * @param iconPath the path to the icon
     */
    public A_CmsHtmlIconButton(
        String id,
        CmsMessageContainer name,
        CmsMessageContainer helpText,
        boolean enabled,
        String iconPath) {

        super(id, name, helpText, enabled);
        m_iconPath = iconPath;
    }

    /**
     * Generates a default html code for big icon buttons.<p>
     * 
     * If the name is empty only the icon is displayed.<br>
     * If the iconPath is empty only the name is displayed.<br>
     * If the onClic is empty no link is generated.<br>
     * If the helptext is empty no mouse events are generated.
     * <p>
     * 
     * @param id the id
     * @param name the name
     * @param helpText the help text
     * @param enabled if enabled or not
     * @param iconPath the path to the icon
     * @param onClic the js code to execute
     * 
     * @return html code
     */
    public static String defaultBigButtonHtml(
        String id,
        String name,
        String helpText,
        boolean enabled,
        String iconPath,
        String onClic) {

        StringBuffer html = new StringBuffer(1024);
        html.append("<div class='bigLink'>\n");
        html.append("\t<a class='link");
        if (enabled) {
            html.append("' href='#'");
        } else {
            html.append(" disabled'");
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
            html.append(" onMouseOver=\"mouseHelpEvent('");
            html.append(id);
            html.append("', true);\" onMouseOut=\"mouseHelpEvent('");
            html.append(id);
            html.append("', false);\"");
        }
        if (enabled && CmsStringUtil.isNotEmptyOrWhitespaceOnly(onClic)) {
            html.append(" onClick=\"");
            html.append(onClic);
            html.append("\"");
        }
        html.append(">");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(iconPath)) {
            html.append("<img src='");
            html.append(CmsWorkplace.getSkinUri());
            html.append(iconPath);
            html.append("'><br>");
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
            html.append(name);
        }
        html.append("</a>\n");
        html.append("</div>\n");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
            html.append("<div class='tip' id='");
            html.append(id);
            html.append("'>");
            if (!enabled) {
                html.append("${key.");
                html.append(Messages.GUI_LIST_ACTION_DISABLED_0);
                html.append("} ");
            }
            html.append(helpText);
            html.append("</div>\n");
        }
        return html.toString();
    }

    /**
     * Generates a default html code for icon buttons.<p>
     * 
     * If the name is empty only the icon is displayed.<br>
     * If the iconPath is empty only the name is displayed.<br>
     * If the onClic is empty no link is generated.<br>
     * If the helptext is empty no mouse events are generated.
     * <p>
     * 
     * @param id the id
     * @param name the name
     * @param helpText the help text
     * @param enabled if enabled or not
     * @param iconPath the path to the icon
     * @param onClic the js code to execute
     * 
     * @return html code
     */
    public static String defaultButtonHtml(
        String id,
        String name,
        String helpText,
        boolean enabled,
        String iconPath,
        String onClic) {

        StringBuffer html = new StringBuffer(1024);
        html.append("<a class='link");
        if (enabled) {
            html.append("' href='#'");
        } else {
            html.append(" disabled'");
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
            html.append(" onMouseOver=\"mouseHelpEvent('");
            html.append(id);
            html.append("', true);\" onMouseOut=\"mouseHelpEvent('");
            html.append(id);
            html.append("', false);\"");
        }
        if (enabled && CmsStringUtil.isNotEmptyOrWhitespaceOnly(onClic)) {
            html.append(" onClick=\"");
            html.append(onClic);
            html.append("\"");
        } 
        html.append(">");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(iconPath)) {
            html.append("<img src='");
            html.append(CmsWorkplace.getSkinUri());
            html.append(iconPath);
            html.append("'>");
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
            html.append(name);
        }
        html.append("</a>\n");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
            html.append("<div class='tip' id='");
            html.append(id);
            html.append("'>");
            if (!enabled) {
                html.append("${key.");
                html.append(Messages.GUI_LIST_ACTION_DISABLED_0);
                html.append("} ");
            }
            html.append(helpText);
            html.append("</div>\n");
        }
        return html.toString();
    }

    /**
     * Returns the path to the icon.<p>
     *
     * @return the path to the icon
     */
    public String getIconPath() {

        return m_iconPath;
    }
}