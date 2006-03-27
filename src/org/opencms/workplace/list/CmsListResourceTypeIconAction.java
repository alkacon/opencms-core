/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListResourceTypeIconAction.java,v $
 * Date   : $Date: 2006/03/27 14:52:28 $
 * Version: $Revision: 1.2 $
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

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.io.File;

/**
 * Displays an icon action for the resource type.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListResourceTypeIconAction extends CmsListExplorerDirectAction {

    /**
     * Default Constructor.<p>
     * 
     * @param id the unique id
     * @param wp the current workplace context
     */
    public CmsListResourceTypeIconAction(String id, A_CmsListExplorerDialog wp) {

        super(id, wp);
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getHelpText()
     */
    public CmsMessageContainer getHelpText() {

        if (super.getHelpText() == null || super.getHelpText().equals(EMPTY_MESSAGE)) {
            return Messages.get().container(Messages.GUI_EXPLORER_LIST_ACTION_RES_HELP_0);
        }
        return super.getHelpText();
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
     */
    public String getIconPath() {

        return getResourceUtil().getIconPathExplorer();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getName()
     */
    public CmsMessageContainer getName() {

        if (super.getName() == null) {
            return new CmsMessageContainer(null, getResourceUtil().getResourceTypeName());
        }
        return super.getName();
    }
    
    /**
     * @see org.opencms.workplace.list.CmsListExplorerDirectAction#defButtonHtml(org.opencms.jsp.CmsJspActionElement, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    protected String defButtonHtml(
        CmsJspActionElement jsp,
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
