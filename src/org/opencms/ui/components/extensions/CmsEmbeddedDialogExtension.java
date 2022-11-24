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

package org.opencms.ui.components.extensions;

import org.opencms.ui.dialogs.CmsEmbeddedDialogsUI;
import org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC;
import org.opencms.ui.shared.rpc.I_CmsEmbeddingServerRpc;

import com.vaadin.server.AbstractExtension;

/**
 * The extension that provides RPC communication between the client and server side of embedded VAADIN dialogs.
 */
public class CmsEmbeddedDialogExtension extends AbstractExtension {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     * @param ui the UI instance to use
     *
     */
    public CmsEmbeddedDialogExtension(CmsEmbeddedDialogsUI ui) {

        super(ui);
        registerRpc(ui, I_CmsEmbeddingServerRpc.class);
    }

    /**
     * Gets the client RPC instance.
     *
     * @return the client RPC instance
     */
    public I_CmsEmbeddedDialogClientRPC getClientRPC() {

        return getRpcProxy(I_CmsEmbeddedDialogClientRPC.class);
    }

}
