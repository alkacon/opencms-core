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

package org.opencms.ui.shared.rpc;

import com.vaadin.shared.communication.ServerRpc;

/**
 * Interface for the server side RPC for the CmsEditableGroupButtons component.<p>
 */
public interface I_CmsEditableGroupButtonsServerRpc extends ServerRpc {

    /**
     * Called when user clicks on 'Add' button.<p>
     */
    void onAdd();

    /**
     * Called when user clicks on 'Delete' button.<p>
     */
    void onDelete();

    /**
     * Called when user clicks on 'Down' button.<p>
     */
    void onDown();

    /**
     * Called when user clicks on the 'Edit' button.
     */
    void onEdit();

    /**
     * Called when user clicks on 'Up' button.<p>
     */
    void onUp();
}
