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

import com.google.common.base.Optional;
import com.google.gwt.dom.client.Element;

/**
 * Interface defining all methods used by drag and drop on a draggable element.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsDraggable {

    /**
     * Gets an optional pair of integers which are to be used as an "offset delta" for the drag and drop process.<p>
     *
     * @return an optional array containing exactly 2 entries [x,y]
     */
    Optional<int[]> getCursorOffsetDelta();

    /**
     * Creates the drag helper element and attaches it into the DOM.<p>
     *
     * @param target the drop target
     *
     * @return the drag helper element
     */
    Element getDragHelper(I_CmsDropTarget target);

    /**
     * Returns the draggable element.<p>
     *
     * @return the element
     */
    Element getElement();

    /**
     * Returns the draggable id.<p>
     *
     * @return the id
     */
    String getId();

    /**
     * Returns the parent drop target or <code>null</code> if there is none.<p>
     *
     * @return the parent drop target
     */
    I_CmsDropTarget getParentTarget();

    /**
     * Creates the drag placeholder element.<p>
     *
     * @param target the drop target
     *
     * @return the drag placeholder element
     */
    Element getPlaceholder(I_CmsDropTarget target);

    /**
     * Executed on drag cancel.<p>
     */
    void onDragCancel();

    /**
     * Executed on drop.<p>
     *
     * @param target the drop target
     */
    void onDrop(I_CmsDropTarget target);

    /**
     * Executed on drag start.<p>
     *
     * @param target the current drop target
     */
    void onStartDrag(I_CmsDropTarget target);
}
