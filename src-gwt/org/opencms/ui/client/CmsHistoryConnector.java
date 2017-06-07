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

import org.opencms.ui.components.extensions.CmsHistoryExtension;
import org.opencms.ui.shared.components.CmsHistoryState;

import com.google.gwt.user.client.History;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * The history extension connector.<p>
 */
@Connect(CmsHistoryExtension.class)
public class CmsHistoryConnector extends AbstractExtensionConnector {

    /** Serial version id. */
    private static final long serialVersionUID = 1172858679530092378L;

    /**
     * @see com.vaadin.client.ui.AbstractConnector#getState()
     */
    @Override
    public CmsHistoryState getState() {

        return (CmsHistoryState)super.getState();

    }

    /**
     * @see com.vaadin.client.ui.AbstractConnector#onStateChanged(com.vaadin.client.communication.StateChangeEvent)
     */
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {

        super.onStateChanged(stateChangeEvent);

        executeHistoryAction();
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        // nothing to do
    }

    /**
     * Executes the history action according to the history state.<p>
     */
    private void executeHistoryAction() {

        CmsHistoryState state = getState();
        if (state.isHistoryBack()) {
            History.back();
        } else if (state.isHistoryForward()) {
            History.forward();
        }
    }
}
