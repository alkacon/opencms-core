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

package org.opencms.ui.components;

import org.opencms.ui.shared.components.CmsGwtContextMenuButtonState;
import org.opencms.ui.shared.rpc.I_CmsGwtContextMenuServerRpc;
import org.opencms.util.CmsUUID;

import com.vaadin.ui.AbstractComponent;

/**
 * Vaadin widget for using the GWT based context menu button.
 */
public class CmsGwtContextMenuButton extends AbstractComponent {

    /**
     * Creates a new instance.
     *
     * @param id the structure id of the content for which the context menu should be displayed
     * @param rpc the RPC which should handle calls from the client
     */
    public CmsGwtContextMenuButton(CmsUUID id, I_CmsGwtContextMenuServerRpc rpc) {

        getState().setStructureId("" + id);
        addStyleName("o-gwt-contextmenu-button");
        registerRpc(rpc);

    }

    /**
     * @see com.vaadin.ui.AbstractComponent#getState()
     */
    @Override
    public CmsGwtContextMenuButtonState getState() {

        return (CmsGwtContextMenuButtonState)super.getState();
    }

}
