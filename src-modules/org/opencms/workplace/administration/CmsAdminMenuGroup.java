/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/administration/CmsAdminMenuGroup.java,v $
 * Date   : $Date: 2005/06/23 11:11:23 $
 * Version: $Revision: 1.5 $
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

package org.opencms.workplace.administration;

import org.opencms.util.CmsIdentifiableObjectContainer;
import org.opencms.workplace.CmsWorkplace;

import java.util.Iterator;
import java.util.List;

/**
 * Container for menu items that generates the necesary html code for a group of items.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAdminMenuGroup {

    /** Item container. */
    private final CmsIdentifiableObjectContainer m_container = new CmsIdentifiableObjectContainer(true, true);

    /** Dhtml id, from name. */
    private final String m_id;

    /** Name of the group. */
    private final String m_name;

    /**
     * Default Constructor.<p> 
     * 
     * @param id a unique id
     * @param name the name of the group
     */
    public CmsAdminMenuGroup(String id, String name) {

        m_id = id;
        m_name = name;
    }

    /**
     * Adds a menu item.<p>
     * 
     * @param item the item
     * 
     * @see org.opencms.util.I_CmsIdentifiableObjectContainer#addIdentifiableObject(String, Object)
     */
    public void addMenuItem(CmsAdminMenuItem item) {

        m_container.addIdentifiableObject(item.getId(), item);
    }

    /**
     * Adds a menu item at the given position.<p>
     * 
     * @param item the item
     * @param position the position
     * 
     * @see org.opencms.util.I_CmsIdentifiableObjectContainer#addIdentifiableObject(String, Object, float)
     */
    public void addMenuItem(CmsAdminMenuItem item, float position) {

        m_container.addIdentifiableObject(item.getId(), item, position);
    }

    /**
     * Returns the dhtml unique id.<p>
     *
     * @return the dhtml unique id
     */
    public String getId() {

        return m_id;
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
     * Returns the group name.<p>
     *
     * @return the group name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the necessary html code.<p>
     * 
     * @param wp the jsp page to write the code to
     * 
     * @return html code
     */
    public String groupHtml(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(512);
        html.append(htmlStart(wp));
        Iterator itItem = m_container.elementList().iterator();
        while (itItem.hasNext()) {
            CmsAdminMenuItem item = (CmsAdminMenuItem)itItem.next();
            html.append(item.itemHtml(wp));
            html.append("\n");
        }
        html.append(htmlEnd());
        return html.toString();
    }

    /**
     * Generates the last part of the html code.<p>
     * 
     * @return html code
     */
    private String htmlEnd() {

        StringBuffer html = new StringBuffer(512);
        html.append("\t\t\t\t\t\t</td>\n");
        html.append("\t\t\t\t\t</tr>\n");
        html.append("\t\t\t\t</table>\n");
        html.append("\t\t\t</div>\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("</table>\n");
        return html.toString();
    }

    /**
     * Generates the first part of the html code.<p>
     * 
     * @param wp the workplace
     * 
     * @return html code
     */
    private String htmlStart(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(1024);
        html.append("<table border='0' cellspacing='0' cellpadding='0' width='100%' class='navOpened' id='");
        html.append(getId());
        html.append("'>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td class='titleBorder'>\n");
        html.append("\t\t\t<table border='0' cellspacing='0' cellpadding='0' width='100%' class='navTitle' onMouseOver='mouseGroupEvent(this, true);' onMouseOut='mouseGroupEvent(this, false);' onClick=\"return openGroup('");
        html.append(getId());
        html.append("');\" >\n");
        html.append("\t\t\t\t<tr>\n");
        html.append("\t\t\t\t\t<td class='titleText' width='100%'>");
        html.append(wp.resolveMacros(getName()));
        html.append("</td>\n");
        html.append("\t\t\t\t</tr>\n");
        html.append("\t\t\t</table>\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr><tr>\n");
        html.append("\t\t<td class='treeBorder'>\n");
        html.append("\t\t\t<div class='tree'>\n");
        html.append("\t\t\t\t<table border='0' cellspacing='0' cellpadding='0' width='100%'>\n");
        html.append("\t\t\t\t\t<tr>\n");
        html.append("\t\t\t\t\t\t<td>\n");
        return html.toString();
    }

}