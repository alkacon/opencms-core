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

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;

/**
 * Drag and drop drag handle.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsDragHandle extends HasMouseDownHandlers {

    /**
     * Returns the draggable item.<p>
     *
     * @return the draggable item
     */
    I_CmsDraggable getDraggable();

    /**
     * Returns the handle element.<p>
     *
     * @return the handle element
     */
    Element getElement();

    /**
     * Sets the title attribute.<p>
     *
     * @param title the title
     */
    void setTitle(String title);
}
