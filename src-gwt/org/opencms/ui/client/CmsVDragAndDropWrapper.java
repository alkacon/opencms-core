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

package org.opencms.ui.client;

import com.vaadin.client.communication.MessageSender;
import com.vaadin.client.ui.VDragAndDropWrapper;
import com.vaadin.client.ui.draganddropwrapper.DragAndDropWrapperConnector;

/**
 * Overrides VDragAndDropWrapper to solve RPC issue.
 */
public class CmsVDragAndDropWrapper extends VDragAndDropWrapper {

    /**
     * @see com.vaadin.client.ui.VDragAndDropWrapper#startNextUpload()
     */
    @Override
    public void startNextUpload() {

        // Workaround for mysterious bug where RPC calls would sometimes not be
        // sent to the server after an upload until another UI event
        // (e.g. mousemove) happened

        final DragAndDropWrapperConnector connector = (DragAndDropWrapperConnector)getConnector();
        final MessageSender sender = connector.getConnection().getMessageSender();
        sender.sendInvocationsToServer();
        super.startNextUpload();
    }
}
