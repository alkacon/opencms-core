/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/administration/Attic/CmsAdminMenuGroup.java,v $
 * Date   : $Date: 2005/02/16 11:43:02 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.util.CmsNamedObjectContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsNamedObject;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.CmsHtmlUtil;

import java.util.Iterator;
import java.util.List;

/**
 * This is a container for menu items that generates the necesary html code.<p>
 * 
 * @author <a href="mailto:m.moossen@alkacon.com">Michael Moossen</a> 
 * @version $Revision: 1.1 $
 * @since 6.0
 */
public class CmsAdminMenuGroup implements I_CmsNamedObject {
    private final String m_id;
    private final String m_name;

    private final CmsNamedObjectContainer m_container = new CmsNamedObjectContainer(true, true);

    /**
     * Default Ctor.<p> 
     * 
     * @param name the name of the group
     */
    public CmsAdminMenuGroup(String name) {

        m_name = name;
        m_id = CmsHtmlUtil.getId(name);
    }

    /**
     * Returns the id.<p>
     * 
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the group name.<p>
     *
     * @return the group name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Adds a menu item.<p>
     * 
     * @param item the item
     * 
     * @see org.opencms.util.I_CmsNamedObjectContainer#addNamedObject(org.opencms.util.I_CmsNamedObject)
     */
    public void addMenuItem(CmsAdminMenuItem item) {

        m_container.addNamedObject(item);
    }

    /**
     * Adds a menu item at the given position.<p>
     * 
     * @param item the item
     * @param position the position
     * 
     * @see org.opencms.util.I_CmsNamedObjectContainer#addNamedObject(org.opencms.util.I_CmsNamedObject, float)
     */
    public void addMenuItem(CmsAdminMenuItem item, float position) {

        m_container.addNamedObject(item, position);
    }

    /**
     * Returns the requested menu item.<p>
     * 
     * @param name the name of the menu item
     * 
     * @return the menu item
     * 
     * @see org.opencms.util.I_CmsNamedObjectContainer#getObject(String)
     */
    public CmsAdminMenuItem getMenuItem(String name) {

        return (CmsAdminMenuItem)m_container.getObject(name);
    }

    /**
     * Retuns a list of menu items.<p>
     * 
     * @return a list of <code>{@link CmsAdminMenuItem}</code>s
     */
    public List getMenuItems() {

        return m_container.elementList();
    }

    /**
     * Returns the necessary html code.<p>
     * 
     * @param page the jsp page to write the code to
     * 
     * @return html code
     */
    public String groupHtml(CmsWorkplace page) {

        StringBuffer html = new StringBuffer(512);
        html.append(htmlStart(page));
        Iterator itItem = m_container.elementList().iterator();
        while (itItem.hasNext()) {
            CmsAdminMenuItem item = (CmsAdminMenuItem)itItem.next();
            html.append(CmsStringUtil.code(7, item.itemHtml(page)));
        }
        html.append(htmlEnd());
        return html.toString();
    }

    private String htmlEnd() {

        StringBuffer html = new StringBuffer(512);
        html.append(CmsStringUtil.code(6, "</td>"));
        html.append(CmsStringUtil.code(5, "</tr>"));
        html.append(CmsStringUtil.code(4, "</table>"));
        html.append(CmsStringUtil.code(3, "</div>"));
        html.append(CmsStringUtil.code(2, "</td>"));
        html.append(CmsStringUtil.code(1, "</tr>"));
        html.append(CmsStringUtil.code("</table>"));
        return html.toString();
    }

    private String htmlStart(CmsWorkplace page) {

        StringBuffer html = new StringBuffer(512);
        html.append(CmsStringUtil
            .code("<table border='0' cellspacing='0' cellpadding='0' width='100%' class='navOpened' id='"
                + getId()
                + "'>"));
        html.append(CmsStringUtil.code(1, "<tr>"));
        html.append(CmsStringUtil.code(2, "<td>"));
        html.append(CmsStringUtil
            .code(
                3,
                "<table border='0' cellspacing='0' cellpadding='0' width='100%' class='navTitle' onMouseOver='mouseGroupEvent(this, true);' onMouseOut='mouseGroupEvent(this, false);' onClick=\"return openGroup('"
                    + getId()
                    + "');\" >"));
        html.append(CmsStringUtil.code(4, "<tr>"));
        html.append(CmsStringUtil.code(5, "<td class='titleLeft'><img src='"
            + CmsWorkplace.getSkinUri()
            + "admin/images/topleft.gif"
            + "' border='0' alt=''/></td>"));
        html.append(CmsStringUtil.code(5, "<td class='titleText' width='100%'>" + page.resolveMacros(getName()) + "</td>"));
        html.append(CmsStringUtil.code(5, "<td class='titleHandle'><img src='"
            + CmsWorkplace.getSkinUri() + "admin/images/1x1.gif"
            + "' width='20' height='1' border='0' alt=''/></td>"));
        html.append(CmsStringUtil.code(5, "<td class='titleRight'><img src='"
            + CmsWorkplace.getSkinUri() + "admin/images/topright.gif"
            + "' border='0' alt=''/></td>"));
        html.append(CmsStringUtil.code(4, "</tr>"));
        html.append(CmsStringUtil.code(3, "</table>"));
        html.append(CmsStringUtil.code(2, "</td>"));
        html.append(CmsStringUtil.code(1, "</tr><tr>"));
        html.append(CmsStringUtil.code(2, "<td>"));
        html.append(CmsStringUtil.code(3, "<div class='tree'>"));
        html.append(CmsStringUtil.code(4, "<table border='0' cellspacing='0' cellpadding='0' width='100%'>"));
        html.append(CmsStringUtil.code(5, "<tr>"));
        html.append(CmsStringUtil.code(6, "<td>"));
        return html.toString();
    }

}