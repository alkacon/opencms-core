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

package org.opencms.ui.contextmenu;

import org.opencms.ui.I_CmsDialogContext;

/**
 * Menu item which acts only as a container for nested menu items.<p>
 */
public class CmsSubmenu extends A_CmsContextMenuItem {

    /**
     * Creates a new instance.<p>
     *
     * @param id the id
     * @param parentId the parent id
     * @param title the title
     * @param order the order
     * @param priority the priority
     */
    public CmsSubmenu(String id, String parentId, String title, int order, int priority) {
        super(id, parentId, title, order, priority);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsContextMenuItem#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        // TODO Auto-generated method stub

    }

    /**
     * @see org.opencms.ui.contextmenu.A_CmsContextMenuItem#isLeafItem()
     */
    @Override
    public boolean isLeafItem() {

        return false;
    }

}
