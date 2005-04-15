/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/administration/Attic/CmsAdminMenu.java,v $
 * Date   : $Date: 2005/04/15 13:04:29 $
 * Version: $Revision: 1.4 $
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

package org.opencms.workplace.administration;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsNamedObjectContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsNamedObjectContainer;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolInstallPoint;
import org.opencms.workplace.tools.CmsToolManager;
import org.opencms.workplace.tools.I_CmsToolHandler;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of the administration view leftside's menu.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsAdminMenu extends CmsToolDialog {

    /** Default link target constant. */
    public static final String DEFAULT_TARGET = "admin_content";

    /** Group container. */
    private I_CmsNamedObjectContainer m_groupContainer = new CmsNamedObjectContainer(true, true);

    /**
     * Default Constructor.<p>
     * 
     * @param jsp the jsp context
     */
    public CmsAdminMenu(CmsJspActionElement jsp) {

        super(jsp);

        installMenu();
    }

    /**
     * Adds a group.<p>
     * 
     * @param group the group
     * 
     * @see I_CmsNamedObjectContainer#addNamedObject(org.opencms.util.I_CmsNamedObject)
     */
    public void addGroup(CmsAdminMenuGroup group) {

        m_groupContainer.addNamedObject(group);
    }

    /**
     * Adds a menu item at the given position.<p>
     * 
     * @param group the group
     * @param position the position
     * 
     * @see I_CmsNamedObjectContainer#addNamedObject(org.opencms.util.I_CmsNamedObject, float)
     */
    public void addGroup(CmsAdminMenuGroup group, float position) {

        m_groupContainer.addNamedObject(group, position);
    }

    /**
     * Adds a new item to the specified menu.<p>
     * 
     * If the menu does not exist, it will be created.<p>
     * 
     * @param groupName the name of the group
     * @param name the name of the item
     * @param icon the icon to display
     * @param link the link to open when selected
     * @param helpText the help text to display
     * @param enabled if enabled or not
     * @param target the target frame to open the link into
     * 
     * @return the new item
     */
    public CmsAdminMenuItem addItem(
        String groupName,
        String name,
        String icon,
        String link,
        String helpText,
        boolean enabled,
        String target) {

        groupName = resolveMacros(groupName);
        CmsAdminMenuGroup group = getGroup(groupName);
        if (group == null) {
            group = new CmsAdminMenuGroup(groupName);
            addGroup(group);
        }
        CmsAdminMenuItem item = new CmsAdminMenuItem(name, icon, link, helpText, enabled, target);
        group.addMenuItem(item);
        return item;
    }

    /**
     * Returns the admin manager.<p>
     * 
     * @return the admin manager
     */
    public CmsToolManager getToolManager() {

        return OpenCms.getWorkplaceManager().getToolManager();
    }

    /**
     * Returns the requested group.<p>
     * 
     * @param name the name of the group
     * 
     * @return the group
     * 
     * @see I_CmsNamedObjectContainer#getObject(String)
     */
    public CmsAdminMenuGroup getGroup(String name) {

        return (CmsAdminMenuGroup)m_groupContainer.getObject(name);
    }

    /**
     * Generates the necesary html code for the groups.<p>
     * 
     * @param wp the page for which the code is generated
     * 
     * @return html code
     */
    public String groupHtml(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(2048);
        Iterator itHtml = m_groupContainer.elementList().iterator();
        while (itHtml.hasNext()) {
            CmsAdminMenuGroup group = (CmsAdminMenuGroup)itHtml.next();
            html.append(group.groupHtml(wp));
        }
        return html.toString();
    }

    /**
     * Creates the default menu as the root tool structure.<p>
     */
    public void installMenu() {

        // initialize the menu groups
        m_groupContainer.clear();

        // creates the context help menu
        CmsAdminMenuGroup helpMenu = new CmsAdminMenuGroup(key("admin.menu.groups.help"));
        helpMenu.addMenuItem(new CmsAdminContextHelpMenuItem());
        addGroup(helpMenu);

        Iterator itElems = getToolManager().getToolHandlers().iterator();
        while (itElems.hasNext()) {
            I_CmsToolHandler handler = (I_CmsToolHandler)itElems.next();
            // check visibility
            if (!getCms().existsResource(handler.getLink())) {
                continue;
            }
            Iterator itIPoints = handler.getInstallPoints().iterator();
            while (itIPoints.hasNext()) {
                CmsToolInstallPoint iPoint = (CmsToolInstallPoint)itIPoints.next();

                // leave out everything above the root
                if (!iPoint.getPath().startsWith(getToolManager().getRootToolPath(getCms()))) {
                    continue;
                }
                // cut out the root
                String path = iPoint.getPath().substring(getToolManager().getRootToolPath(getCms()).length());
                // special case of the root tool
                if (CmsStringUtil.isEmpty(path)) {
                    continue;
                }
                // skip initial '/'
                int pos = iPoint.getPath().indexOf(CmsToolManager.C_TOOLPATH_SEPARATOR);
                // only install if at first level
                if (path.indexOf(CmsToolManager.C_TOOLPATH_SEPARATOR, pos + 1) < 0) {
                    String groupName = resolveMacros(iPoint.getGroup());
                    CmsAdminMenuGroup group = getGroup(groupName);
                    if (group == null) {
                        group = new CmsAdminMenuGroup(groupName);
                        addGroup(group, iPoint.getPosition());
                    }
                    CmsAdminMenuItem item = new CmsAdminMenuItem(
                        handler.getName(),
                        handler.getSmallIconPath(),
                        getToolManager().linkForPath(iPoint.getPath()),
                        handler.getHelpText(),
                        handler.isEnabled(getCms()),
                        CmsAdminMenu.DEFAULT_TARGET);
                    group.addMenuItem(item, iPoint.getPosition());
                }
            }
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        fillParamValues(request);
    }

}