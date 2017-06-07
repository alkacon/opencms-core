/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.Iterator;
import java.util.List;

/**
 * This is an admin tool group, it just generates the html code for
 * the group structure.<p>
 *
 * @since 6.0.0
 */
public class CmsToolGroup {

    /** Container for the items. */
    private final CmsIdentifiableObjectContainer<CmsTool> m_container = new CmsIdentifiableObjectContainer<CmsTool>(
        true,
        true);

    /** Dhtml id. */
    private final String m_id;

    /** Display name. */
    private final String m_name;

    /**
     * Default Constructor.<p>
     *
     * @param id a unique id
     * @param name the name of the group
     */
    public CmsToolGroup(String id, String name) {

        m_id = id;
        m_name = name;
    }

    /**
     * Adds an admin tool.<p>
     *
     * @param adminTool the admin tool
     *
     * @see org.opencms.workplace.tools.CmsIdentifiableObjectContainer#addIdentifiableObject(String, Object)
     */
    public void addAdminTool(CmsTool adminTool) {

        m_container.addIdentifiableObject(adminTool.getId(), adminTool);
    }

    /**
     * Adds an admin tool at the given position.<p>
     *
     * @param adminTool the admin tool
     * @param position the position
     *
     * @see org.opencms.workplace.tools.CmsIdentifiableObjectContainer#addIdentifiableObject(String, Object, float)
     */
    public void addAdminTool(CmsTool adminTool, float position) {

        m_container.addIdentifiableObject(adminTool.getId(), adminTool, position);
    }

    /**
     * Returns a list of admin tools.<p>
     *
     * @return a list of <code>{@link CmsTool}</code>s
     */
    public List<CmsTool> getAdminTools() {

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
        Iterator<CmsTool> itItem = m_container.elementList().iterator();
        while (itItem.hasNext()) {
            CmsTool item = itItem.next();
            html.append(item.buttonHtml(wp));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(html.toString())) {
            html.insert(0, ((CmsToolDialog)wp).iconsBlockAreaStart(getName()));
            html.append(((CmsToolDialog)wp).iconsBlockAreaEnd());
        }
        return html.toString();
    }

}