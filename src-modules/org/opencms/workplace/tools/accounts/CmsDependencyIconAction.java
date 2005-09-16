/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsDependencyIconAction.java,v $
 * Date   : $Date: 2005/09/16 13:11:11 $
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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.list.CmsListDirectAction;

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
public class CmsDependencyIconAction extends CmsListDirectAction {

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** the cms object. */
    private final CmsObject m_cms;

    /** the type of the icon. */
    private final CmsDependencyIconActionType m_type;

    /**
     * Default Constructor.<p>
     * 
     * @param id the unique id
     * @param type the type of the icon
     * @param cms the cms context
     */
    public CmsDependencyIconAction(String id, CmsDependencyIconActionType type, CmsObject cms) {

        super(id + type.getId());
        m_cms = cms;
        m_type = type;
    }

    /**
     * @see org.opencms.workplace.list.CmsListDirectAction#buttonHtml(org.opencms.workplace.CmsWorkplace)
     */
    public String buttonHtml(CmsWorkplace wp) {

        if (m_type == CmsDependencyIconActionType.RESOURCE) {
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
        } else {
            return super.buttonHtml(wp);
        }
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

        if (m_type == CmsDependencyIconActionType.RESOURCE) {
            try {
                m_cms.getRequestContext().saveSiteRoot();
                m_cms.getRequestContext().setSiteRoot("/");
                int resourceType = m_cms.readResource(
                    getItem().get(CmsGroupDependenciesList.LIST_COLUMN_NAME).toString()).getTypeId();
                String typeName = OpenCms.getResourceManager().getResourceType(resourceType).getTypeName();
                return "filetypes/" + OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName).getIcon();
            } catch (CmsException e) {
                return super.getIconPath();
            } finally {
                m_cms.getRequestContext().restoreSiteRoot();
            }
        } else if (m_type == CmsDependencyIconActionType.USER) {
            return PATH_BUTTONS + "user.png";
        } else if (m_type == CmsDependencyIconActionType.GROUP) {
            return PATH_BUTTONS + "group.png";
        } else {
            return super.getIconPath();
        }
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public CmsDependencyIconActionType getType() {

        return m_type;
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
     */
    public boolean isVisible() {

        boolean visible = false;
        if (getItem() != null) {
            CmsUUID id = new CmsUUID(getItem().getId());
            try {
                if (m_type == CmsDependencyIconActionType.USER) {
                    m_cms.readUser(id);
                    visible = true;
                } else if (m_type == CmsDependencyIconActionType.GROUP) {
                    m_cms.readGroup(id);
                    visible = true;
                } else if (m_type == CmsDependencyIconActionType.RESOURCE) {
                    m_cms.getRequestContext().saveSiteRoot();
                    m_cms.getRequestContext().setSiteRoot("/");
                    m_cms.readResource(getItem().get(CmsGroupDependenciesList.LIST_COLUMN_NAME).toString());
                    visible = true;
                } else {
                    // never happens
                    visible = false;
                }
            } catch (CmsException e) {
                // not visible
                visible = false;
            } finally {
                if (m_type == CmsDependencyIconActionType.RESOURCE) {
                    m_cms.getRequestContext().restoreSiteRoot();
                }
            }
        } else {
            visible = super.isVisible();
        }
        return visible;
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