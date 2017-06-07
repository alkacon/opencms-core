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

package org.opencms.gwt.client.dnd;

import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;

import com.google.gwt.dom.client.Element;

/**
 * Interface defining all methods needed for a drag and drop target. These will mostly be called by the drag and drop handler.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsDropTarget {

    /**
     * Returns true if the given cursor position is over the drop target.<p>
     *
     * @param x the cursor client x position
     * @param y the cursor client y position
     * @param orientation the drag and drop orientation
     *
     * @return <code>true</code> if the given cursor position is over the drop target
     */
    boolean checkPosition(int x, int y, Orientation orientation);

    /**
     * Returns the drop target element.<p>
     * This must be the element, where all children will be attached.<p>
     *
     * @return the element
     */
    Element getElement();

    /**
     * Returns the index of the placeholder or -1 if no placeholder is attached.<p>
     *
     * @return the index
     */
    int getPlaceholderIndex();

    /**
     * Inserts a new placeholder.<p>
     *
     * @param placeholder the placeholder element
     * @param x the cursor client x position
     * @param y the cursor client y position
     * @param orientation the drag and drop orientation
     */
    void insertPlaceholder(Element placeholder, int x, int y, Orientation orientation);

    /**
     * Executed on drop.<p>
     *
     * @param draggable the draggable being dropped
     */
    void onDrop(I_CmsDraggable draggable);

    /**
     * Removes the placeholder.<p>
     */
    void removePlaceholder();

    /**
     * Repositions the placeholder.<p>
     *
     * @param x the cursor client x position
     * @param y the cursor client y position
     * @param orientation the drag and drop orientation
     */
    void repositionPlaceholder(int x, int y, Orientation orientation);

}
