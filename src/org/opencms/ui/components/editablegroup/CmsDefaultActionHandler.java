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

/**
 * Default action handler for group buttons.
 */
public class CmsDefaultActionHandler implements I_CmsEditableGroupActionHandler {

    /** The editable group. */
    protected CmsEditableGroup m_group;

    /** The row. */
    protected I_CmsEditableGroupRow m_row;

    /**
     * Creates a new instance.
     *
     * @param group the group
     * @param row the row
     */
    public CmsDefaultActionHandler(CmsEditableGroup group, I_CmsEditableGroupRow row) {

        m_group = group;
        m_row = row;
    }

    /**
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroupActionHandler#onAdd()
     */
    public void onAdd() {

        m_group.addRowAfter(m_row);
    }

    /**
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroupActionHandler#onDelete()
     */
    public void onDelete() {

        m_group.remove(m_row);
    }

    /**
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroupActionHandler#onDown()
     */
    public void onDown() {

        m_group.moveDown(m_row);
    }

    /**
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroupActionHandler#onEdit()
     */
    public void onEdit() {
        // nop
    }

    /**
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroupActionHandler#onUp()
     */
    public void onUp() {

        m_group.moveUp(m_row);
    }

}
