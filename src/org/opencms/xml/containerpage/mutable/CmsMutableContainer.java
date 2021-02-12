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

package org.opencms.xml.containerpage.mutable;

import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A mutable bean representing a container for use in programmaticall editing container pages.
 *
 */
public class CmsMutableContainer {

    /** The mutable list of container elements. */
    private List<CmsContainerElementBean> m_elements = new ArrayList<>();

    /** The container name. */
    private String m_name;

    /** The container type. */
    private String m_type;

    /** The parent instance id. */
    private String m_parentInstanceId;

    /** True if this is a root container. */
    private boolean m_isRootContainer;

    /**
     * Creates a new instance.
     *
     * @param name the container name
     * @param type the container type
     * @param parentInstanceId the parent instance id
     * @param isRootContainer true if this is a root container
     * @param elements the list of container elements (will be copied)
     */
    public CmsMutableContainer(
        String name,
        String type,
        String parentInstanceId,
        boolean isRootContainer,
        List<CmsContainerElementBean> elements) {

        m_name = name;
        m_type = type;
        m_parentInstanceId = parentInstanceId;
        m_isRootContainer = isRootContainer;
        m_elements = elements.stream().map(elem -> elem.clone()).collect(Collectors.toList());
    }

    /**
     * Converts a CmsContainerBean to an instance of this class.
     *
     * @param container the immutable container bean
     * @return the new instance
     */
    public static CmsMutableContainer fromImmutable(CmsContainerBean container) {

        return new CmsMutableContainer(
            container.getName(),
            container.getType(),
            container.getParentInstanceId(),
            container.isRootContainer(),
            container.getElements());

    }

    /**
     * Gets the mutable list of container elements.
     *
     * @return the list of container elements
     */
    public List<CmsContainerElementBean> elements() {

        return m_elements;
    }

    /**
     * Gets the container name.
     *
     * @return the container name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the parent instance id.
     *
     * @return the parent instance id
     */
    public String getParentInstanceId() {

        return m_parentInstanceId;
    }

    /**
     * Gets the container type
     *
     * @return the container type
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns true if this is a root container.
     *
     * @return true if this is a root container
     */
    public boolean isRootContainer() {

        return m_isRootContainer;
    }

    /**
     * Checks if the container matches the given user-readable name.
     *<p>
     * Because of nested containers, container names are not always just the values given to the cms:container tag,
     * but can also have a prefix consisting of the parent instance id of the element which contains them.
     *
     * @param name the user-readable name
     * @return true if the container matches the name
     */
    public boolean matches(String name) {

        return m_name.equals(name) || m_name.endsWith("-" + name);
    }

    /**
     * Sets the container name.
     *
     * @param name the container name
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the parent instance id.
     *
     * @param parentInstanceId the parent instance id
     */
    public void setParentInstanceId(String parentInstanceId) {

        m_parentInstanceId = parentInstanceId;
    }

    /**
     * Sets the 'is root container' property.
     *
     * @param isRootContainer true if this should be set as a root container
     */
    public void setRootContainer(boolean isRootContainer) {

        m_isRootContainer = isRootContainer;
    }

    /**
     * Sets the type.
     *
     * @param type the type
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * Converts this bean to a CmsContainerBean.
     *
     * @return a new  CmsContainerBean with the data from this bean
     */
    public CmsContainerBean toImmutable() {

        return new CmsContainerBean(
            m_name,
            m_type,
            m_parentInstanceId,
            m_isRootContainer,
            new ArrayList<CmsContainerElementBean>(m_elements));
    }

}
