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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Container bean.<p>
 *
 * @since 8.0.0
 */
public class CmsContainer implements IsSerializable {

    /** Flag indicating the container is displayed in detail view only. */
    private boolean m_detailOnly;

    /** Flag indicating this container is used for detail views. */
    private boolean m_detailView;

    /** Flag indicating the container is editable by the current user. */
    private boolean m_editable;

    /** List of the contained elements. */
    private List<CmsContainerElement> m_elements;

    /** The content to display in case the container is empty. */
    private String m_emptyContainerContent;

    /**
     * Indicates whether this container not nested,
     * or in case of a detail only container page the starting point of a detail only container hierarchy.
     **/
    private boolean m_isRootContainer;

    /** The maximum number of elements. */
    private int m_maxElements;

    /** The container name. */
    private String m_name;

    /** The parent container name. */
    private String m_parentContainerName;

    /** The parent instance id. */
    private String m_parentInstanceId;

    /** The container type. */
    private String m_type;

    /** The width of the container. */
    private int m_width;

    /**
     * Constructor.<p>
     *
     * @param name the container name, also used as id within a container-page
     * @param type the container type
     * @param emptyContainerContent content to display in case the container is empty
     * @param width the width of the container
     * @param maxElements the maximum number of elements displayed by this container
     * @param detailView flag indicating this container is used for detail views
     * @param editable flag indicating the container is editable by the current user
     * @param elements the container elements id's
     * @param parentContainerName the parent container name
     * @param parentInstanceId the parent instance id
     */
    public CmsContainer(
        String name,
        String type,
        String emptyContainerContent,
        int width,
        int maxElements,
        boolean detailView,
        boolean editable,
        List<CmsContainerElement> elements,
        String parentContainerName,
        String parentInstanceId) {

        m_elements = elements;
        m_name = name;
        m_type = type;
        m_emptyContainerContent = emptyContainerContent;
        m_maxElements = maxElements;
        m_width = width;
        m_detailView = detailView;
        m_editable = editable;
        m_parentContainerName = parentContainerName;
        m_parentInstanceId = parentInstanceId;
    }

    /**
     * Hidden default constructor (for GWT serialization).<p>
     */
    protected CmsContainer() {

        // do nothing
    }

    /**
     * Splits the type attribute of a container into individual types.<p>
     *
     * @param containerTypeSpec the container type attribute
     *
     * @return the entries of the type attribute
     */
    public static Set<String> splitType(String containerTypeSpec) {

        return Sets.newHashSet(Arrays.asList(containerTypeSpec.trim().split(" *, *")));

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
     * Returns the content to display in case the container is empty.<p>
     *
     * @return the content to display in case the container is empty
     */
    public String getEmptyContainerContent() {

        return m_emptyContainerContent;
    }

    /**
     * Returns the maximum number of elements allowed in this container.<p>
     *
     * @return the maximum number of elements allowed in this container
     */
    public int getMaxElements() {

        return m_maxElements;
    }

    /**
     * Returns the container name, also used as HTML-id for the container DOM-element. Has to be unique within the template.<p>
     *
     * @return the container name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the parent container name.<p>
     *
     * @return the parent container name
     */
    public String getParentContainerName() {

        return m_parentContainerName;
    }

    /**
     * Returns the parent instance id.<p>
     *
     * @return the parent instance id
     */
    public String getParentInstanceId() {

        return m_parentInstanceId;
    }

    /**
     * Returns the container type. Used to determine the formatter used to render the contained elements.<p>
     *
     * @return the container type
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the container width.<p>
     *
     * @return the container width
     */
    public int getWidth() {

        return m_width;
    }

    /**
     * Returns <code>true</code> if the container is displayed in detail view only.<p>
     *
     * @return <code>true</code> if the container is displayed in detail view only
     */
    public boolean isDetailOnly() {

        return m_detailOnly;
    }

    /**
     * Returns if this container is used for detail views.<p>
     *
     * @return <code>true</code> if this container is used for detail views
     */
    public boolean isDetailView() {

        return m_detailView;
    }

    /**
     * Returns if the container is editable by the current user.<p>
     *
     * @return <code>true</code> if the container is editable by the current user
     */
    public boolean isEditable() {

        return m_editable;
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
     * Returns if this is a sub container.<p>
     *
     * @return <code>true</code> this is a sub container
     */
    public boolean isSubContainer() {

        return m_parentContainerName != null;
    }

    /**
     * Sets the detail only flag.<p>
     *
     * @param detailOnly <code>true</code> if the container is displayed in detail view only
     */
    public void setDeatilOnly(boolean detailOnly) {

        m_detailOnly = detailOnly;
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
     * Sets the maxElements.<p>
     *
     * @param maxElements the maxElements to set
     */
    public void setMaxElements(int maxElements) {

        m_maxElements = maxElements;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the if this container not nested,
     * or in case of a detail only container page the starting point of a detail only container hierarchy.<p>
     *
     * @param isRootContainer <code>true</code> if this container not nested
     */
    public void setRootContainer(boolean isRootContainer) {

        m_isRootContainer = isRootContainer;
    }

    /**
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "[Container name='"
            + m_name
            + "' type='"
            + m_type
            + "' numElements='"
            + (m_elements == null ? 0 : m_elements.size())
            + "']";
    }

}
