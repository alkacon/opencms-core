/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/CmsToolGroup.java,v $
 * Date   : $Date: 2005/04/14 13:11:15 $
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

package org.opencms.workplace.tools;

import org.opencms.util.CmsNamedObjectContainer;
import org.opencms.util.I_CmsNamedObject;
import org.opencms.workplace.CmsWorkplace;

import java.util.Iterator;
import java.util.List;

/**
 * This is an admin tool group, it just generates the html code for
 * the group structure.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.3 $
 * @since 5.7.3
 */
public class CmsToolGroup implements I_CmsNamedObject {

    private final CmsNamedObjectContainer m_container = new CmsNamedObjectContainer(true, true);
    private final String m_id;
    private final String m_name;

    /**
     * Default Constructor.<p> 
     * 
     * @param name the name of the group
     */
    public CmsToolGroup(String name) {

        m_name = name;
        m_id = CmsHtmlUtil.getId(name);
    }

    /**
     * Adds an admin tool.<p>
     * 
     * @param adminTool the admin tool
     * 
     * @see org.opencms.util.I_CmsNamedObjectContainer#addNamedObject(org.opencms.util.I_CmsNamedObject)
     */
    public void addAdminTool(CmsTool adminTool) {

        m_container.addNamedObject(adminTool);
    }

    /**
     * Adds an admin tool at the given position.<p>
     * 
     * @param adminTool the admin tool
     * @param position the position
     * 
     * @see org.opencms.util.I_CmsNamedObjectContainer#addNamedObject(org.opencms.util.I_CmsNamedObject, float)
     */
    public void addAdminTool(CmsTool adminTool, float position) {

        m_container.addNamedObject(adminTool, position);
    }

    /**
     * Returns the requested admin tool.<p>
     * 
     * @param name the name of the admin tool
     * 
     * @return the admin tool
     * 
     * @see org.opencms.util.I_CmsNamedObjectContainer#getObject(String)
     */
    public CmsTool getAdminTool(String name) {

        return (CmsTool)m_container.getObject(name);
    }

    /**
     * Retuns a list of admin tools.<p>
     * 
     * @return a list of <code>{@link CmsTool}</code>s
     */
    public List getAdminTools() {

        return m_container.elementList();
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
     * Returns the necessary html code.<p>
     * 
     * @param wp the jsp page to write the code to
     * 
     * @return html code
     */
    public String groupHtml(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(2048);
        html.append(((CmsToolDialog)wp).iconsBlockAreaStart(wp.resolveMacros(getName())));
        Iterator itItem = m_container.elementList().iterator();
        while (itItem.hasNext()) {
            CmsTool item = (CmsTool)itItem.next();
            html.append(item.buttonHtml(wp));
        }
        html.append(((CmsToolDialog)wp).iconsBlockAreaEnd());
        return html.toString();
    }

}