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

package org.opencms.ui.components.editablegroup;

import java.util.List;

import com.vaadin.ui.Component;

/**
 * Interface for a multivalue widget group.
 */
public interface I_CmsEditableGroup {

    /**
     * Adds a new row.<p>
     *
     * @param component the component to wrap in a row
     */
    void addRow(Component component);

    /**
     * Adds a new row after the given row.<p>
     *
     * @param row the row after which a new row should be inserted
     */
    void addRowAfter(CmsEditableGroupRow row);

    /**
     * Gets the list of rows.<p>
     *
     * @return the list of rows
     */
    List<CmsEditableGroupRow> getRows();

    /**
     * Moves a row down.<p>
     *
     * @param row the row to act on
     */
    void moveDown(CmsEditableGroupRow row);

    /**
     * Moves the row up.<p>
     *
     * @param row the row to act on
     */
    void moveUp(CmsEditableGroupRow row);

    /**
     * Removes a row.<p>
     *
     * @param row the row to remove
     */
    void remove(CmsEditableGroupRow row);

}
