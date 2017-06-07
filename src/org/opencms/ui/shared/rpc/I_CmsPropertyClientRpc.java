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

import com.vaadin.shared.communication.ClientRpc;

/**
 * Server-to-client Interface for the GWT dialog extension.<p>
 */
public interface I_CmsPropertyClientRpc extends ClientRpc {

    /**
     * Send a confirmation to the client that the properties for a new resource have been saved.<p>
     */
    void confirmSaveForNew();

    /**
     * Tells the client to open the property editing dialog for the resource with the given structure id.<p>
     *
     * @param structureId the structure id of a resource, as a string
     * @param editName if true, makes the file name editable
     * @param disablePrevNext if true, disables the prev/next button
     */
    void editProperties(String structureId, boolean editName, boolean disablePrevNext);

    /**
     * Opens the property editor for a new resource.<p>
     *
     * @param propertyData the property data serialized using GWT serialization
     */
    void editPropertiesForNewResource(String propertyData);

    /**
     * Sends next structure id to the client.<p>
     *
     * @param id the next structure id
     */
    void sendNextId(String id);

}
