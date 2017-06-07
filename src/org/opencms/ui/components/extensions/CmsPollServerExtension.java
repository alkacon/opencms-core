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

import org.opencms.main.CmsLog;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.shared.rpc.I_CmsPollServerRpc;

import org.apache.commons.logging.Log;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.AbstractComponent;

/**
 * Allows the use of max height in combination with vaadin layout components.<p>
 */
public class CmsPollServerExtension extends AbstractExtension implements I_CmsPollServerRpc {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPollServerExtension.class);

    /** The serial version id. */
    private static final long serialVersionUID = 3978957151754705873L;

    /**
     * Constructor.<p>
     *
     * @param component the component to extend
     */
    public CmsPollServerExtension(AbstractComponent component) {
        extend(component);
        registerRpc(this);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsPollServerRpc#poll()
     */
    public void poll() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Client poll recieved.");
        }
        CmsAppWorkplaceUi.get().checkBroadcasts();
    }
}
