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

package org.opencms.xml.containerpage;

import java.util.List;
import java.util.Set;

/**
 * A group container.<p>
 *
 * @since 8.0.0
 */
public class CmsGroupContainerBean {

    /** The group container description. */
    private String m_description;

    /** The group container elements.*/
    private List<CmsContainerElementBean> m_elements;

    /** The group container title. */
    private String m_title;

    /** The supported container types. */
    private Set<String> m_types;

    /**
     * Creates a new group container bean.<p>
     *
     * @param title the group container title
     * @param description the group container description
     * @param elements the group container elements
     * @param types the supported container types
     */
    public CmsGroupContainerBean(
        String title,
        String description,
        List<CmsContainerElementBean> elements,
        Set<String> types) {

        m_title = title;
        m_description = description;
        m_elements = elements;
        m_types = types;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the elements.<p>
     *
     * @return the elements
     */
    public List<CmsContainerElementBean> getElements() {

        return m_elements;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns the types.<p>
     *
     * @return the types
     */
    public Set<String> getTypes() {

        return m_types;
    }
}
