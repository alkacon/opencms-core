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

package org.opencms.ui.shared.components;

import java.util.List;

import com.vaadin.shared.communication.ServerRpc;

/**
 * Client-to-server rpc interface for the GWT dialog extension.<p>
 */
public interface I_CmsGwtDialogServerRpc extends ServerRpc {

    /**
     *  Disposes of the extension and tells the server whether to re-init the UI.<p>
     *
     * @param reinitUI <code>true</code> to reinit the UI
     */
    void onClose(boolean reinitUI);

    /**
     * Disposes of the extension, and tells the server which resources have changed.<p>
     *
     * @param changedStructureIds the structure ids of changed resources, as strings
     * @param delayMillis time to delay the RPC (for allowing short background operations to finish)
     */
    void onClose(List<String> changedStructureIds, long delayMillis);
}
