/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.components.extensions;

import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.shared.rpc.I_CmsWindowCloseServerRpc;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

/**
 * Makes window close events available on the server side.<p>
 */
public class CmsWindowCloseExtension extends AbstractExtension implements I_CmsWindowCloseServerRpc {

    /** The serial version id. */
    private static final long serialVersionUID = 3978957151754705873L;

    /** The registered window close listeners. */
    private List<I_CmsWindowCloseListener> m_listeners;

    /**
     * Constructor.<p>
     *
     * @param ui the UI to extend
     */
    public CmsWindowCloseExtension(UI ui) {
        extend(ui);
        registerRpc(this);
        m_listeners = new ArrayList<I_CmsWindowCloseListener>();
    }

    /**
     * Adds a window close listener.<p>
     *
     * @param listener the listener to add
     */
    public void addWindowCloseListener(I_CmsWindowCloseListener listener) {

        m_listeners.add(listener);
    }

    /**
     * Removes the given window close listener.<p>
     *
     * @param listener the listener to remove
     */
    public void removeWindowCloseListener(I_CmsWindowCloseListener listener) {

        m_listeners.remove(listener);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsWindowCloseServerRpc#windowClosed(java.lang.String)
     */
    public void windowClosed(String syncToken) {

        for (I_CmsWindowCloseListener listener : m_listeners) {
            listener.onWindowClose();
        }
    }
}
