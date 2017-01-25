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

import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Transformer;

/**
 * One container of a container page.<p>
 *
 * @since 8.0
 */
public class CmsContainerBean {

    /** A lazy initialized map that describes if a certain element if part of this container. */
    private transient Map<CmsUUID, Boolean> m_containsElement;

    /** Flag indicating this container is used on detail pages only. */
    private boolean m_detailOnly;

    /** The id's of of all elements in this container. */
    private transient List<CmsUUID> m_elementIds;

    /** The container elements. */
    private final List<CmsContainerElementBean> m_elements;

    /**
     * Indicates whether this container not nested,
     * or in case of a detail only container page the starting point of a detail only container hierarchy.
     **/
    private boolean m_isRootContainer;

    /** The maximal number of elements in the container. */
    private int m_maxElements;

    /** The container name. */
    private final String m_name;

    /** The optional container parameter. */
    private String m_param;

    /** The parent element instance id. */
    private String m_parentInstanceId;

    /** The container type. */
    private String m_type;

    /** The container width set by the rendering container tag. */
    private String m_width;

    /**
     * Creates a new container bean.<p>
     *
     * @param name the container name
     * @param type the container type
     * @param parentInstanceId the parent instance id
     * @param isRootContainer <code>true</code> if this container not nested
     * @param maxElements the maximal number of elements in the container
     * @param elements the elements
     **/
    public CmsContainerBean(
        String name,
        String type,
        String parentInstanceId,
        boolean isRootContainer,
        int maxElements,
        List<CmsContainerElementBean> elements) {

        m_name = name;
        m_type = type;
        m_parentInstanceId = parentInstanceId;
        m_isRootContainer = isRootContainer;
        m_maxElements = maxElements;
        m_elements = (elements == null
        ? Collections.<CmsContainerElementBean> emptyList()
        : Collections.unmodifiableList(elements));
    }

    /**
     * Creates a new container bean with an unlimited number of elements.<p>
     *
     * @param name the container name
     * @param type the container type
     * @param parentInstanceId the parent instance id
     * @param isRootContainer <code>true</code> if this container not nested
     * @param elements the elements
     **/
    public CmsContainerBean(
        String name,
        String type,
        String parentInstanceId,
        boolean isRootContainer,
        List<CmsContainerElementBean> elements) {

        this(name, type, parentInstanceId, isRootContainer, -1, elements);
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
            m_elementIds = new ArrayList<CmsUUID>(m_elements.size());
            for (CmsContainerElementBean element : m_elements) {
                m_elementIds.add(element.getId());
            }
        }
        return m_elementIds;
    }

    /**
     * Returns the elements in this container.<p>
     *
     * @return the elements in this container
     */
    public List<CmsContainerElementBean> getElements() {

        return m_elements;
    }

    /**
     * Returns the maximal number of elements in this container.<p>
     *
     * @return the maximal number of elements in this container
     */
    public int getMaxElements() {

        return m_maxElements;
    }

    /**
     * Returns the name of this container.<p>
     *
     * @return the name of this container
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the (optional) container parameter.<p>
     *
     * This is useful for a dynamically generated nested container,
     * to pass information to the formatter used inside that container.
     *
     * If no parameters have been set, this will return <code>null</code>
     *
     * @return the (optional) container parameter
     */
    public String getParam() {

        return m_param;
    }

    /**
     * Returns the the parent instance id.<p>
     *
     * @return the parent instance id
     */
    public String getParentInstanceId() {

        return m_parentInstanceId;
    }

    /**
     * Returns the type of this container.<p>
     *
     * @return the type of this container
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the container width set by the rendering container tag.<p>
     *
     * @return the container width
     */
    public String getWidth() {

        return m_width;
    }

    /**
     * Returns if this container is used on detail pages only.<p>
     *
     * @return <code>true</code> if this container is used on detail pages only
     */
    public boolean isDetailOnly() {

        return m_detailOnly;
    }

    /**
     * Returns if the given container is a nested container.<p>
     *
     * @return <code>true</code> if the given container is a nested container
     */
    public boolean isNestedContainer() {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_parentInstanceId);
    }

    /**
     * Returns if this container not nested,
     * or in case of a detail only container page the starting point of a detail only container hierarchy.<p>
     *
     * @return <code>true</code> if this container not nested
     */
    public boolean isRootContainer() {

        return m_isRootContainer;
    }

    /**
     * Sets if this container is used on detail pages only.<p>
     *
     * @param detailOnly <code>true</code> if this container is used on detail pages only
     */
    public void setDetailOnly(boolean detailOnly) {

        m_detailOnly = detailOnly;
    }

    /**
     * Sets the maximal number of elements in the container.<p>
     *
     * @param maxElements the maximal number of elements to set
     */
    public void setMaxElements(int maxElements) {

        m_maxElements = maxElements;
    }

    /**
     * Sets the container parameter.<p>
     *
     * This is useful for a dynamically generated nested container,
     * to pass information to the formatter used inside that container.
     *
     * @param param the parameter String to set
     */
    public void setParam(String param) {

        m_param = param;
    }

    /**
     * Sets the container type.<p>
     *
     * @param type the container type
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * Sets the client side render with of this container.<p>
     *
     * @param width the client side render with of this container
     */
    public void setWidth(String width) {

        m_width = width;
    }
}
