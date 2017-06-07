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
 * Client-to-server rpc interface for the GWT dialog extension.<p>
 */
public interface I_CmsPropertyServerRpc extends ServerRpc {

    /**
     * Disposes of the extension, and tells the server which resources have changed.<p>
     *
     * @param delayMillis time to delay the RPC (for allowing short background operations to finish)
     */
    void onClose(long delayMillis);

    /**
     * Removes the extension on the server side.<p>
     */
    void removeExtension();

    /**
     * Requests the id of the next file.<p>
     *
     * @param offset should be +1 for the next file, or -1 for the previous file
     */
    void requestNextFile(int offset);

    /**
     * Saves the properties for a new resource.<p>
     *
     * @param data the serialized property data (CmsPropertyChangeSet)
     */
    void savePropertiesForNewResource(String data);
}
