/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.dnd.I_CmsDraggable;

import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a UI list item.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsListItem extends I_CmsTruncable, I_CmsDraggable {

    /**
     * Adds a widget to this list item.<p>
     *
     * @param w the widget to add
     */
    void add(Widget w);

    /**
     * Returns the logical id, it is not the HTML id.<p>
     *
     * @return the id
     */
    String getId();

    /**
     * Sets the logical id, it is not the HTML id.<p>
     *
     * @param id the id to set
     */
    void setId(String id);
}
