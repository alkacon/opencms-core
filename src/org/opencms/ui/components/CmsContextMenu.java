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

package org.opencms.ui.components;

import org.vaadin.peter.contextmenu.ContextMenu;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;

/**
 * Extends the default context menu to allow slightly changed event handling.<p>
 */
public class CmsContextMenu extends ContextMenu {

    /** The serial version id. */
    private static final long serialVersionUID = -6944216817668052628L;

    /**
     * Opens the context menu of the given table.<p>
     *
     * @param event the click event
     * @param table the table
     */
    public void openForTable(ItemClickEvent event, Table table) {

        fireEvent(new ContextMenuOpenedOnTableRowEvent(this, table, event.getItemId(), event.getPropertyId()));
        open(event.getClientX(), event.getClientY());

    }

    /**
     * Opens the context menu of the given tree.<p>
     *
     * @param event the click event
     * @param tree the tree
     */
    public void openForTree(ItemClickEvent event, Tree tree) {

        fireEvent(new ContextMenuOpenedOnTreeItemEvent(this, tree, event.getItemId()));
        open(event.getClientX(), event.getClientY());
    }

    /**
     * Other than the default implementation, this method will not add the item click listener to open the context menu.<p>
     *
     * @see org.vaadin.peter.contextmenu.ContextMenu#setAsTableContextMenu(com.vaadin.ui.Table)
     */
    @Override
    public void setAsTableContextMenu(final Table table) {

        extend(table);
        setOpenAutomatically(false);
    }

    /**
     * Other than the default implementation, this method will not add the item click listener to open the context menu.<p>
     *
     * @see org.vaadin.peter.contextmenu.ContextMenu#setAsTreeContextMenu(com.vaadin.ui.Tree)
     */
    @Override
    public void setAsTreeContextMenu(final Tree tree) {

        extend(tree);
        setOpenAutomatically(false);
    }
}
