/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListResourceIconAction.java,v $
 * Date   : $Date: 2005/10/10 10:53:19 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.list;

import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.io.File;

/**
 * Displays an icon action for dependency lists.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListResourceIconAction extends CmsListDirectAction {

    /** the cms object. */
    private final CmsObject m_cms;

    /** the id of the column with the resource type. */
    private final String m_resColumnTypeId;

    /**
     * Default Constructor.<p>
     * 
     * @param id the unique id
     * @param resColumnTypeId the id of the column with the resource type
     * @param cms the cms context
     */
    public CmsListResourceIconAction(String id, String resColumnTypeId, CmsObject cms) {

        super(id);
        m_cms = cms;
        m_resColumnTypeId = resColumnTypeId;
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#buttonHtml(org.opencms.workplace.CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        if (!isVisible()) {
            return "";
        }
        return defButtonHtml(
            wp.getJsp(),
            getId() + getItem().getId(),
            getId(),
            resolveName(wp.getLocale()),
            resolveHelpText(wp.getLocale()),
            isEnabled(),
            getIconPath(),
            resolveOnClic(wp.getLocale()),
            getColumnForTexts() == null);
    }

    /**
     * Returns the cms context.<p>
     *
     * @return the cms context
     */
    public CmsObject getCms() {

        return m_cms;
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
     */
    public String getIconPath() {

        try {
            int resourceType = Integer.parseInt(getItem().get(m_resColumnTypeId).toString());
            String typeName = OpenCms.getResourceManager().getResourceType(resourceType).getTypeName();
            return "filetypes/" + OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName).getIcon();
        } catch (CmsException e) {
            return super.getIconPath();
        }
    }

    /**
     * Generates a default html code where several buttons can have the same help text.<p>
     * 
     * the only diff to <code>{@link org.opencms.workplace.tools.A_CmsHtmlIconButton#defaultButtonHtml(CmsJspActionElement, CmsHtmlIconButtonStyleEnum, String, String, String, String, boolean, String, String, boolean)}</code>
     * is that the icons are 16x16.<p>
     * 
     * @param jsp the cms context, can be null
     * @param id the id
     * @param helpId the id of the helptext div tag
     * @param name the name, if empty only the icon is displayed
     * @param helpText the help text, if empty no mouse events are generated
     * @param enabled if enabled or not, if not set be sure to take an according helptext
     * @param iconPath the path to the icon, if empty only the name is displayed
     * @param onClick the js code to execute, if empty no link is generated
     * @param singleHelp if set, no helptext is written, you have to use the defaultHelpHtml() method later
     * 
     * @return html code
     * 
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#defaultButtonHtml(CmsJspActionElement, CmsHtmlIconButtonStyleEnum, String, String, String, String, boolean, String, String, boolean)
     */
    private String defButtonHtml(
        CmsJspActionElement jsp,
        String id,
        String helpId,
        String name,
        String helpText,
        boolean enabled,
        String iconPath,
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
            html.append(onClick);
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
                if (jsp != null) {
                    String resorcesRoot = jsp.getJspContext().getServletConfig().getServletContext().getRealPath(
                        "/resources/");
                    File test = new File(resorcesRoot + "/" + icon.toString());
                    if (test.exists()) {
                        html.append(icon);
                    } else {
                        html.append(iconPath);
                    }
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
            html.append("style='width: 16px; height: 16px;' >");
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