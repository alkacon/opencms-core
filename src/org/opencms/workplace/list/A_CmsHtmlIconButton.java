/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/Attic/A_CmsHtmlIconButton.java,v $
 * Date   : $Date: 2005/04/22 08:38:52 $
 * Version: $Revision: 1.1 $
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

import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

/**
 * Default skeleton for an html icon button.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.1 $
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
    public A_CmsHtmlIconButton(String id, String name, String helpText, boolean enabled, String iconPath) {

        super(id, name, helpText, enabled);
        m_iconPath = iconPath;
    }

    /**
     * Returns the path to the icon.<p>
     *
     * @return the path to the icon
     */
    public String getIconPath() {

        return m_iconPath;
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
    public static String defaultButtonHtml(String id, String name, String helpText, boolean enabled, String iconPath, String onClic) {
        
        StringBuffer html = new StringBuffer(1024);
        if (enabled) {
            html.append("<div class='commonButton' title='");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
                html.append(name);
            } else {
                html.append(id);
            }
            html.append("'");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
                html.append(" onMouseOver=\"mouseHelpEvent('");
                html.append(id);
                html.append("', true);\" onMouseOut=\"mouseHelpEvent('");
                html.append(id);
                html.append("', false);\"");
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(onClic)) {
                html.append(" onClick=\"");
                html.append(onClic);
                html.append("\"");
            }
            html.append(">\n");
            html.append("\t<button>\n\t\t");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
                html.append(name);
            } else {
                html.append(id);
            }
            html.append("\n\t</button>\n");
            html.append("\t<span");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(iconPath)) {
                html.append(" style='background-image: url(");
                html.append(CmsWorkplace.getSkinUri());
                html.append(iconPath);
                html.append(")'");
            } else {
                html.append(" style='padding-left: 0px;'");
            }
            html.append(">\n\t\t");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
                html.append(name);
            }
            html.append("\n\t</span>\n");
            html.append("</div>\n");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
                html.append("<div class='tip' id='");
                html.append(id);
                html.append("'>\n\t");
                html.append(helpText);
                html.append("\n</div>\n");
            }
        } else {
            html.append("<span class='commonButton' title='");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
                html.append(name);
            } else {
                html.append(id);
            }
            html.append("'");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
                html.append(" onMouseOver=\"mouseHelpEvent('");
                html.append(id);
                html.append("', true);\" onMouseOut=\"mouseHelpEvent('");
                html.append(id);
                html.append("', false);\"");
            }
            html.append(" disabled>\n");
            html.append("\t<button>\n\t\t");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
                html.append(name);
            } else {
                html.append(id);
            }
            html.append("\n\t</button>\n");
            html.append("\t<span style='cursor:default;");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(iconPath)) {
                html.append(" background-image: url(");
                html.append(CmsWorkplace.getSkinUri());
                html.append(iconPath);
                html.append(")");
            }
            html.append("'>\n\t\t");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
                html.append(name);
            }
            html.append("\n\t</span>\n");
            html.append("</span>\n");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(helpText)) {
                html.append("<div class='tip' id='");
                html.append(id);
                html.append("'>\n\t");
                html.append("${key.");
                html.append(Messages.GUI_LIST_ACTION_DISABLED_0);
                html.append("}");
                html.append(helpText);
                html.append("\n</div>\n");
            }
        }        
        return html.toString();        
    }
}