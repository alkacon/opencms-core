/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

/**
 * Interface representing the container methods to be used by overlay type CmsContainerJso and other types.<p>
 * 
 * @since 8.0.0
 */
public interface I_CmsContainer {

    /**
     * Returns the elements client id's contained in this container.<p>
     * 
     * @return the elements
     */
    String[] getElements();

    /**
     * Returns the maximum number of elements allowed in this container.<p>
     * 
     * @return the maximum number of elements
     */
    int getMaxElements();

    /**
     * Returns the container name, also used as HTML-id for the container DOM-element. Has to be unique within the template.<p>
     *  
     * @return the container name
     */
    String getName();

    /**
     * Returns the container type. Used to determine the formatter used to render the contained elements.<p>
     * 
     * @return the container type
     */
    String getType();

    /** 
     * Returns the width of the container.<p>
     * 
     * @return the width of the container 
     */
    int getWidth();

    /**
     * Returns <code>true</code> if the container is displayed in detail view only.<p>
     * 
     * @return <code>true</code> if the container is displayed in detail view only
     */
    boolean isDetailOnly();

    /**
     * Returns <code>true</code> if this container is used for the detail view. 
     * 
     * @return <code>true</code> if this is a detail view container
     */
    boolean isDetailView();

    /**
     * Sets the elements contained in this container.<p>
     * 
     * @param elements the elements
     */
    void setElements(String[] elements);

}
