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

package org.opencms.ade.containerpage.shared;

import java.util.List;
import java.util.Set;

/**
 * Group container bean.<p>
 *
 * @since 8.0.0
 */
public class CmsGroupContainer extends CmsContainerElement {

    /** The group-container description. */
    private String m_description;

    /** List of the contained elements id's. */
    private List<CmsContainerElement> m_elements;

    /** The container types supported by this group container. */
    private Set<String> m_types;

    /**
     * Returns the group-container description.<p>
     *
     * @return the group-container description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the list of the contained elements id's.<p>
     *
     * @return the list of the contained elements id's
     */
    public List<CmsContainerElement> getElements() {

        return m_elements;
    }

    /**
     * Returns the container types.<p>
     *
     * @return the container types
     */
    public Set<String> getTypes() {

        return m_types;
    }

    /**
     * Sets the group-container description.<p>
     *
     * @param description the group-container description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the elements contained in this container.<p>
     *
     * @param elements the elements
     */
    public void setElements(List<CmsContainerElement> elements) {

        m_elements = elements;

    }

    /**
     * Sets the types.<p>
     *
     * @param types the types to set
     */
    public void setTypes(Set<String> types) {

        m_types = types;
    }

}
