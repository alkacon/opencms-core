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
 * Server side handler interface for button presses in a multivalue widget group row's button bar.<p>
 */
public interface I_CmsEditableGroupActionHandler {

    /**
     * Triggered when the 'add' button is clicked.<p>
     */
    void onAdd();

    /**
     * Triggered when the 'delete' button is clicked.<p>
     */
    void onDelete();

    /**
     * Triggered when the 'down' button is clicked.<p>
     */
    void onDown();

    /**
     * Triggered when the 'edit' button is clicked.<p>
     */
    void onEdit();

    /**
     * Triggered when the 'up' button is clicked.<p>
     */
    void onUp();

}
