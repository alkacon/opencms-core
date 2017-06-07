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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The inheritance container data.<p>
 */
public class CmsInheritanceContainer implements IsSerializable {

    /** The container reference client id. */
    private String m_clientId;

    /** The description. */
    private String m_description;

    /** The elements of the container. */
    private List<CmsContainerElement> m_elements;

    /** Indicates whether a change has occurred. */
    private boolean m_elementsChanged;

    /** Indicates whether elements have been moved. */
    private boolean m_elementsMoved;

    /** The name of the inheritance line. */
    private String m_name;

    /** Flag indicating if this is a new inheritance reference. */
    private boolean m_new;

    /** The title. */
    private String m_title;

    /**
     * Returns the clientId.<p>
     *
     * @return the clientId
     */
    public String getClientId() {

        return m_clientId;
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
     * Returns the container elements.<p>
     *
     * @return the elements
     */
    public List<CmsContainerElement> getElements() {

        return m_elements;
    }

    /**
     * Returns true whether the inheritance information has been changed.<p>
     *
     * @return true if the inheritance information has been changed
     */
    public boolean getElementsChanged() {

        return m_elementsChanged;
    }

    /**
     * Returns true when the elements have been moved.<p>
     *
     * @return true if the elements have been moved
     */
    public boolean getElementsMoved() {

        return m_elementsMoved;
    }

    /**
     * Returns the name of the inheritance line.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
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
     * Returns if this is a new inheritance reference.<p>
     *
     * @return <code>true</code> if this is a new inheritance reference
     */
    public boolean isNew() {

        return m_new;
    }

    /**
     * Sets the container reference client id.<p>
     *
     * @param clientId the container reference client id to set
     */
    public void setClientId(String clientId) {

        m_clientId = clientId;
    }

    /**
     * Sets the description.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the container elements.<p>
     *
     * @param elements the elements to set
     */
    public void setElements(List<CmsContainerElement> elements) {

        m_elements = elements;
    }

    /**
     * Sets the 'elements changed' flag.<p>
     *
     * @param elementsChanged the 'element changed' flag's new value
     */
    public void setElementsChanged(boolean elementsChanged) {

        m_elementsChanged = elementsChanged;
    }

    /**
     * Sets the 'elements moved' flag.<p>
     *
     * @param moved the 'element moved' flag's new value
     */
    public void setElementsMoved(boolean moved) {

        m_elementsMoved = moved;
    }

    /**
     * Sets the name of the inheritance line.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the if this is a new inheritance reference.<p>
     *
     * @param new1  <code>true</code> if this is a new inheritance reference
     */
    public void setNew(boolean new1) {

        m_new = new1;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

}
