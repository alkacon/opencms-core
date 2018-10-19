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

package org.opencms.ui.apps;

import java.util.List;

/**
 * Interface for apps which perform CRUD operations on any kind of element like CmsSites or CmsScheduledJobs.
 * @param <T> the type of element.
 */
public interface I_CmsCRUDApp<T> {

    /**
     * Creates the element of type T in the system.<p>
     *
     * @param element to be saved
     */
    void createElement(T element);

    /**
     * Performs a default action.
     *
     * @param elementId id of element
     */
    void defaultAction(String elementId);

    /**
     * Delete the given List of elements.<p>
     *
     * @param elementId of elements to be deleted
     */
    void deleteElements(List<String> elementId);

    /**
     * Get all Elements.<p>
     *
     * @return a list of all elements
     */
    List<T> getAllElements();

    /**
     * Reads out an element.<p>
     *
     * @param elementId of the object
     * @return the object of type T
     */
    T getElement(String elementId);

    /**
     * Writes a changed element to the system which already exists.<p>
     *
     * @param element to be saved
     */
    void writeElement(T element);
}
