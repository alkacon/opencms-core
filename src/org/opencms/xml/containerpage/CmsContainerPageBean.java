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

package org.opencms.xml.containerpage;

import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Transformer;

/**
 * Describes one locale of a container page.<p>
 *
 * @since 8.0
 */
public class CmsContainerPageBean {

    /** The containers. */
    private final Map<String, CmsContainerBean> m_containers;

    /** A lazy initialized map that describes if a certain element if part of this container. */
    private transient Map<CmsUUID, Boolean> m_containsElement;

    /** The id's of of all elements in this page. */
    private transient List<CmsUUID> m_elementIds;

    /** The container elements. */
    private transient List<CmsContainerElementBean> m_elements;

    /** The container names in the right order. */
    private final List<String> m_names;

    /** The supported types. */
    private final Set<String> m_types;

    /**
     * Creates a new container page bean.<p>
     *
     * @param containers the containers
     **/
    public CmsContainerPageBean(List<CmsContainerBean> containers) {

        // we want to preserve container order
        Map<String, CmsContainerBean> cnts = new LinkedHashMap<String, CmsContainerBean>();
        Set<String> types = new HashSet<String>();
        List<String> names = new ArrayList<String>();
        for (CmsContainerBean container : containers) {
            cnts.put(container.getName(), container);
            types.add(container.getType());
            names.add(container.getName());
        }
        m_containers = Collections.unmodifiableMap(cnts);
        m_types = Collections.unmodifiableSet(types);
        m_names = Collections.unmodifiableList(names);
    }

    /**
     * Returns <code>true</code> if the element with the provided id is contained in this container.<p>
     *
     * @param elementId the element id to check
     *
     * @return <code>true</code> if the element with the provided id is contained in this container
     */
    public boolean containsElement(CmsUUID elementId) {

        return getElementIds().contains(elementId);
    }

    /**
     * Returns all container of this page.<p>
     *
     * @return all container of this page
     */
    public Map<String, CmsContainerBean> getContainers() {

        return m_containers;
    }

    /**
     * Returns a lazy initialized map that describes if a certain element if part of this container.<p>
     *
     * @return a lazy initialized map that describes if a certain element if part of this container
     */
    public Map<CmsUUID, Boolean> getContainsElement() {

        if (m_containsElement == null) {
            m_containsElement = CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

                public Object transform(Object input) {

                    return Boolean.valueOf(containsElement((CmsUUID)input));
                }
            });
        }
        return m_containsElement;
    }

    /**
     * Returns the id's of all elements in this container.<p>
     *
     * @return the id's of all elements in this container
     */
    public List<CmsUUID> getElementIds() {

        if (m_elementIds == null) {
            m_elementIds = new ArrayList<CmsUUID>(getElements().size());
            for (CmsContainerElementBean element : getElements()) {
                m_elementIds.add(element.getId());
            }
        }
        return m_elementIds;
    }

    /**
     * Returns the elements of all containers in this page.<p>
     *
     * @return the elements of all containers in this page
     */
    public List<CmsContainerElementBean> getElements() {

        if (m_elements == null) {
            m_elements = new ArrayList<CmsContainerElementBean>();
            for (CmsContainerBean container : m_containers.values()) {
                m_elements.addAll(container.getElements());
            }
        }
        return m_elements;
    }

    /**
     * Returns the list of container names.<p>
     *
     * @return the list of container names
     */
    public List<String> getNames() {

        return m_names;
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
